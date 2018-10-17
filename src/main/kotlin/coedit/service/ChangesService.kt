package coedit.service

import coedit.CoeditPlugin
import coedit.model.ChangeType
import coedit.model.CoChange
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.LocalFileSystem

/**
 * Created by Alex Plate on 17.10.2018.
 */

class ChangesService(private val project: Project) {
    fun handleChange(change: CoChange) {
        when (change.changeType) {
            ChangeType.CREATE_FILE -> createFile(change)
        }
    }

    private fun createFile(change: CoChange) {
        val parentPath = LocalFileSystem.getInstance().findFileByPath(CoeditPlugin.getInstance(project).myBasePath)
                ?: throw RuntimeException("Cannot access base directory")

        val newFile = parentPath.findOrCreateChildData(project, change.filePath)
        newFile.setBinaryContent(change.data)
    }
}