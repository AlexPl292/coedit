package coedit.listener

import coedit.CoeditPlugin
import coedit.Utils
import coedit.connection.protocol.CoRequestFileCreation
import coedit.connection.protocol.CoRequestFileDeletion
import coedit.connection.protocol.CoRequestFileRename
import coedit.connection.protocol.CoResponse
import coedit.model.LockState
import com.intellij.notification.Notification
import com.intellij.notification.NotificationType
import com.intellij.notification.Notifications
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.LocalFileOperationsHandler
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.util.ThrowableConsumer
import java.io.File
import java.io.IOException

/**
 * Created by Alex Plate on 23.10.2018.
 */
class CoOperationsHandler(val project: Project) : LocalFileOperationsHandler {
    override fun afterDone(invoker: ThrowableConsumer<LocalFileOperationsHandler, IOException>?) {

    }

    override fun createFile(dir: VirtualFile?, name: String?): Boolean {
        if (dir == null) {
            throw RuntimeException("IntelliJ IDEA error. Cannot get deleted file")
        }
        val relativePath = Utils.getRelativePath(dir.path, project) + name
        val coeditPlugin = CoeditPlugin.getInstance(project)
        if (coeditPlugin.lockHandler.handleDisabledAndReset(relativePath) || coeditPlugin.isIgnored(relativePath)) {
            return false
        }
        if (coeditPlugin.lockHandler.stateOf(relativePath) == null) {
            val response = coeditPlugin.myConn.sendAndWaitForResponse(CoRequestFileCreation(relativePath, false))
            if (response.code != CoResponse.OK.code) {
                Notifications.Bus.notify(Notification("CoEdit", "Error!", "Cannot create file. Response $response", NotificationType.ERROR))
                return true
            }
        }
        return false
    }

    override fun rename(file: VirtualFile?, newName: String?): Boolean {
        if (file == null || newName == null) {
            throw RuntimeException("IntelliJ IDEA error. Cannot get deleted file")
        }
        val relativePath = Utils.getRelativePath(file.path, project)
        val coeditPlugin = CoeditPlugin.getInstance(project)
        if (coeditPlugin.lockHandler.handleDisabledAndReset(relativePath) || coeditPlugin.isIgnored(relativePath)) {
            return false
        }
        val isDirectory = file.isDirectory

        coeditPlugin.lockHandler.lockByMe(relativePath)
        val response = coeditPlugin.myConn.sendAndWaitForResponse(CoRequestFileRename(relativePath, newName, isDirectory))

        if (response.code != CoResponse.OK.code) {
            Notifications.Bus.notify(Notification("CoEdit", "Error!", "Cannot rename file. Response $response", NotificationType.ERROR))
            coeditPlugin.lockHandler.unlock(relativePath)
            return true
        }
        coeditPlugin.lockHandler.unlock(relativePath)
        return false
    }

    override fun move(file: VirtualFile?, toDir: VirtualFile?): Boolean {
        return false
    }

    override fun copy(file: VirtualFile?, toDir: VirtualFile?, copyName: String?): File? {
        return null
    }

    override fun createDirectory(dir: VirtualFile?, name: String?): Boolean {
        if (dir == null) {
            throw RuntimeException("IntelliJ IDEA error. Cannot get deleted file")
        }
        val relativePath = Utils.getRelativePath(dir.path, project) + name

        val coeditPlugin = CoeditPlugin.getInstance(project)
        if (coeditPlugin.lockHandler.handleDisabledAndReset(relativePath) || coeditPlugin.isIgnored(relativePath)) {
            return false
        }
        if (coeditPlugin.lockHandler.stateOf(relativePath) == null) {
            val response = coeditPlugin.myConn.sendAndWaitForResponse(CoRequestFileCreation(relativePath, true))
            if (response.code != CoResponse.OK.code) {
                Notifications.Bus.notify(Notification("CoEdit", "Error!", "Cannot create directory. Response $response", NotificationType.ERROR))
                return true
            }
        }
        return false
    }

    override fun delete(file: VirtualFile?): Boolean {
        if (file == null) {
            throw RuntimeException("IntelliJ IDEA error. Cannot get deleted file")
        }
        val relativePath = Utils.getRelativePath(file.path, project)

        val coeditPlugin = CoeditPlugin.getInstance(project)
        if (coeditPlugin.lockHandler.handleDisabledAndReset(relativePath) || coeditPlugin.isIgnored(relativePath)) {

            return false
        }

        val isDirectory = file.isDirectory
        if (isDirectory) {
            for (lockedFile in coeditPlugin.lockHandler.allLockedFiles()) {
                if (coeditPlugin.lockHandler.locksInDir(lockedFile, LockState.LOCKED_FOR_EDIT)) {
                    return true
                }
            }
        } else {
            if (coeditPlugin.lockHandler.stateOf(relativePath) == LockState.LOCKED_FOR_EDIT) {
                return true
            }
        }

        coeditPlugin.lockHandler.lockByMe(relativePath)

        val response = coeditPlugin.myConn.sendAndWaitForResponse(CoRequestFileDeletion(relativePath, isDirectory))
        if (response.code != CoResponse.OK.code) {
            Notifications.Bus.notify(Notification("CoEdit", "Error!", "Cannot delete file. Response $response", NotificationType.ERROR))
            coeditPlugin.lockHandler.unlock(relativePath)
            return true
        }
        coeditPlugin.lockHandler.unlock(relativePath)
        return false
    }
}