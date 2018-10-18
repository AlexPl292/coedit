package coedit.action

import coedit.CoeditPlugin
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent

/**
 * Created by Alex Plate on 18.10.2018.
 */

abstract class SetUpConnectionAction(name: String) : AnAction(name) {
    override fun actionPerformed(e: AnActionEvent?) {
        if (e == null) {
            throw RuntimeException("IntelliJ IDEA error. Cannot get action event")
        }

        val project = e.project ?: return

        val coeditPlugin = CoeditPlugin.getInstance(project)
        coeditPlugin.subscribeToMessageBus()
    }
}