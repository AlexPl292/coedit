package coedit

import coedit.listener.ChangeListener
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.editor.Document
import com.intellij.openapi.editor.event.DocumentListener
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ProjectFileIndex
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.VfsUtilCore
import com.intellij.openapi.vfs.VirtualFile

/**
 * Created by Alex Plate on 18.10.2018.
 */
class Utils {

    companion object {

        private val log = Logger.getInstance("#coedit.utils")

        fun getRelativePath(document: Document, project: Project): String {
            val file = FileDocumentManager.getInstance().getFile(document)
                    ?: throw RuntimeException("Cannot access document")
            return getRelativePath(file, project)
        }

        fun getRelativePath(file: VirtualFile, project: Project): String {
            val root = ProjectFileIndex.getInstance(project).getContentRootForFile(file)
                    ?: throw RuntimeException("Cannot detect root of project for ${file.path}")
            return VfsUtilCore.getRelativePath(file, root)
                    ?: throw RuntimeException("Cannot get relative path for file")
        }


        fun registerListener(virtualFile: VirtualFile, listener: DocumentListener) {
            registerListener(FileDocumentManager.getInstance().getDocument(virtualFile), listener)
        }

        fun registerListener(document: Document?, listener: DocumentListener) {
            try {
                document?.addDocumentListener(listener)
            } catch (e: Throwable) {
                // Nothing. This listener is already registered
            }
        }

        fun unregisterListener(virtualFile: VirtualFile, listener: DocumentListener) {
            unregisterListener(FileDocumentManager.getInstance().getDocument(virtualFile), listener)
        }

        fun unregisterListener(document: Document?, listener: DocumentListener) {
            try {
                document?.removeDocumentListener(listener)
            } catch (e: Throwable) {
                // Nothing. There is no such listener
            }
        }

        fun removeAllGuardedBlocks(file: VirtualFile) {
            val document = FileDocumentManager.getInstance().getDocument(file) ?: return
            removeAllGuardedBlocks(document)
        }

        fun removeAllGuardedBlocks(document: Document) {
            while (true) {
                // Well, I have no idea, how can I get all guard blocks in another way
                val guard = document.getRangeGuard(0, document.textLength) ?: break
                document.removeGuardedBlock(guard)
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
                    Utils.unregisterListener(file, ChangeListener(project))
                    Utils.removeAllGuardedBlocks(file)
                }
            }
        }
    }
}