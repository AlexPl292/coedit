package coedit.connection

import coedit.connection.protocol.CoRequest
import coedit.service.ChangesService
import com.intellij.openapi.project.Project
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.net.ServerSocket
import java.net.Socket

/**
 * Created by Alex Plate on 16.10.2018.
 */

class CoeditConnection {

    private val myPort = 8089
    private val myHost = "localhost"
    private var myServerSocket: ServerSocket? = null
    private var myClientSocket: Socket? = null

    private var objectOutputStream: ObjectOutputStream? = null
    private var objectInputStream: ObjectInputStream? = null

    fun startServer(project: Project) {

        myServerSocket = ServerSocket(myPort)

        Thread(Runnable {
            myClientSocket = myServerSocket?.accept()

            if (myClientSocket == null) {
                throw RuntimeException("Client socket is null")
            }
            objectInputStream = ObjectInputStream(myClientSocket?.getInputStream())
            objectOutputStream = ObjectOutputStream(myClientSocket?.getOutputStream())

            startReading(project)
        }).start()
    }

    fun connectToServer(project: Project) {
        val socket = Socket(myHost, myPort)

        objectOutputStream = ObjectOutputStream(socket.getOutputStream())
        objectInputStream = ObjectInputStream(socket.getInputStream())

        Thread(Runnable { startReading(project) }).start()
    }

    fun startReading(project: Project) {
        val changesService = ChangesService(project)
        myServerSocket.use { _ ->
            myClientSocket.use { _ ->
                objectInputStream.use { inStream ->
                    objectOutputStream.use { outStream ->
                        while (true) {
                            val request = inStream?.readObject() as CoRequest
                            changesService.handleChange(request)
                        }
                    }
                }
            }
        }
    }

    fun send(request: Any) {
        objectOutputStream?.writeObject(request)
    }
}