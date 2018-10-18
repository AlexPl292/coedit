package coedit.action

import coedit.CoeditPlugin
import coedit.Utils
import coedit.listener.ChangeListener
import coedit.model.LockState
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.fileEditor.FileEditorManagerListener
import com.intellij.openapi.vfs.VirtualFile

/**
 * Created by Alex Plate on 18.10.2018.
 */

abstract class SetUpConnectionAction(name: String) : AnAction(name) {
    override fun actionPerformed(e: AnActionEvent?) {
        if (e == null) {
            throw RuntimeException("IntelliJ IDEA error. Cannot get action event")
        }

        val project = e.project ?: return

        val coeditPlugin = CoeditPlugin.getInstance(project)
        val messageBus = ApplicationManager.getApplication().messageBus.connect()
        messageBus.subscribe(FileEditorManagerListener.FILE_EDITOR_MANAGER, object : FileEditorManagerListener {

            override fun fileOpened(source: FileEditorManager, file: VirtualFile) {
                val relativePath = Utils.getRelativePath(file, project)

                if (coeditPlugin.lockHandler.stateOf(relativePath) == LockState.LOCKED_FOR_EDIT) {
                    return
                }
                Utils.registerListener(file, ChangeListener(project))
            }

            override fun fileClosed(source: FileEditorManager, file: VirtualFile) {
                Utils.unregisterListener(file, ChangeListener(project))
            }
        })
    }
}