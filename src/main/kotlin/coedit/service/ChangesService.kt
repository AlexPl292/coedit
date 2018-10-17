package coedit.service

import coedit.CoeditPlugin
import coedit.connection.protocol.CoRequest
import coedit.connection.protocol.CoRequestFileCreation
import coedit.connection.protocol.CoRequestFileEdit
import coedit.connection.protocol.CoResponse
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

        val document = FileDocumentManager.getInstance().getDocument(newFile)

        WriteCommandAction.runWriteCommandAction(project) {
            document?.insertString(change.patch.offset, change.patch.newString)
        }

        return CoResponse.OK
    }
}