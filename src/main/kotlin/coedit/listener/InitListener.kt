package coedit.listener

import coedit.CoeditPlugin
import coedit.connection.protocol.CoRequestTryLock
import com.intellij.openapi.editor.event.DocumentEvent
import com.intellij.openapi.editor.event.DocumentListener
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ProjectFileIndex
import com.intellij.openapi.vfs.VfsUtilCore

/**
 * Created by Alex Plate on 17.10.2018.
 */
class InitListener(val project: Project) : DocumentListener, CoListener {
    override val name: String
        get() = "InitListener"

    override fun beforeDocumentChange(event: DocumentEvent?) {
        var file = FileDocumentManager.getInstance().getFile(event?.document!!)
        var root = ProjectFileIndex.getInstance(project).getContentRootForFile(file!!)
        var relativePath = VfsUtilCore.getRelativePath(file, root!!)

        CoeditPlugin.getInstance(project).myConn.send(CoRequestTryLock(relativePath!!))
    }
}