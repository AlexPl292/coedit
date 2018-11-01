package coedit.connection.protocol

import java.io.Serializable

/**
 * Created by Alex Plate on 17.10.2018.
 */

sealed class CoRequest(open var requestUuid: String?) : Serializable

data class CoRequestFileCreation(
        val filePath: String,
        val isDirectory: Boolean,
        override var requestUuid: String? = null
) : CoRequest(requestUuid)

data class CoRequestFileDeletion(
        val filePath: String,
        val isDirectory: Boolean,
        override var requestUuid: String? = null
) : CoRequest(requestUuid)

data class CoRequestFileRename(
        val filePath: String,
        val newName: String,
        val isDirectory: Boolean,
        override var requestUuid: String? = null
) : CoRequest(requestUuid)

data class CoPatch(
        val offset: Int,
        val oldLength: Int,
        val newString: String
) : Serializable

data class CoRequestFileEdit(
        val filePath: String,
        val patch: CoPatch,
        override var requestUuid: String? = null
) : CoRequest(requestUuid)

data class CoRequestTryLock(
        val filePath: String,
        val contentHashCode: Int,
        override var requestUuid: String? = null
) : CoRequest(requestUuid)

data class CoRequestUnlock(
        val filePath: String,
        override var requestUuid: String? = null
) : CoRequest(requestUuid)

class CoRequestStopCollaboration(
        override var requestUuid: String? = null
) : CoRequest(requestUuid)
