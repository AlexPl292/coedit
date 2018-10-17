package coedit.connection.protocol

import java.io.Serializable
import java.util.*

/**
 * Created by Alex Plate on 17.10.2018.
 */

interface CoRequest : Serializable

data class CoRequestFileCreation(
        val filePath: String,
        val data: ByteArray
) : CoRequest {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as CoRequestFileCreation

        if (filePath != other.filePath) return false
        if (!Arrays.equals(data, other.data)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = filePath.hashCode()
        result = 31 * result + Arrays.hashCode(data)
        return result
    }
}

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
        val filePath: String
) : CoRequest
