package coedit

import coedit.model.ChangeType
import coedit.model.CoChange
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

    val testFile = CoChange(ChangeType.CREATE_FILE, "Test", "TestData".toByteArray())

    Socket(host, port).use { echoSocket ->
        ObjectOutputStream(echoSocket.getOutputStream()).use { objectStream ->
            objectStream.writeObject(testFile)
        }
    }
}