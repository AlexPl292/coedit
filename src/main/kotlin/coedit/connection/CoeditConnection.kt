package coedit.connection

import coedit.model.CoChange
import coedit.service.ChangesService
import com.intellij.openapi.project.Project
import java.io.ObjectInputStream
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

            myServerSocket.use { _ ->
                myClientSocket.use { _ ->
                    objectInputStream.use {
                        while (true) {
                            val change: CoChange = objectInputStream.readObject() as CoChange
                            changesService.handleChange(change)
                        }
                    }
                }
            }
        }).start()
    }
}