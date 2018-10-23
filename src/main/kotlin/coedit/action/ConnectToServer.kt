package coedit.action

import coedit.CoeditPlugin
import coedit.dialog.ConnectToServerDialog
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent

/**
 * Created by Alex Plate on 17.10.2018.
 */
class ConnectToServer : AnAction("ConnectToServer") {
    override fun actionPerformed(e: AnActionEvent?) {
        val project = e?.project ?: throw RuntimeException("IntelliJ IDEA error. Cannot get action event")

        ConnectToServerDialog(project).show()
    }

    override fun update(e: AnActionEvent?) {
        val project = e?.project ?: throw RuntimeException("IntelliJ IDEA error. Cannot get action event")

        val coeditPlugin = CoeditPlugin.getInstance(project)
        e.presentation.isEnabled = !coeditPlugin.myConn.waitForConnection.get() && !coeditPlugin.editing.get()
    }
}