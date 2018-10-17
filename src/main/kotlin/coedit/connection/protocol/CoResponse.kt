package coedit.connection.protocol

import java.io.Serializable

/**
 * Created by Alex Plate on 17.10.2018.
 */
data class CoResponse(
        val code: Int
) : Serializable {
    companion object {
        val OK = CoResponse(200)
    }
}
