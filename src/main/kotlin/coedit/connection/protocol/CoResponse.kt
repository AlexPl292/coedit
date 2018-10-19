package coedit.connection.protocol

import java.io.Serializable

/**
 * Created by Alex Plate on 17.10.2018.
 */
data class CoResponse(
        val code: Int,
        val message: String
) : Serializable {
    companion object {

        // Special response. Should be used on client side and means that we've waited for response to long and should
        // continue work
        val CONTINUE = CoResponse(0, "")

        // This codes have nothing common with HTTP codes
        val OK = CoResponse(200, "OK")
        val LOCK_FILE_ALREADY_LOCKED = CoResponse(201, "File already locked")

        val ERROR = CoResponse(500, "ERROR")

        fun CANNOT_GET_FILE(path: String): CoResponse {
            return CoResponse(501, "Cannot get file. Path: $path")
        }

        val CANNOT_LOCK_FILE_ALREADY_LOCKED = CoResponse(502, "Cannot lock file. This file is already locked")
        val CANNOT_LOCK_FILE_MISSING = CoResponse(503, "Cannot lock file. File missing")
        val CANNOT_UNLOCK_FILE = CoResponse(503, "Cannot lock file")

        fun CANNOT_DELETE_FILE_LOCKED(path: String): CoResponse {
            return CoResponse(504, "Cannot delete file. Path: $path")
        }
    }
}
