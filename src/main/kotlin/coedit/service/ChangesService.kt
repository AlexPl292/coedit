package coedit.service

import coedit.CoeditPlugin
import coedit.Utils
import coedit.connection.protocol.*
import coedit.model.LockHandler
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.LocalFileSystem
import java.io.File

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
            is CoRequestStopCollaboration -> stopCollaboration(change)
            else -> CoResponse.ERROR
        }
        val coeditPlugin = CoeditPlugin.getInstance(project)
        coeditPlugin.myConn.response(response)
    }

    private fun createFile(change: CoRequestFileCreation): CoResponse {
        val coeditPlugin = CoeditPlugin.getInstance(project)

        val file = File(coeditPlugin.myBasePath + File.separator + change.filePath)
        file.parentFile.mkdirs()
        file.createNewFile()

        return CoResponse.OK
    }

    private fun editFile(change: CoRequestFileEdit): CoResponse {
        val coeditPlugin = CoeditPlugin.getInstance(project)
        val parentPath = LocalFileSystem.getInstance().findFileByPath(coeditPlugin.myBasePath)
                ?: return CoResponse.CANNOT_GET_FILE(coeditPlugin.myBasePath)

        val newFile = parentPath.findChild(change.filePath)
                ?: return CoResponse.CANNOT_GET_FILE(change.filePath)

        WriteCommandAction.runWriteCommandAction(project) {
            val document = FileDocumentManager.getInstance().getDocument(newFile)
            if (document != null && change.patch.offset + change.patch.oldLength <= document.textLength) {
                document.replaceString(change.patch.offset, change.patch.offset + change.patch.oldLength, change.patch.newString)
                Utils.removeAllGuardedBlocks(document)
                document.createGuardedBlock(0, document.textLength)
            }
        }

        return CoResponse.OK
    }

    private fun tryLock(change: CoRequestTryLock): CoResponse {
        val lockForEditStatus = CoeditPlugin.getInstance(project).lockHandler.lockForEdit(change.filePath)
        return when (lockForEditStatus) {
            LockHandler.Status.OK -> CoResponse.OK
            LockHandler.Status.ALREADY_LOCKED_FOR_EDIT -> CoResponse.LOCK_FILE_ALREADY_LOCKED
            LockHandler.Status.ALREADY_LOCKED_BY_ME -> CoResponse.CANNOT_LOCK_FILE_ALREADY_LOCKED
            LockHandler.Status.CANNOT_GET_FILE -> CoResponse.CANNOT_LOCK_FILE_MISSING
        }
    }

    private fun unlock(change: CoRequestUnlock): CoResponse {
        return if (CoeditPlugin.getInstance(project).lockHandler.unlock(change.filePath)) CoResponse.OK else CoResponse.CANNOT_UNLOCK_FILE
    }

    private fun stopCollaboration(change: CoRequestStopCollaboration): CoResponse {
        Utils.stopWork(project)
        return CoResponse.OK
    }
}