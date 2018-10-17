package coedit.connection

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


    fun startServer() {
        Thread(Runnable {
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
                            println(inputLine)
                        }
                    }
                }
            }
        }).start()
    }
}