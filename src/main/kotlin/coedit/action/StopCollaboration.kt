package coedit.action

import coedit.CoeditPlugin
import coedit.Utils
import coedit.connection.protocol.CoRequestStopCollaboration
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.diagnostic.Logger

/**
 * Created by Alex Plate on 18.10.2018.
 */
class StopCollaboration : AnAction("StopCollaboration") {

    private val log = Logger.getInstance(this.javaClass)

    override fun actionPerformed(e: AnActionEvent?) {
        val project = e?.project ?: throw RuntimeException("IntelliJ IDEA error. Cannot get action event")

        val coeditPlugin = CoeditPlugin.getInstance(project)
        log.debug("Stop connection..")
        coeditPlugin.myConn.sendAndWaitForResponse(CoRequestStopCollaboration())
        Utils.stopWork(project)
    }

    override fun update(e: AnActionEvent?) {
        val project = e?.project ?: throw RuntimeException("IntelliJ IDEA error. Cannot get action event")

        val coeditPlugin = CoeditPlugin.getInstance(project)
        e.presentation.isEnabled = coeditPlugin.editing.get()
    }
}