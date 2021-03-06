package coedit.action

import coedit.CoeditPlugin
import coedit.dialog.StartServerDialog
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent


/**
 * Created by Alex Plate on 17.10.2018.
 */
class StartServer : AnAction("StartServer") {
    override fun actionPerformed(e: AnActionEvent?) {
        val project = e?.project ?: throw RuntimeException("IntelliJ IDEA error. Cannot get action event")

        StartServerDialog(project).show()
    }

    override fun update(e: AnActionEvent?) {
        val project = e?.project ?: throw RuntimeException("IntelliJ IDEA error. Cannot get action event")

        val coeditPlugin = CoeditPlugin.getInstance(project)
        e.presentation.isEnabled = !coeditPlugin.editing.get() && !coeditPlugin.myConn.waitForConnection.get()
    }
}