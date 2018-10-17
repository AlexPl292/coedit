package coedit.service

import coedit.CoeditPlugin
import coedit.model.ChangeType
import coedit.model.CoChangeProtocol
import coedit.model.CoRequestFileCreation
import coedit.model.CoResponse
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.LocalFileSystem

/**
 * Created by Alex Plate on 17.10.2018.
 */

class ChangesService(private val project: Project) {
    fun handleChange(change: CoChangeProtocol): CoResponse {
        return when (change.changeType) {
            ChangeType.CREATE_FILE -> createFile(change.request as CoRequestFileCreation)
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
}