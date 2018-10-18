package coedit.action

import coedit.CoeditPlugin
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.ui.Messages

/**
 * Created by Alex Plate on 17.10.2018.
 */
class ConnectToServer : SetUpConnectionAction("ConnectToServer") {
    override fun actionPerformed(e: AnActionEvent?) {
        if (e == null) {
            throw RuntimeException("IntelliJ IDEA error. Cannot get action event")
        }

        val project = e.project ?: return

        val coeditPlugin = CoeditPlugin.getInstance(project)
        val connection = coeditPlugin.myConn
        connection.connectToServer(project)

        super.actionPerformed(e)

        Messages.showMessageDialog(project, "Connected to server", "Greeting", Messages.getInformationIcon())
    }
}