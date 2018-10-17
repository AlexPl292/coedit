package coedit

import coedit.connection.protocol.ChangeType
import coedit.connection.protocol.CoRequest
import coedit.connection.protocol.CoRequestBodyFileCreation
import coedit.connection.protocol.CoRequestBodyFileEdit
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.net.Socket
import java.util.*

/**
 * Created by Alex Plate on 17.10.2018.
 */

fun main(args: Array<String>) {
    createFile()
}

fun createFile() {
    val host = "localhost"
    val port = 8089

    val testFile = CoRequestBodyFileCreation("Test", "TestData".toByteArray())
    val changeFile = CoRequestBodyFileEdit("Test", ("TestData" + Random().nextInt(100)).toByteArray())

    Socket(host, port).use { echoSocket ->
        ObjectOutputStream(echoSocket.getOutputStream()).use { objectStream ->
            ObjectInputStream(echoSocket.getInputStream()).use { inStream ->
                objectStream.writeObject(CoRequest(ChangeType.CREATE_FILE, testFile))
                var readObject = inStream.readObject()
                println(readObject)
                // ----
                objectStream.writeObject(CoRequest(ChangeType.EDIT_FILE, changeFile))
            }
        }
    }
}