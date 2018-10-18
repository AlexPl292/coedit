package coedit.connection

import coedit.connection.protocol.CoRequest
import coedit.connection.protocol.CoResponse
import coedit.service.ChangesService
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.net.ServerSocket
import java.net.Socket
import java.util.concurrent.ArrayBlockingQueue
import java.util.concurrent.BlockingQueue
import java.util.concurrent.TimeUnit

/**
 * Created by Alex Plate on 16.10.2018.
 */

class CoeditConnection {

    private val log = Logger.getInstance(this.javaClass)

    private val myPort = 8089
    private val myHost = "localhost"
    private var myServerSocket: ServerSocket? = null
    private var myClientSocket: Socket? = null

    private var objectOutputStream: ObjectOutputStream? = null
    private var objectInputStream: ObjectInputStream? = null

    private val serverThread: Thread? = null

    private val responseQueue: BlockingQueue<CoResponse> = ArrayBlockingQueue(1)

    fun startServer(project: Project) {

        log.debug("Start server")
        myServerSocket = ServerSocket(myPort)

        val thread = Thread(Runnable {
            myClientSocket = myServerSocket?.accept()

            if (myClientSocket == null) {
                throw RuntimeException("Client socket is null")
            }
            objectInputStream = ObjectInputStream(myClientSocket?.getInputStream())
            objectOutputStream = ObjectOutputStream(myClientSocket?.getOutputStream())

            startReading(project)
        })
        thread.start()

        print("y")
    }

    fun connectToServer(project: Project) {
        val socket = Socket(myHost, myPort)

        objectOutputStream = ObjectOutputStream(socket.getOutputStream())
        objectInputStream = ObjectInputStream(socket.getInputStream())

        val thread = Thread(Runnable { startReading(project) })
        thread.start()

        print("x")
    }

    fun stopWork() {
        serverThread?.interrupt()
        objectOutputStream?.close()
        objectInputStream?.close()
    }

    private fun startReading(project: Project) {
        val changesService = ChangesService(project)
        myServerSocket.use { _ ->
            myClientSocket.use { _ ->
                objectInputStream.use { inStream ->
                    objectOutputStream.use { _ ->
                        while (true) {
                            if (Thread.interrupted()) break;
                            log.debug("Wait for incoming requests")
                            val request = inStream?.readObject()
                            log.debug("Got request. ", request)

                            if (Thread.interrupted()) break;

                            if (request is CoResponse) {
                                responseQueue.put(request)
                                continue
                            }
                            changesService.handleChange(request as CoRequest)
                        }
                    }
                }
            }
        }
    }

    fun send(request: CoRequest): CoResponse {
        log.debug("Sending object. ", request)
        objectOutputStream?.writeObject(request)

        // TODO handle ERROR response
        log.debug("Waiting for response...")
        val coResponse = responseQueue.poll(1, TimeUnit.SECONDS) ?: CoResponse.CONTINUE
        log.debug("Got response. ", coResponse)

        return coResponse
    }

    fun response(response: CoResponse) {
        log.debug("Response ", response)
        objectOutputStream?.writeObject(response)
    }

}