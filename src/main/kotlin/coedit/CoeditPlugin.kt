package coedit

import coedit.connection.CoeditConnection
import coedit.listener.ChangeListener
import coedit.model.LockState
import com.intellij.openapi.components.ProjectComponent
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.LocalFileSystem

/**
 * Created by Alex Plate on 16.10.2018.
 */

class CoeditPlugin(private val myProject: Project) : ProjectComponent {

    val myConn: CoeditConnection = CoeditConnection()
    val myBasePath = myProject.basePath ?: throw RuntimeException("Cannot detect base path of project")
    val locks: MutableMap<String, LockState> = HashMap()

    companion object {
        fun getInstance(project: Project): CoeditPlugin {
            return project.getComponent(CoeditPlugin::class.java)
        }
    }

    override fun projectOpened() {
    }

    fun lockByMe(file: String) {
        locks[file] = LockState.LOCKED_BY_ME
    }

    fun lockForEdit(filePath: String) {
        val file = LocalFileSystem.getInstance().findFileByPath(myBasePath)?.findChild(filePath)
        val document = FileDocumentManager.getInstance().getDocument(file!!)
        document?.removeDocumentListener(ChangeListener(myProject))
        document?.createGuardedBlock(0, document.textLength)
        locks[filePath] = LockState.LOCKED_FOR_EDIT
    }
}