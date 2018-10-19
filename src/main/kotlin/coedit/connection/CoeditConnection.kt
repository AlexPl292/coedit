package coedit.connection

import coedit.CoeditPlugin
import coedit.connection.protocol.CoRequest
import coedit.connection.protocol.CoResponse
import coedit.service.ChangesService
import com.intellij.notification.Notification
import com.intellij.notification.NotificationType
import com.intellij.notification.Notifications
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.net.ConnectException
import java.net.ServerSocket
import java.net.Socket
import java.util.concurrent.ArrayBlockingQueue
import java.util.concurrent.BlockingQueue
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Created by Alex Plate on 16.10.2018.
 */

class CoeditConnection {

    private val log = Logger.getInstance(this.javaClass)

    var myPort = 8089
    var myHost = "localhost"
    private var myServerSocket: ServerSocket? = null
    private var myClientSocket: Socket? = null

    private var objectOutputStream: ObjectOutputStream? = null
    private var objectInputStream: ObjectInputStream? = null

    private var serverThread: Thread? = null

    private val responseQueue: BlockingQueue<CoResponse> = ArrayBlockingQueue(1)

    var waitForConnection: AtomicBoolean = AtomicBoolean(false)

    fun startServer(project: Project) {
        val coeditPlugin = CoeditPlugin.getInstance(project)
        if (coeditPlugin.editing.get()) {
            return
        }

        waitForConnection.set(true)
        log.debug("Start server")
        myServerSocket = ServerSocket(myPort)

        serverThread = Thread(Runnable {
            Notifications.Bus.notify(Notification("CoEdit", "CoEdit", "Waiting for connections", NotificationType.INFORMATION))
            myClientSocket = myServerSocket?.accept()
            waitForConnection.set(false)
            val coeditPlugin1 = CoeditPlugin.getInstance(project)
            coeditPlugin1.editing.set(true)
            coeditPlugin1.lockHandler.clear()
            Notifications.Bus.notify(Notification("CoEdit", "CoEdit", "Connection! Start work", NotificationType.INFORMATION))


            if (myClientSocket == null) {
                throw RuntimeException("Client socket is null")
            }
            objectInputStream = ObjectInputStream(myClientSocket?.getInputStream())
            objectOutputStream = ObjectOutputStream(myClientSocket?.getOutputStream())

            startReading(project)
        })
        serverThread?.start()
    }

    fun connectToServer(project: Project) {
        val coeditPlugin = CoeditPlugin.getInstance(project)
        if (coeditPlugin.editing.get()) {
            return
        }
        val socket: Socket
        try {
            socket = Socket(myHost, myPort)
        } catch (e: ConnectException) {
            Notifications.Bus.notify(Notification("CoEdit", "CoEdit", "Cannot connect to server", NotificationType.ERROR))
            return
        }
        Notifications.Bus.notify(Notification("CoEdit", "CoEdit", "Connected to server", NotificationType.INFORMATION))

        objectOutputStream = ObjectOutputStream(socket.getOutputStream())
        objectInputStream = ObjectInputStream(socket.getInputStream())

        serverThread = Thread(Runnable { startReading(project) })
        serverThread?.start()
        coeditPlugin.editing.set(true)
        coeditPlugin.lockHandler.clear()
    }

    fun stopWork() {
        serverThread?.interrupt()
        objectOutputStream?.close()
        objectInputStream?.close()
        Notifications.Bus.notify(Notification("CoEdit", "CoEdit", "Stop work", NotificationType.INFORMATION))
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

        log.debug("Waiting for response...")
        val coResponse = responseQueue.poll(1, TimeUnit.SECONDS) ?: CoResponse.CONTINUE
        log.debug("Got response. ", coResponse)

        if (coResponse == CoResponse.ERROR) {
            log.error("Error response from server")
            Notifications.Bus.notify(Notification("CoEdit", "CoEdit", "Unhandled error on server", NotificationType.ERROR))
        }

        return coResponse
    }

    fun response(response: CoResponse) {
        log.debug("Response ", response)
        if (myClientSocket?.isClosed != true && myServerSocket?.isClosed != true) {
            objectOutputStream?.writeObject(response)
        }
    }

}