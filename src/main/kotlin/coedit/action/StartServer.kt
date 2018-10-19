package coedit.action

import coedit.CoeditPlugin
import com.intellij.notification.Notification
import com.intellij.notification.NotificationType
import com.intellij.notification.Notifications
import com.intellij.openapi.actionSystem.AnActionEvent




/**
 * Created by Alex Plate on 17.10.2018.
 */
class StartServer : SetUpConnectionAction("StartServer") {
    override fun actionPerformed(e: AnActionEvent?) {
        if (e == null) {
            throw RuntimeException("IntelliJ IDEA error. Cannot get action event")
        }
        val project = e.project ?: return

        val coeditPlugin = CoeditPlugin.getInstance(project)
        val connection = coeditPlugin.myConn
        connection.startServer(project)

        super.actionPerformed(e)

        Notifications.Bus.notify(Notification("CoEdit", "CoEdit", "Waiting for connections", NotificationType.INFORMATION))
    }

    override fun update(e: AnActionEvent?) {
        val project = e?.project ?: return

        val coeditPlugin = CoeditPlugin.getInstance(project)
        e.presentation.isEnabled = !coeditPlugin.editing.get() && !coeditPlugin.myConn.waitForConnection.get()
    }
}