package coedit

import coedit.connection.CoeditConnection
import coedit.connection.protocol.CoRequestFileCreation
import coedit.connection.protocol.CoRequestFileDeletion
import coedit.connection.protocol.CoRequestFileRename
import coedit.listener.ChangeListener
import coedit.model.LockHandler
import coedit.model.LockState
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.ProjectComponent
import com.intellij.openapi.editor.EditorFactory
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.LocalFileOperationsHandler
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.VirtualFileManager
import com.intellij.openapi.vfs.newvfs.BulkFileListener
import com.intellij.openapi.vfs.newvfs.events.VFileCreateEvent
import com.intellij.openapi.vfs.newvfs.events.VFileEvent
import com.intellij.openapi.vfs.newvfs.events.VFilePropertyChangeEvent
import com.intellij.util.ThrowableConsumer
import com.intellij.util.messages.MessageBusConnection
import java.io.File
import java.io.IOException
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Created by Alex Plate on 16.10.2018.
 */

class CoeditPlugin(private val myProject: Project) : ProjectComponent {

    val myConn: CoeditConnection = CoeditConnection()
    val myBasePath = myProject.basePath ?: throw RuntimeException("Cannot detect base path of project")
    val lockHandler = LockHandler(myProject, myBasePath)
    private lateinit var messageBusConnection: MessageBusConnection

    val editing: AtomicBoolean = AtomicBoolean(false)

    // Very simple implementation of .ignore. Ignore files and dirs if path returns true on startsWith
    val coIgnore = listOf(".idea")

    companion object {
        fun getInstance(project: Project): CoeditPlugin {
            return project.getComponent(CoeditPlugin::class.java)
        }
    }

    override fun projectOpened() {
    }

    fun isIgnored(relativePath: String): Boolean {
        return coIgnore.any { relativePath.startsWith(it) }
    }

    fun subscribeToMessageBus() {
        EditorFactory.getInstance().eventMulticaster.addDocumentListener(ChangeListener(myProject))
        LocalFileSystem.getInstance().registerAuxiliaryFileOperationsHandler(object : LocalFileOperationsHandler {
            override fun afterDone(invoker: ThrowableConsumer<LocalFileOperationsHandler, IOException>?) {

            }

            override fun createFile(dir: VirtualFile?, name: String?): Boolean {
                return false
            }

            override fun rename(file: VirtualFile?, newName: String?): Boolean {
                return false
            }

            override fun move(file: VirtualFile?, toDir: VirtualFile?): Boolean {
                return false
            }

            override fun copy(file: VirtualFile?, toDir: VirtualFile?, copyName: String?): File? {
                return null
            }

            override fun createDirectory(dir: VirtualFile?, name: String?): Boolean {
                return false
            }

            override fun delete(file: VirtualFile?): Boolean {
                if (file == null) {
                    throw RuntimeException("IntelliJ IDEA error. Cannot get deleted file")
                }
                val relativePath = Utils.getRelativePath(file.path, myProject)
                if (lockHandler.handleDisabledAndReset(relativePath) || isIgnored(relativePath)) {
                    return false
                }

                val isDirectory = file.isDirectory
                if (isDirectory) {
                    for (lockedFile in lockHandler.allLockedFiles()) {
                        if (lockHandler.locksInDir(lockedFile, LockState.LOCKED_FOR_EDIT)) {
                            return true
                        }
                    }
                } else {
                    if (lockHandler.stateOf(relativePath) == LockState.LOCKED_FOR_EDIT) {
                        return true
                    }
                }

                lockHandler.lockByMe(relativePath)

                val response = myConn.sendAndWaitForResponse(CoRequestFileDeletion(relativePath, isDirectory))
                if (response.code != 200) {
                    lockHandler.unlock(relativePath)
                    return true
                }
                lockHandler.unlock(relativePath)
                return false
            }

        })
        messageBusConnection = ApplicationManager.getApplication().messageBus.connect()
        messageBusConnection.subscribe(VirtualFileManager.VFS_CHANGES, object : BulkFileListener {
            override fun before(events: MutableList<out VFileEvent>) {
                events.forEach {
                    val relativePath = Utils.getRelativePath(it.path, myProject)
                    if (lockHandler.handleDisabledAndReset(relativePath) || isIgnored(relativePath)) {
                        return
                    }
                    if (it is VFileCreateEvent) {
                        if (lockHandler.stateOf(relativePath) == null) {
                            myConn.sendAndWaitForResponse(CoRequestFileCreation(relativePath, it.isDirectory))
                        }
                    } else if (it is VFilePropertyChangeEvent && it.propertyName == "name") {
                        val newName = it.newValue as String
                        val isDirectory = it.file.isDirectory

                        myConn.sendAndWaitForResponse(CoRequestFileRename(relativePath, newName, isDirectory))
                    }
                }
            }
        })
    }

    fun disconnectMessageBus() {
        EditorFactory.getInstance().eventMulticaster.removeDocumentListener(ChangeListener(myProject))
        if (this::messageBusConnection.isInitialized) {
            messageBusConnection.disconnect()
        }
    }
}