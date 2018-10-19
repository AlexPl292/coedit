package coedit

import coedit.connection.CoeditConnection
import coedit.connection.protocol.CoRequestFileCreation
import coedit.connection.protocol.CoRequestFileDeletion
import coedit.connection.protocol.CoRequestFileRename
import coedit.listener.ChangeListener
import coedit.model.LockHandler
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.ProjectComponent
import com.intellij.openapi.editor.EditorFactory
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFileManager
import com.intellij.openapi.vfs.newvfs.BulkFileListener
import com.intellij.openapi.vfs.newvfs.events.VFileCreateEvent
import com.intellij.openapi.vfs.newvfs.events.VFileDeleteEvent
import com.intellij.openapi.vfs.newvfs.events.VFileEvent
import com.intellij.openapi.vfs.newvfs.events.VFilePropertyChangeEvent
import com.intellij.util.messages.MessageBusConnection
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Created by Alex Plate on 16.10.2018.
 */

class CoeditPlugin(private val myProject: Project) : ProjectComponent {

    val myConn: CoeditConnection = CoeditConnection()
    val myBasePath = myProject.basePath ?: throw RuntimeException("Cannot detect base path of project")
    val lockHandler = LockHandler(myProject, myBasePath)
    var messageBusConnection: MessageBusConnection? = null

    val editing: AtomicBoolean = AtomicBoolean(false)

    companion object {
        fun getInstance(project: Project): CoeditPlugin {
            return project.getComponent(CoeditPlugin::class.java)
        }
    }

    override fun projectOpened() {
    }

    fun subscribeToMessageBus() {
        EditorFactory.getInstance().eventMulticaster.addDocumentListener(ChangeListener(myProject))
        messageBusConnection = ApplicationManager.getApplication().messageBus.connect()
        messageBusConnection?.subscribe(VirtualFileManager.VFS_CHANGES, object : BulkFileListener {
            override fun before(events: MutableList<out VFileEvent>) {
                events.forEach {
                    if (lockHandler.handleDisabledAndReset(it.path)) {
                        return
                    }
                    if (it is VFileCreateEvent) {
                        val relativePath = Utils.getRelativePath(it.path, myProject)
                        if (lockHandler.stateOf(relativePath) == null) {
                            myConn.send(CoRequestFileCreation(relativePath, it.isDirectory))
                        }
                    } else if (it is VFileDeleteEvent) {
                        val relativePath = Utils.getRelativePath(it.path, myProject)
                        val isDirectory = it.file.isDirectory

                        // TODO **DELETE FILE IN CASE OF BAD RESPONSE**
                        myConn.send(CoRequestFileDeletion(relativePath, isDirectory))
                    } else if (it is VFilePropertyChangeEvent && it.propertyName == "name") {
                        val relativePath = Utils.getRelativePath(it.path, myProject)
                        val newName = it.newValue as String
                        val isDirectory = it.file.isDirectory

                        myConn.send(CoRequestFileRename(relativePath, newName, isDirectory))
                    }
                }
            }
        })
    }

    fun disconnectMessageBus() {
        EditorFactory.getInstance().eventMulticaster.removeDocumentListener(ChangeListener(myProject))
        messageBusConnection?.disconnect()
    }
}