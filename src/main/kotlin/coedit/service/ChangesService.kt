package coedit.service

import coedit.CoeditPlugin
import coedit.connection.protocol.*
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.LocalFileSystem

/**
 * Created by Alex Plate on 17.10.2018.
 */

class ChangesService(private val project: Project) {
    fun handleChange(change: CoRequest): CoResponse {
        // FIXME Not Open-closed principle
        return when (change) {
            is CoRequestFileCreation -> createFile(change)
            is CoRequestFileEdit -> editFile(change)
            is CoRequestTryLock -> tryLock(change)
            else -> CoResponse.ERROR
        }
    }

    private fun createFile(change: CoRequestFileCreation): CoResponse {
        val coeditPlugin = CoeditPlugin.getInstance(project)
        val parentPath = LocalFileSystem.getInstance().findFileByPath(coeditPlugin.myBasePath)
                ?: throw RuntimeException("Cannot access base directory")

        val newFile = parentPath.findOrCreateChildData(project, change.filePath)
        newFile.setBinaryContent(change.data)
        coeditPlugin.lockForEdit(change.filePath)
        return CoResponse.OK
    }

    private fun editFile(change: CoRequestFileEdit): CoResponse {
        val coeditPlugin = CoeditPlugin.getInstance(project)
        val parentPath = LocalFileSystem.getInstance().findFileByPath(coeditPlugin.myBasePath)
                ?: throw RuntimeException("Cannot access base directory")

        val newFile = parentPath.findChild(change.filePath) ?: throw RuntimeException("Cannot read file")

        WriteCommandAction.runWriteCommandAction(project) {
            val document = FileDocumentManager.getInstance().getDocument(newFile)
            document?.deleteString(change.patch.offset, change.patch.offset + change.patch.oldLength)
            document?.insertString(change.patch.offset, change.patch.newString)
        }

        coeditPlugin.myConn.send(CoResponse.OK)

        return CoResponse.OK
    }

    private fun tryLock(change: CoRequestTryLock): CoResponse {
        CoeditPlugin.getInstance(project).lockForEdit(change.filePath)
        return CoResponse.OK
    }
}