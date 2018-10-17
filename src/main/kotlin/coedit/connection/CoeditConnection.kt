package coedit.connection

import coedit.model.ChangeType
import coedit.model.CoChange
import coedit.service.ChangesService
import com.intellij.openapi.project.Project
import java.io.BufferedReader
import java.io.InputStreamReader
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

            val testData = CoChange(ChangeType.CREATE_FILE, "TestTest", "ThisIsATEst".toByteArray())

            myServerSocket = ServerSocket(myPort)
            myClientSocket = myServerSocket?.accept()

            if (myClientSocket == null) {
                throw RuntimeException("Client socket is null")
            }
            val reader = BufferedReader(InputStreamReader(myClientSocket?.getInputStream()))

            myServerSocket.use { _ ->
                myClientSocket.use { _ ->
                    reader.use {
                        var inputLine: String
                        while (true) {
                            inputLine = it.readLine()
                            if (inputLine == null) {
                                break;
                            }
                            changesService.handleChange(testData)
                            println(inputLine)
                        }
                    }
                }
            }
        }).start()
    }
}