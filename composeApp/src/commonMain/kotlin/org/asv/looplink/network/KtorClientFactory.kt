package org.asv.looplink.network

import io.ktor.client.HttpClient
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.plugins.websocket.WebSockets
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

// Expect declaration for the platform-specific HTTP client engine
internal expect fun httpClientEngine(): io.ktor.client.engine.HttpClientEngine

// Common HttpClient configuration
fun createKtorClient(): HttpClient {
    return HttpClient(httpClientEngine()) { // Pass the platform-specific engine
        // Configure plugins

        // Content Negotiation for JSON
        install(ContentNegotiation) {
            json(Json {
                prettyPrint = true
                isLenient = true
                ignoreUnknownKeys = true // Be flexible with server responses
                explicitNulls = false
            })
        }

        install(WebSockets){
            maxFrameSize = Long.MAX_VALUE // Disabled (max value). The connection will be closed if surpassed this length.
        }

        // Logging (optional, but helpful for debugging)
        install(Logging) {
            logger = object : Logger {
                override fun log(message: String) {
                    println("Ktor HTTP Client: $message") // Simple logger
                }
            }
            level = LogLevel.ALL // Log everything, adjust as needed (INFO, HEADERS, BODY)
        }

        // Default request parameters (optional)
        // defaultRequest {
        // url("https://api.example.com/") // Base URL if all requests go to the same server
        // header(HttpHeaders.ContentType, ContentType.Application.Json)
        // }
    }
}
