package coedit.model

import coedit.listener.ChangeListener
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.LocalFileSystem

/**
 * Created by Alex Plate on 17.10.2018.
 */

enum class LockState {
    LOCKED_FOR_EDIT, LOCKED_BY_ME
}

class LockHandler(val project: Project, val basePath: String) {
    private val locks: MutableMap<String, LockState>

    init {
        locks = HashMap()
    }

    fun lockByMe(file: String) {
        locks[file] = LockState.LOCKED_BY_ME
    }

    fun lockForEdit(filePath: String) {
        val file = LocalFileSystem.getInstance().findFileByPath(basePath)?.findChild(filePath)
        val document = FileDocumentManager.getInstance().getDocument(file!!)
        document?.removeDocumentListener(ChangeListener(project))
        document?.createGuardedBlock(0, document.textLength)
        locks[filePath] = LockState.LOCKED_FOR_EDIT
    }

    fun stateOf(filePath: String): LockState? {
        return locks[filePath]
    }

    fun unlock(filePath: String) {
        val status = locks.remove(filePath)
        val file = LocalFileSystem.getInstance().findFileByPath(basePath)?.findChild(filePath)
        val document = FileDocumentManager.getInstance().getDocument(file!!)

        if (status == LockState.LOCKED_FOR_EDIT) {
            while (true) {
                // Well, I have no idea, how can I get all guard blocks in another way
                val guard = document?.getRangeGuard(0, document.textLength) ?: break
                document.removeGuardedBlock(guard)
            }
        }
        if (status != LockState.LOCKED_BY_ME) {
            document?.addDocumentListener(ChangeListener(project))
        }
    }

    fun lockedByMe(): Set<String> {
        return locks.filterValues { it == LockState.LOCKED_BY_ME }.keys
    }
}
