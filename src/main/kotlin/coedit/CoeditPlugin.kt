package coedit

import coedit.connection.CoeditConnection
import coedit.model.LockState
import com.intellij.openapi.components.ProjectComponent
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.LocalFileSystem

/**
 * Created by Alex Plate on 16.10.2018.
 */

class CoeditPlugin(myProject: Project) : ProjectComponent {

    val myConn: CoeditConnection = CoeditConnection()
    val myBasePath = myProject.basePath ?: throw RuntimeException("Cannot detect base path of project")
    val locks: MutableMap<String, LockState> = HashMap()

    companion object {
        fun getInstance(project: Project): CoeditPlugin {
            return project.getComponent(CoeditPlugin::class.java)
        }
    }

    override fun projectOpened() {
/*        val connection = ApplicationManager.getApplication().messageBus.connect()
        connection.subscribe(FileEditorManagerListener.FILE_EDITOR_MANAGER, object : FileEditorManagerListener {
            val listener = InitListener()

            override fun fileOpened(source: FileEditorManager, file: VirtualFile) {
                FileDocumentManager.getInstance().getDocument(file)?.addDocumentListener(listener)
            }

            override fun fileClosed(source: FileEditorManager, file: VirtualFile) {
                FileDocumentManager.getInstance().getDocument(file)?.removeDocumentListener(listener)
                FileDocumentManager.getInstance().getDocument(file)?.removeDocumentListener(ChangeListener())
            }
        })*/
    }

    fun lockByMe(file: String) {
        locks[file] = LockState.LOCKED_BY_ME
    }

    fun lockForEdit(file: String) {
        val vFile = LocalFileSystem.getInstance().findFileByPath(myBasePath)?.findChild(file)
        val document = FileDocumentManager.getInstance().getDocument(vFile!!)
        document?.createGuardedBlock(0, document.textLength)

        locks[file] = LockState.LOCKED_FOR_EDIT
    }
}