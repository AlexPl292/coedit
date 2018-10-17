package coedit.model

import java.util.*

/**
 * Created by Alex Plate on 17.10.2018.
 */

enum class ChangeType {
    CREATE_FILE
}

data class CoChange(
        val changeType: ChangeType,
        val filePath: String,
        val data: ByteArray
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as CoChange

        if (changeType != other.changeType) return false
        if (filePath != other.filePath) return false
        if (!Arrays.equals(data, other.data)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = changeType.hashCode()
        result = 31 * result + filePath.hashCode()
        result = 31 * result + Arrays.hashCode(data)
        return result
    }
}
