package coedit.action

import coedit.CoeditPlugin
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.ui.Messages


/**
 * Created by Alex Plate on 17.10.2018.
 */
class StartServer : SetUpConnectionAction("StartServer") {
    override fun actionPerformed(e: AnActionEvent?) {
        val project = e?.project

        val coeditPlugin = CoeditPlugin.getInstance(project!!)
        val connection = coeditPlugin.myConn
        connection.startServer(project)

        super.actionPerformed(e)

        Messages.showMessageDialog(project, "Server started", "Greeting", Messages.getInformationIcon())
    }
}