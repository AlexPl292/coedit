package coedit.action

import coedit.CoeditPlugin
import coedit.listener.InitListener
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.fileEditor.FileEditorManagerListener
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.vfs.VirtualFile


/**
 * Created by Alex Plate on 17.10.2018.
 */
class StartServer : AnAction("StartServer") {
    override fun actionPerformed(e: AnActionEvent?) {
        val project = e?.project

        val connection = CoeditPlugin.getInstance(project!!).myConn
        connection.startServer(project)

        val messageBus = ApplicationManager.getApplication().messageBus.connect()
        messageBus.subscribe(FileEditorManagerListener.FILE_EDITOR_MANAGER, object : FileEditorManagerListener {
            val listener = InitListener(project)

            override fun fileOpened(source: FileEditorManager, file: VirtualFile) {
                FileDocumentManager.getInstance().getDocument(file)?.addDocumentListener(listener)
            }

            override fun fileClosed(source: FileEditorManager, file: VirtualFile) {
                FileDocumentManager.getInstance().getDocument(file)?.removeDocumentListener(listener)
            }
        })

        Messages.showMessageDialog(project, "Server started", "Greeting", Messages.getInformationIcon())
    }
}