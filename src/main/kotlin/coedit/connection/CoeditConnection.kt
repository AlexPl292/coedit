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
import java.util.*
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
    private lateinit var myServerSocket: ServerSocket
    private lateinit var myClientSocket: Socket

    private lateinit var objectOutputStream: ObjectOutputStream
    private lateinit var objectInputStream: ObjectInputStream

    private lateinit var serverThread: Thread

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
            myClientSocket = myServerSocket.accept()
            waitForConnection.set(false)
            val coeditPlugin1 = CoeditPlugin.getInstance(project)
            coeditPlugin1.editing.set(true)
            coeditPlugin1.lockHandler.clear()
            Notifications.Bus.notify(Notification("CoEdit", "CoEdit", "Connection! Start work", NotificationType.INFORMATION))

            objectInputStream = ObjectInputStream(myClientSocket.getInputStream())
            objectOutputStream = ObjectOutputStream(myClientSocket.getOutputStream())

            startReading(project)
        })
        serverThread.start()
    }

    fun connectToServer(project: Project) {
        val coeditPlugin = CoeditPlugin.getInstance(project)
        if (coeditPlugin.editing.get()) {
            return
        }
        try {
            myClientSocket = Socket(myHost, myPort)
        } catch (e: ConnectException) {
            Notifications.Bus.notify(Notification("CoEdit", "CoEdit", "Cannot connect to server", NotificationType.ERROR))
            return
        }
        Notifications.Bus.notify(Notification("CoEdit", "CoEdit", "Connected to server", NotificationType.INFORMATION))

        objectOutputStream = ObjectOutputStream(myClientSocket.getOutputStream())
        objectInputStream = ObjectInputStream(myClientSocket.getInputStream())

        serverThread = Thread(Runnable { startReading(project) })
        serverThread.start()
        coeditPlugin.editing.set(true)
        coeditPlugin.lockHandler.clear()
    }

    fun stopWork() {
        serverThread.interrupt()
        objectOutputStream.close()
        objectInputStream.close()
        myClientSocket.close()
        if (this::myServerSocket.isInitialized) {
            myServerSocket.close()
        }
        Notifications.Bus.notify(Notification("CoEdit", "CoEdit", "Stop work", NotificationType.INFORMATION))
    }

    private fun startReading(project: Project) {
        val changesService = ChangesService(project)
        while (true) {
            if (Thread.interrupted()) break
            log.debug("Wait for incoming requests")
            val request = objectInputStream.readObject()
            log.debug("Read object from input stream.")

            if (Thread.interrupted()) break

            if (request is CoResponse) {
                log.debug("Request is response")
                if (request.requestUuid != null) {
                    log.debug("Request has not null requestUuid, ", request)
                    responseQueue.offer(request, 5, TimeUnit.SECONDS)
                    log.debug("Put response into queue")
                }
                continue
            }
            log.debug("Got request. ", request)
            changesService.handleChange(request as CoRequest)
        }
    }

    fun sendAndWaitForResponse(request: CoRequest): CoResponse {
        request.requestUuid = UUID.randomUUID().toString()
        log.debug("Sending object. ", request)
        objectOutputStream.writeUnshared(request)

        log.debug("Waiting for response...")
        var coResponse: CoResponse
        do {
            coResponse = responseQueue.poll(5, TimeUnit.SECONDS) ?: CoResponse.CONTINUE
            if (coResponse.requestUuid == request.requestUuid) {
                break
            }
        } while (coResponse != CoResponse.CONTINUE)
        log.debug("Got response. ", coResponse)

        return coResponse
    }

    fun send(request: CoRequest) {
        log.debug("Sending object without waiting for response", request)
        request.requestUuid = null
        objectOutputStream.writeUnshared(request)
    }

    fun response(response: CoResponse) {
        log.debug("Response ", response)
        if (!myClientSocket.isClosed) {
            objectOutputStream.writeUnshared(response)
        }
    }

}