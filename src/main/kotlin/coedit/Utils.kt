package coedit

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.editor.Document
import com.intellij.openapi.editor.impl.DocumentImpl
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.VirtualFile
import java.io.File

/**
 * Created by Alex Plate on 18.10.2018.
 */
class Utils {

    companion object {

        private val log = Logger.getInstance("#coedit.utils")

        fun getRelativePath(document: Document, project: Project): String {
            val file = FileDocumentManager.getInstance().getFile(document)
                    ?: throw RuntimeException("Cannot access document $document")
            return getRelativePath(file.path, project)
        }

        fun getRelativePath(filePath: String, project: Project): String {
            return File(project.basePath).toURI().relativize(File(filePath).toURI()).path
        }

        fun removeAllGuardedBlocks(file: VirtualFile) {
            val document = FileDocumentManager.getInstance().getDocument(file) ?: return
            removeAllGuardedBlocks(document)
        }

        fun removeAllGuardedBlocks(document: Document) {
            ApplicationManager.getApplication().runReadAction {
                if (document is DocumentImpl) {
                    document.guardedBlocks.forEach { document.removeGuardedBlock(it) }
                }
            }
        }

        fun stopWork(project: Project) {
            val coeditPlugin = CoeditPlugin.getInstance(project)
            coeditPlugin.editing.set(false)
            coeditPlugin.myConn.stopWork()

            log.debug("Disconnect message bus..")
            coeditPlugin.disconnectMessageBus()

            log.debug("Unlock files...")
            coeditPlugin.lockHandler.allLockedFiles().forEach {
                val file = LocalFileSystem.getInstance().findFileByPath(coeditPlugin.myBasePath)?.findChild(it)
                if (file != null) {
                    Utils.removeAllGuardedBlocks(file)
                }
            }
        }
    }
}