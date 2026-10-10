package com.bananahacks.agent

import com.bananahacks.agent.core.BananaAgent
import com.bananahacks.agent.model.AgentStatusResponse
import com.bananahacks.agent.model.ChatRequest
import com.bananahacks.agent.model.ChatResponse
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*

import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.cors.routing.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.http.content.*
import kotlinx.serialization.json.Json
import java.util.UUID

fun main() {
    embeddedServer(Netty, port = 8080, host = "0.0.0.0", module = Application::module)
        .start(wait = true)
}

fun Application.module() {
    install(ContentNegotiation) {
        json(Json {
            prettyPrint = true
            isLenient = true
            ignoreUnknownKeys = true
        })
    }

    install(CORS) {
        anyHost()
    }

    val agent = BananaAgent()

    routing {
        // API Endpoints
        route("/api") {
            post("/chat") {
                val request = call.receive<ChatRequest>()
                val conversationId = request.conversationId ?: UUID.randomUUID().toString()
                
                val reply = agent.processMessage(request.prompt, conversationId)

                call.respond(
                    ChatResponse(
                        response = reply,
                        conversationId = conversationId,
                        agentName = agent.name
                    )
                )
            }

            get("/status") {
                call.respond(
                    AgentStatusResponse(
                        agentName = agent.name,
                        status = "ONLINE",
                        activeTools = agent.getToolNames(),
                        uptimeMs = agent.getUptime()
                    )
                )
            }
        }

        // Static Web UI hosting
        staticResources("/", "static") {
            default("index.html")
        }
    }
}
