package coedit

import coedit.connection.CoeditConnection
import coedit.listener.ChangeListener
import coedit.listener.CoOperationsHandler
import coedit.model.LockHandler
import com.intellij.openapi.components.ProjectComponent
import com.intellij.openapi.editor.EditorFactory
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.LocalFileSystem
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Created by Alex Plate on 16.10.2018.
 */

class CoeditPlugin(private val myProject: Project) : ProjectComponent {

    val myConn: CoeditConnection = CoeditConnection()
    val myBasePath = myProject.basePath ?: throw RuntimeException("Cannot detect base path of project")
    val lockHandler = LockHandler(myProject, myBasePath)

    val editing: AtomicBoolean = AtomicBoolean(false)

    private val coOperationsHandler = CoOperationsHandler(myProject)

    // Very simple implementation of .ignore. Ignore files and dirs if path returns true on startsWith
    private val coIgnore = listOf(".idea")

    companion object {
        fun getInstance(project: Project): CoeditPlugin {
            return project.getComponent(CoeditPlugin::class.java)
        }
    }

    override fun projectOpened() {
    }

    fun isIgnored(relativePath: String): Boolean {
        return coIgnore.any { relativePath.startsWith(it) }
    }

    fun subscribeToMessageBus() {
        EditorFactory.getInstance().eventMulticaster.addDocumentListener(ChangeListener(myProject))
        LocalFileSystem.getInstance().registerAuxiliaryFileOperationsHandler(coOperationsHandler)
    }

    fun disconnectMessageBus() {
        EditorFactory.getInstance().eventMulticaster.removeDocumentListener(ChangeListener(myProject))
        LocalFileSystem.getInstance().unregisterAuxiliaryFileOperationsHandler(coOperationsHandler)
    }
}