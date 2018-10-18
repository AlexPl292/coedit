package coedit.listener

import coedit.CoeditPlugin
import coedit.connection.protocol.CoPatch
import coedit.connection.protocol.CoRequestFileEdit
import coedit.connection.protocol.CoRequestTryLock
import coedit.model.LockState
import com.intellij.openapi.editor.event.DocumentEvent
import com.intellij.openapi.editor.event.DocumentListener
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ProjectFileIndex
import com.intellij.openapi.vfs.VfsUtilCore

/**
 * Created by Alex Plate on 17.10.2018.
 */
class ChangeListener(private val project: Project) : DocumentListener, CoListener("ChangeListener") {

    override fun beforeDocumentChange(event: DocumentEvent?) {
        var file = FileDocumentManager.getInstance().getFile(event?.document!!)
        var root = ProjectFileIndex.getInstance(project).getContentRootForFile(file!!)
        var relativePath = VfsUtilCore.getRelativePath(file, root!!)
        val coeditPlugin = CoeditPlugin.getInstance(project)

        if (coeditPlugin.locks[relativePath] == LockState.LOCKED_FOR_EDIT) {
            event.document.removeDocumentListener(this)
            return
        }
        if (coeditPlugin.locks[relativePath] != LockState.LOCKED_BY_ME) {
            val contentHashCode = event.document.text.hashCode()

            val response = coeditPlugin.myConn.send(CoRequestTryLock(relativePath!!, contentHashCode))
            coeditPlugin.lockByMe(relativePath)
        }

        val patch = CoPatch(event.offset, event.oldLength, event.newFragment.toString())
        CoeditPlugin.getInstance(project).myConn.send(CoRequestFileEdit(relativePath!!, patch))
    }
}