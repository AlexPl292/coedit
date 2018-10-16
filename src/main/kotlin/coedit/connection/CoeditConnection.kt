package coedit.connection

import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.ServerSocket
import java.net.Socket

/**
 * Created by Alex Plate on 16.10.2018.
 */

class CoeditConnection {

    private val port = 8089
    private var serverSocket: ServerSocket? = null
    private var clientSocket: Socket? = null


    fun startServer() {
        serverSocket = ServerSocket(port)
        clientSocket = serverSocket?.accept()

        if (clientSocket == null) {
            throw RuntimeException("Client socket is null")
        }
        val reader = BufferedReader(InputStreamReader(clientSocket?.getInputStream()))

        serverSocket.use { _ ->
            clientSocket.use { _ ->
                reader.use {
                    while (true) {
                        val line = it.readLine() ?: break
                        println(line)
                    }
                }
            }
        }
    }
}