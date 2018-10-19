package coedit.action

import coedit.CoeditPlugin
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent

/**
 * Created by Alex Plate on 17.10.2018.
 */
class ConnectToServer : AnAction("ConnectToServer") {
    override fun actionPerformed(e: AnActionEvent?) {
        if (e == null) {
            throw RuntimeException("IntelliJ IDEA error. Cannot get action event")
        }

        val project = e.project ?: return

        val coeditPlugin = CoeditPlugin.getInstance(project)
        val connection = coeditPlugin.myConn
        connection.connectToServer(project)

        coeditPlugin.subscribeToMessageBus()

    }

    override fun update(e: AnActionEvent?) {
        val project = e?.project ?: return

        val coeditPlugin = CoeditPlugin.getInstance(project)
        e.presentation.isEnabled = !coeditPlugin.myConn.waitForConnection.get() && !coeditPlugin.editing.get()
    }
}