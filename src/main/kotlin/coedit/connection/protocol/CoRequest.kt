package coedit.connection.protocol

import java.io.Serializable

/**
 * Created by Alex Plate on 17.10.2018.
 */

interface CoRequest : Serializable

data class CoRequestFileCreation(
        val filePath: String,
        val isDirectory: Boolean
) : CoRequest

data class CoRequestFileDeletion(
        val filePath: String,
        val isDirectory: Boolean
) : CoRequest

data class CoRequestFileRename(
        val filePath: String,
        val newName: String,
        val isDirectory: Boolean
) : CoRequest

data class CoPatch(
        val offset: Int,
        val oldLength: Int,
        val newString: String
) : Serializable

data class CoRequestFileEdit(
        val filePath: String,
        val patch: CoPatch
) : CoRequest

data class CoRequestTryLock(
        val filePath: String,
        val contentHashCode: Int
) : CoRequest

data class CoRequestUnlock(
        val filePath: String
) : CoRequest

class CoRequestStopCollaboration : CoRequest
