package coedit.action

import coedit.CoeditPlugin
import coedit.connection.protocol.CoRequestUnlock
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent

/**
 * Created by Alex Plate on 18.10.2018.
 */

class StopEditingAction : AnAction("StopEditingAction") {
    override fun actionPerformed(e: AnActionEvent?) {
        val project = e?.project

        val coeditPlugin = CoeditPlugin.getInstance(project!!)
        val lockHandler = coeditPlugin.lockHandler

        val lockedByMe = lockHandler.lockedByMe()
        lockedByMe.forEach {
            lockHandler.unlock(it)
            coeditPlugin.myConn.send(CoRequestUnlock(it))
        }
    }
}