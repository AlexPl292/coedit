package coedit.service

import coedit.CoeditPlugin
import coedit.Utils
import coedit.connection.protocol.*
import coedit.model.LockHandler
import coedit.model.LockState
import coedit.removeGuardedBlocks
import coedit.stopWork
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
        val response = when (change) {
            is CoRequestFileCreation -> createFile(change)
            is CoRequestFileEdit -> editFile(change)
            is CoRequestTryLock -> tryLock(change)
            is CoRequestUnlock -> unlock(change)
            is CoRequestStopCollaboration -> stopCollaboration(change)
            is CoRequestFileDeletion -> deleteFile(change)
            is CoRequestFileRename -> renameFile(change)
        }
        response.requestUuid = change.requestUuid
        val coeditPlugin = CoeditPlugin.getInstance(project)
        coeditPlugin.myConn.response(response)
    }

    private fun createFile(change: CoRequestFileCreation): CoResponse {
        val coeditPlugin = CoeditPlugin.getInstance(project)

        WriteCommandAction.runWriteCommandAction(project) {
            coeditPlugin.lockHandler.disableHandler(change.filePath)
            val file = File(coeditPlugin.myBasePath + File.separator + change.filePath)
            if (change.isDirectory) {
                file.mkdirs()
            } else {
                file.parentFile.mkdirs()
                file.createNewFile()
                LocalFileSystem.getInstance().refreshAndFindFileByIoFile(file)
            }
        }

        return CoResponse.OK
    }

    private fun deleteFile(change: CoRequestFileDeletion): CoResponse {
        val coeditPlugin = CoeditPlugin.getInstance(project)

        if (change.isDirectory && coeditPlugin.lockHandler.locksInDir(change.filePath, LockState.LOCKED_BY_ME) || !change.isDirectory && coeditPlugin.lockHandler.stateOf(change.filePath) == LockState.LOCKED_BY_ME) {
            return CoResponse.CANNOT_CHANGE_FILE_LOCKED(change.filePath)
        }
        WriteCommandAction.runWriteCommandAction(project) {
            coeditPlugin.lockHandler.disableHandler(change.filePath)
            LocalFileSystem.getInstance()
                    .findFileByPath(coeditPlugin.myBasePath + File.separator + change.filePath)
                    ?.delete(project)
        }
        return CoResponse.OK
    }

    private fun renameFile(change: CoRequestFileRename): CoResponse {
        val coeditPlugin = CoeditPlugin.getInstance(project)

        if (change.isDirectory && coeditPlugin.lockHandler.locksInDir(change.filePath, LockState.LOCKED_BY_ME) || !change.isDirectory && coeditPlugin.lockHandler.stateOf(change.filePath) == LockState.LOCKED_BY_ME) {
            return CoResponse.CANNOT_CHANGE_FILE_LOCKED(change.filePath)
        }

        WriteCommandAction.runWriteCommandAction(project) {
            coeditPlugin.lockHandler.disableHandler(change.filePath)
            LocalFileSystem.getInstance()
                    .findFileByPath(coeditPlugin.myBasePath + File.separator + change.filePath)
                    ?.rename(project, change.newName)
        }

        return CoResponse.OK
    }

    private fun editFile(change: CoRequestFileEdit): CoResponse {
        val coeditPlugin = CoeditPlugin.getInstance(project)
        val parentPath = LocalFileSystem.getInstance().findFileByPath(coeditPlugin.myBasePath)
                ?: return CoResponse.CANNOT_GET_FILE(coeditPlugin.myBasePath)

        val newFile = parentPath.findFileByRelativePath(change.filePath)
                ?: return CoResponse.CANNOT_GET_FILE(change.filePath)

        WriteCommandAction.runWriteCommandAction(project) {
            val document = FileDocumentManager.getInstance().getDocument(newFile)
            if (document != null && change.patch.offset + change.patch.oldLength <= document.textLength) {
                document.replaceString(change.patch.offset, change.patch.offset + change.patch.oldLength, change.patch.newString)
                document.removeGuardedBlocks()
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
        stopWork(project)
        return CoResponse.OK
    }
}