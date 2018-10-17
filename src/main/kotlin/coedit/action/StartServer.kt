package coedit.action

import coedit.CoeditPlugin
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.ui.Messages


/**
 * Created by Alex Plate on 17.10.2018.
 */
class StartServer : AnAction("StartServer") {
    override fun actionPerformed(e: AnActionEvent?) {
        val project = e?.project

        val connection = CoeditPlugin.getInstance(project!!).myConn
        connection.startServer(project)

        Messages.showMessageDialog(project, "Server started", "Greeting", Messages.getInformationIcon())
    }
}