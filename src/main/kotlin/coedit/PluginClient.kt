package coedit

import coedit.connection.protocol.*
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.net.Socket

/**
 * Created by Alex Plate on 17.10.2018.
 */

fun main(args: Array<String>) {
    changeFile()
}

fun createFile() {
    val host = "localhost"
    val port = 8089

    val testFile = CoRequestBodyFileCreation("Test.java", "TestData".toByteArray())

    Socket(host, port).use { echoSocket ->
        ObjectOutputStream(echoSocket.getOutputStream()).use { objectStream ->
            ObjectInputStream(echoSocket.getInputStream()).use { inStream ->
                objectStream.writeObject(CoRequest(ChangeType.CREATE_FILE, testFile))
                var readObject = inStream.readObject()
                println(readObject)
            }
        }
    }
}

fun changeFile() {
    val host = "localhost"
    val port = 8089

    val changeFile = CoRequestBodyFileEdit("Test.java", CoPatch(5, "XXX"))

    Socket(host, port).use { echoSocket ->
        ObjectOutputStream(echoSocket.getOutputStream()).use { objectStream ->
            ObjectInputStream(echoSocket.getInputStream()).use { inStream ->
                objectStream.writeObject(CoRequest(ChangeType.EDIT_FILE, changeFile))
            }
        }
    }
}
