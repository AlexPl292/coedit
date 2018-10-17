package coedit.connection

import coedit.model.CoChangeProtocol
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
    private var myServerSocket: ServerSocket? = null
    private var myClientSocket: Socket? = null


    fun startServer(project: Project) {
        Thread(Runnable {
            val changesService = ChangesService(project)

            myServerSocket = ServerSocket(myPort)
            myClientSocket = myServerSocket?.accept()

            if (myClientSocket == null) {
                throw RuntimeException("Client socket is null")
            }
            val objectInputStream = ObjectInputStream(myClientSocket?.getInputStream())
            val objectOutputStream = ObjectOutputStream(myClientSocket?.getOutputStream())

            myServerSocket.use { _ ->
                myClientSocket.use { _ ->
                    objectInputStream.use { inStream ->
                        objectOutputStream.use { outStream ->
                            while (true) {
                                val request = inStream.readObject() as CoChangeProtocol
                                val coResponse = changesService.handleChange(request)
                                outStream.writeObject(coResponse)
                            }
                        }
                    }
                }
            }
        }).start()
    }
}