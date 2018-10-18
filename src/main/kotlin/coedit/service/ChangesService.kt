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
    fun handleChange(change: CoRequest) {
        // FIXME Not Open-closed principle
        val response = when (change) {
            is CoRequestFileCreation -> createFile(change)
            is CoRequestFileEdit -> editFile(change)
            is CoRequestTryLock -> tryLock(change)
            is CoRequestUnlock -> unlock(change)
            else -> CoResponse.ERROR
        }
        val coeditPlugin = CoeditPlugin.getInstance(project)
        coeditPlugin.myConn.response(response)
    }

    private fun createFile(change: CoRequestFileCreation): CoResponse {
        val coeditPlugin = CoeditPlugin.getInstance(project)
        val parentPath = LocalFileSystem.getInstance().findFileByPath(coeditPlugin.myBasePath)
                ?: throw RuntimeException("Cannot access base directory")

        val newFile = parentPath.findOrCreateChildData(project, change.filePath)
        newFile.setBinaryContent(change.data)
        coeditPlugin.lockHandler.lockForEdit(change.filePath)
        return CoResponse.OK
    }

    private fun editFile(change: CoRequestFileEdit): CoResponse {
        val coeditPlugin = CoeditPlugin.getInstance(project)
        val parentPath = LocalFileSystem.getInstance().findFileByPath(coeditPlugin.myBasePath)
                ?: throw RuntimeException("Cannot access base directory")

        val newFile = parentPath.findChild(change.filePath) ?: throw RuntimeException("Cannot read file")

        WriteCommandAction.runWriteCommandAction(project) {
            val document = FileDocumentManager.getInstance().getDocument(newFile)
            document?.replaceString(change.patch.offset, change.patch.offset + change.patch.oldLength, change.patch.newString)
            document?.createGuardedBlock(0, document.textLength)
        }

        return CoResponse.OK
    }

    private fun tryLock(change: CoRequestTryLock): CoResponse {
        CoeditPlugin.getInstance(project).lockHandler.lockForEdit(change.filePath)

        return CoResponse.OK
    }

    private fun unlock(change: CoRequestUnlock): CoResponse {
        CoeditPlugin.getInstance(project).lockHandler.unlock(change.filePath)

        return CoResponse.OK
    }
}