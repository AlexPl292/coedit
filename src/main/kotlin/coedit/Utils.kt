package coedit

import com.intellij.openapi.editor.Document
import com.intellij.openapi.editor.event.DocumentListener
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ProjectFileIndex
import com.intellij.openapi.vfs.VfsUtilCore
import com.intellij.openapi.vfs.VirtualFile

/**
 * Created by Alex Plate on 18.10.2018.
 */
class Utils {
    companion object {
        fun getRelativePath(document: Document, project: Project): String {
            val file = FileDocumentManager.getInstance().getFile(document)
                    ?: throw RuntimeException("Cannot access document")
            return getRelativePath(file, project)
        }

        fun getRelativePath(file: VirtualFile, project: Project): String {
            val root = ProjectFileIndex.getInstance(project).getContentRootForFile(file)
                    ?: throw RuntimeException("Cannot detect root of project")
            return VfsUtilCore.getRelativePath(file, root)
                    ?: throw RuntimeException("Cannot get relative path for file")
        }

        fun registerListener(virtualFile: VirtualFile, listener: DocumentListener) {
            try {
                FileDocumentManager.getInstance().getDocument(virtualFile)?.addDocumentListener(listener)
            } catch (e: Throwable) {
                // Nothing. This listener is already registered
            }
        }

        fun unregisterListener(virtualFile: VirtualFile, listener: DocumentListener) {
            try {
                FileDocumentManager.getInstance().getDocument(virtualFile)?.removeDocumentListener(listener)
            } catch (e: Throwable) {
                // Nothing. There is no such listener
            }
        }

        fun removeAllGuardedBlocks(document: Document) {
            while (true) {
                // Well, I have no idea, how can I get all guard blocks in another way
                val guard = document.getRangeGuard(0, document.textLength) ?: break
                document.removeGuardedBlock(guard)
            }
        }
    }
}