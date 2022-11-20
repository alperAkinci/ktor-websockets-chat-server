package com.example.plugins

import com.example.Connection
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import io.ktor.websocket.*
import java.time.Duration
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.request.*
import io.ktor.server.routing.*
import java.lang.Exception
import java.util.*
import kotlin.collections.LinkedHashSet

fun Application.configureSockets() {
    install(WebSockets) {
        pingPeriod = Duration.ofSeconds(15)
        timeout = Duration.ofSeconds(15)
        maxFrameSize = Long.MAX_VALUE
        masking = false
    }

    routing {
        val connections = Collections.synchronizedSet<Connection?>(LinkedHashSet())
        webSocket("/chat") { // websocketSession
            println("Adding user!")
            val thisConnection = Connection(this)
            connections += thisConnection

            try {
                send("You are connected! There are ${connections.count()}")
                for (frame in incoming) {
                    if (frame is Frame.Text) {
                        val text = frame.readText()
                        val textWithUsername = "[${thisConnection.name}]: $text"

                        connections.forEach {
                            if (it.name.equals(thisConnection.name).not()) {
                                it.session.outgoing.send(Frame.Text(textWithUsername))
                            }
                        }

                        //if (text.equals("[${thisConnection.name}]: quit", ignoreCase = true)) {
                        //    close(CloseReason(CloseReason.Codes.NORMAL, "Client said BYE"))
                        //}

                        //if (text.equals("[${thisConnection.name}]: remove me", ignoreCase = true)) {
                        //    close(CloseReason(CloseReason.Codes.NORMAL, "Client said BYE"))
                        //}
                    }
                }
            } catch (e: Exception) {
                println(e.localizedMessage)
            } finally {
                println("Removing $thisConnection!")
                connections -= thisConnection
            }
        }
    }
}
