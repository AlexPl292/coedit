package coedit.action

import coedit.CoeditPlugin
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.ui.Messages

/**
 * Created by Alex Plate on 17.10.2018.
 */
class ConnectToServer : SetUpConnectionAction("ConnectToServer") {
    override fun actionPerformed(e: AnActionEvent?) {
        val project = e?.project

        val connection = CoeditPlugin.getInstance(project!!).myConn
        connection.connectToServer(project)

        super.actionPerformed(e)

        Messages.showMessageDialog(project, "Connected to server", "Greeting", Messages.getInformationIcon())
    }
}