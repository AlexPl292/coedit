package coedit.model

import coedit.Utils
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

    /**
     * Lock file for editing from this side of plugin
     *
     * File will **not** be locked if it's already locked for edit
     */
    fun lockByMe(file: String): Status {
        // TODO handle return types
        if (file in locks) {
            if (locks[file] == LockState.LOCKED_BY_ME) {
                return Status.ALREADY_LOCKED_BY_ME
            }
            if (locks[file] == LockState.LOCKED_FOR_EDIT) {
                return Status.ALREADY_LOCKED_FOR_EDIT
            }
        }
        locks[file] = LockState.LOCKED_BY_ME
        return Status.OK
    }

    /**
     * Lock file for edit
     *
     * File will be **not** locked if it's already locked by me
     */
    fun lockForEdit(filePath: String): Status {
        val file = LocalFileSystem.getInstance().findFileByPath(basePath)?.findChild(filePath)
                ?: return Status.CANNOT_GET_FILE
        val document = FileDocumentManager.getInstance().getDocument(file)
                ?: return Status.CANNOT_GET_FILE
        Utils.unregisterListener(document, ChangeListener(project))
        document.createGuardedBlock(0, document.textLength)

        if (filePath in locks) {
            if (locks[filePath] == LockState.LOCKED_FOR_EDIT) {
                return Status.ALREADY_LOCKED_FOR_EDIT
            }
            if (locks[filePath] == LockState.LOCKED_BY_ME) {
                return Status.ALREADY_LOCKED_BY_ME
            }
        }
        return Status.OK
    }

    fun stateOf(filePath: String): LockState? {
        return locks[filePath]
    }

    fun unlock(filePath: String): Boolean {
        val status = locks.remove(filePath)
        val file = LocalFileSystem.getInstance().findFileByPath(basePath)?.findChild(filePath)
                ?: return false
        val document = FileDocumentManager.getInstance().getDocument(file)
                ?: return false

        if (status == LockState.LOCKED_FOR_EDIT) {
            Utils.removeAllGuardedBlocks(document)
        }
        if (status != LockState.LOCKED_BY_ME) {
            Utils.registerListener(document, ChangeListener(project))
        }
        return true
    }

    fun lockedByMe(): Set<String> {
        return locks.filterValues { it == LockState.LOCKED_BY_ME }.keys
    }

    enum class Status {
        OK, ALREADY_LOCKED_BY_ME, ALREADY_LOCKED_FOR_EDIT, CANNOT_GET_FILE
    }
}
