package coedit

import coedit.connection.CoeditConnection
import coedit.listener.ChangeListener
import coedit.model.LockHandler
import coedit.model.LockState
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.ProjectComponent
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.fileEditor.FileEditorManagerListener
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Created by Alex Plate on 16.10.2018.
 */

class CoeditPlugin(private val myProject: Project) : ProjectComponent {

    val myConn: CoeditConnection = CoeditConnection()
    val myBasePath = myProject.basePath ?: throw RuntimeException("Cannot detect base path of project")
    val lockHandler = LockHandler(myProject, myBasePath)
    val messageBusConnection = ApplicationManager.getApplication().messageBus.connect()

    val editing: AtomicBoolean = AtomicBoolean(false)

    companion object {
        fun getInstance(project: Project): CoeditPlugin {
            return project.getComponent(CoeditPlugin::class.java)
        }
    }

    override fun projectOpened() {
    }

    fun subscribeToMessageBus() {
        try {
            messageBusConnection.subscribe(FileEditorManagerListener.FILE_EDITOR_MANAGER, object : FileEditorManagerListener {

                override fun fileOpened(source: FileEditorManager, file: VirtualFile) {
                    val relativePath = Utils.getRelativePath(file, myProject)

                    if (lockHandler.stateOf(relativePath) == LockState.LOCKED_FOR_EDIT) {
                        return
                    }
                    Utils.registerListener(file, ChangeListener(myProject))
                }

                override fun fileClosed(source: FileEditorManager, file: VirtualFile) {
                    Utils.unregisterListener(file, ChangeListener(myProject))
                }
            })
        } catch (e: Throwable) {
            // Nothing. We already have this (on second connect)
        }
    }

    fun disconnectMessageBus() {
        messageBusConnection.disconnect()
    }
}