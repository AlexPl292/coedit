package coedit.action

import coedit.CoeditPlugin
import coedit.listener.ChangeListener
import coedit.model.LockState
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.fileEditor.FileEditorManagerListener
import com.intellij.openapi.roots.ProjectFileIndex
import com.intellij.openapi.vfs.VfsUtilCore
import com.intellij.openapi.vfs.VirtualFile

/**
 * Created by Alex Plate on 18.10.2018.
 */

abstract class SetUpConnectionAction(name: String) : AnAction(name) {
    override fun actionPerformed(e: AnActionEvent?) {
        val project = e?.project

        val coeditPlugin = CoeditPlugin.getInstance(project!!)
        val messageBus = ApplicationManager.getApplication().messageBus.connect()
        messageBus.subscribe(FileEditorManagerListener.FILE_EDITOR_MANAGER, object : FileEditorManagerListener {

            override fun fileOpened(source: FileEditorManager, file: VirtualFile) {
                var root = ProjectFileIndex.getInstance(project).getContentRootForFile(file)
                var relativePath = VfsUtilCore.getRelativePath(file, root!!)
                if (coeditPlugin.lockHandler.stateOf(relativePath!!) == LockState.LOCKED_FOR_EDIT) {
                    return
                }
                FileDocumentManager.getInstance().getDocument(file)?.addDocumentListener(ChangeListener(project))
            }

            override fun fileClosed(source: FileEditorManager, file: VirtualFile) {
                try {
                    FileDocumentManager.getInstance().getDocument(file)?.removeDocumentListener(ChangeListener(project))
                } catch (e: Throwable) {
                    // Nothing
                }
            }
        })
    }
}