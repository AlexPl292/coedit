package coedit

import coedit.model.ChangeType
import coedit.model.CoChangeProtocol
import coedit.model.CoRequestFileCreation
import java.io.ObjectOutputStream
import java.net.Socket

/**
 * Created by Alex Plate on 17.10.2018.
 */

fun main(args: Array<String>) {
    createFile()
}


fun createFile() {
    val host = "localhost"
    val port = 8089

    val testFile = CoRequestFileCreation("Test", "TestData".toByteArray())

    Socket(host, port).use { echoSocket ->
        ObjectOutputStream(echoSocket.getOutputStream()).use { objectStream ->
            objectStream.writeObject(CoChangeProtocol(ChangeType.CREATE_FILE, testFile))
        }
    }
}