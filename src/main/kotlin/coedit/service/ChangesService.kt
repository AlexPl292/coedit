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
        return when (change.changeType) {
            ChangeType.CREATE_FILE -> createFile(change.requestBody as CoRequestBodyFileCreation)
            ChangeType.EDIT_FILE -> editFile(change.requestBody as CoRequestBodyFileEdit)
        }
    }

    private fun createFile(change: CoRequestBodyFileCreation): CoResponse {
        val coeditPlugin = CoeditPlugin.getInstance(project)
        val parentPath = LocalFileSystem.getInstance().findFileByPath(coeditPlugin.myBasePath)
                ?: throw RuntimeException("Cannot access base directory")

        val newFile = parentPath.findOrCreateChildData(project, change.filePath)
        newFile.setBinaryContent(change.data)
        coeditPlugin.lockForEdit(change.filePath)
        return CoResponse.OK
    }

    private fun editFile(change: CoRequestBodyFileEdit): CoResponse {
        val coeditPlugin = CoeditPlugin.getInstance(project)
        val parentPath = LocalFileSystem.getInstance().findFileByPath(coeditPlugin.myBasePath)
                ?: throw RuntimeException("Cannot access base directory")

        val newFile = parentPath.findChild(change.filePath) ?: throw RuntimeException("Cannot read file")

        val document = FileDocumentManager.getInstance().getDocument(newFile)

        WriteCommandAction.runWriteCommandAction(project) { document?.insertString(0, "ABC") }

        return CoResponse.OK
    }
}