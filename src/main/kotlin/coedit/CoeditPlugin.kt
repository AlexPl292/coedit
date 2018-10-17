package coedit

import coedit.connection.CoeditConnection
import com.intellij.openapi.components.ProjectComponent
import com.intellij.openapi.project.Project

/**
 * Created by Alex Plate on 16.10.2018.
 */

class CoeditPlugin(private val myProject: Project) : ProjectComponent {

    private val myConn: CoeditConnection = CoeditConnection()

    companion object {
        fun getInstance(project: Project): CoeditPlugin {
            return project.getComponent(CoeditPlugin::class.java)
        }
    }

    override fun projectOpened() {
        myConn.startServer()
    }
}