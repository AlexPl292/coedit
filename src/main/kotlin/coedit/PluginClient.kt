package coedit

import coedit.connection.protocol.CoPatch
import coedit.connection.protocol.CoRequestFileCreation
import coedit.connection.protocol.CoRequestFileEdit
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

    val testFile = CoRequestFileCreation("Test.java", "TestData".toByteArray())

    Socket(host, port).use { echoSocket ->
        ObjectOutputStream(echoSocket.getOutputStream()).use { objectStream ->
            ObjectInputStream(echoSocket.getInputStream()).use { inStream ->
                objectStream.writeObject(testFile)
                val readObject = inStream.readObject()
                println(readObject)
            }
        }
    }
}

fun changeFile() {
    val host = "localhost"
    val port = 8089

    val changeFile = CoRequestFileEdit("Test.java", CoPatch(5, "DDD"))

    Socket(host, port).use { echoSocket ->
        ObjectOutputStream(echoSocket.getOutputStream()).use { objectStream ->
            ObjectInputStream(echoSocket.getInputStream()).use { inStream ->
                objectStream.writeObject(changeFile)
            }
        }
    }
}
