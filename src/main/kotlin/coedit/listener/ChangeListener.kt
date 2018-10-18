package coedit.listener

import coedit.CoeditPlugin
import coedit.Utils
import coedit.connection.protocol.CoPatch
import coedit.connection.protocol.CoRequestFileEdit
import coedit.connection.protocol.CoRequestTryLock
import coedit.connection.protocol.CoRequestUnlock
import coedit.model.LockState
import com.intellij.openapi.editor.event.DocumentEvent
import com.intellij.openapi.editor.event.DocumentListener
import com.intellij.openapi.project.Project

/**
 * Created by Alex Plate on 17.10.2018.
 */
class ChangeListener(private val project: Project) : DocumentListener, CoListener("ChangeListener") {

    override fun beforeDocumentChange(event: DocumentEvent?) {
        if (event == null) {
            throw RuntimeException("IntelliJ IDEA error. Cannot get document event")
        }

        val relativePath = Utils.getRelativePath(event.document, project)
        val coeditPlugin = CoeditPlugin.getInstance(project)

        val lockHandler = coeditPlugin.lockHandler
        if (lockHandler.stateOf(relativePath) == LockState.LOCKED_FOR_EDIT) {
            event.document.removeDocumentListener(this)
            return
        }
        if (lockHandler.stateOf(relativePath) != LockState.LOCKED_BY_ME) {
            val contentHashCode = event.document.text.hashCode()
            val lockedByMe = lockHandler.lockedByMe()
            lockedByMe.forEach {
                lockHandler.unlock(it)
                //TODO handle problems with unlock
                coeditPlugin.myConn.send(CoRequestUnlock(it))
            }

            //TODO handle problems with lock
            coeditPlugin.myConn.send(CoRequestTryLock(relativePath, contentHashCode))
            lockHandler.lockByMe(relativePath)
        }

        val patch = CoPatch(event.offset, event.oldLength, event.newFragment.toString())

        //TODO handle problems with changes
        CoeditPlugin.getInstance(project).myConn.send(CoRequestFileEdit(relativePath, patch))
    }
}