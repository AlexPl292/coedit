package coedit.connection.protocol

import java.io.Serializable
import java.util.*

/**
 * Created by Alex Plate on 17.10.2018.
 */

enum class ChangeType {
    CREATE_FILE, EDIT_FILE
}

data class CoRequest(
        val changeType: ChangeType,
        val requestBody: CoRequestBody
) : Serializable

interface CoRequestBody : Serializable
data class CoRequestBodyFileCreation(
        val filePath: String,
        val data: ByteArray
) : CoRequestBody {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as CoRequestBodyFileCreation

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
        val newString: String
) : Serializable

data class CoRequestBodyFileEdit(
        val filePath: String,
        val patch: CoPatch
) : CoRequestBody
