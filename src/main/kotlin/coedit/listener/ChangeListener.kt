package coedit.listener

import coedit.CoeditPlugin
import coedit.connection.protocol.CoPatch
import coedit.connection.protocol.CoRequestFileEdit
import coedit.connection.protocol.CoRequestTryLock
import coedit.connection.protocol.CoRequestUnlock
import coedit.getRelativePath
import coedit.model.LockState
import com.intellij.openapi.editor.event.DocumentEvent
import com.intellij.openapi.editor.event.DocumentListener
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.project.Project
import com.intellij.testFramework.LightVirtualFile

/**
 * Created by Alex Plate on 17.10.2018.
 */
class ChangeListener(private val project: Project) : DocumentListener, CoListener("ChangeListener") {

    override fun beforeDocumentChange(event: DocumentEvent?) {
        if (event == null) {
            throw RuntimeException("IntelliJ IDEA error. Cannot get document event")
        }

        val relativePath = getRelativePath(event.document, project)
        val coeditPlugin = CoeditPlugin.getInstance(project)

        val lockHandler = coeditPlugin.lockHandler
        if (lockHandler.stateOf(relativePath) == LockState.LOCKED_FOR_EDIT
                || !coeditPlugin.editing.get()
                || FileDocumentManager.getInstance().getFile(event.document) is LightVirtualFile
                || coeditPlugin.isIgnored(relativePath)) {
            return
        }

        if (lockHandler.stateOf(relativePath) != LockState.LOCKED_BY_ME) {
            val contentHashCode = event.document.text.hashCode()
            val lockedByMe = lockHandler.lockedByMe()
            lockedByMe.forEach {
                lockHandler.unlock(it)
                coeditPlugin.myConn.send(CoRequestUnlock(it))
            }

            coeditPlugin.myConn.send(CoRequestTryLock(relativePath, contentHashCode))
            lockHandler.lockByMe(relativePath)
        }

        val patch = CoPatch(event.offset, event.oldLength, event.newFragment.toString())

        CoeditPlugin.getInstance(project).myConn.send(CoRequestFileEdit(relativePath, patch))
    }
}