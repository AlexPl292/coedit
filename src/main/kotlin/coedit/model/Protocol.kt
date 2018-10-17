package coedit.model

import java.io.Serializable
import java.util.*

/**
 * Created by Alex Plate on 17.10.2018.
 */

enum class ChangeType {
    CREATE_FILE
}

interface CoRequest : Serializable

data class CoResponse(
        val code: Int
) : Serializable {
    companion object {
        val OK = CoResponse(200)
    }
}

data class CoChangeProtocol(
        val changeType: ChangeType,
        val request: CoRequest
) : Serializable

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

