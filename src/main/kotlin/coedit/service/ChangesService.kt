package coedit.service

import coedit.CoeditPlugin
import coedit.model.ChangeType
import coedit.model.CoChangeProtocol
import coedit.model.CoRequestFileCreation
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.LocalFileSystem

/**
 * Created by Alex Plate on 17.10.2018.
 */

class ChangesService(private val project: Project) {
    fun handleChange(change: CoChangeProtocol) {
        when (change.changeType) {
            ChangeType.CREATE_FILE -> createFile(change.request as CoRequestFileCreation)
        }
    }

    private fun createFile(change: CoRequestFileCreation) {
        val parentPath = LocalFileSystem.getInstance().findFileByPath(CoeditPlugin.getInstance(project).myBasePath)
                ?: throw RuntimeException("Cannot access base directory")

        val newFile = parentPath.findOrCreateChildData(project, change.filePath)
        newFile.setBinaryContent(change.data)
    }
}