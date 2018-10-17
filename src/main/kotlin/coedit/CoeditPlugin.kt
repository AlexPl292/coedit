package coedit

import coedit.connection.CoeditConnection
import coedit.model.LockState
import com.intellij.openapi.components.ProjectComponent
import com.intellij.openapi.project.Project

/**
 * Created by Alex Plate on 16.10.2018.
 */

class CoeditPlugin(private val myProject: Project) : ProjectComponent {

    private val myConn: CoeditConnection = CoeditConnection()
    val myBasePath = myProject.basePath ?: throw RuntimeException("Cannot detect base path of project")
    val locks: MutableMap<String, LockState> = HashMap()

    companion object {
        fun getInstance(project: Project): CoeditPlugin {
            return project.getComponent(CoeditPlugin::class.java)
        }
    }

    override fun projectOpened() {
        myConn.startServer(myProject)
    }

    fun lockByMe(file: String) {
        locks[file] = LockState.LOCKED_BY_ME
    }

    fun lockForEdit(file: String) {
//        LocalFileSystem.getInstance().findFileByPath(myBasePath)?.findChild(file)?.isWritable = false
        locks[file] = LockState.LOCKED_FOR_EDIT
    }
}