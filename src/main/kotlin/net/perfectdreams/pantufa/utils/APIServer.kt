package net.perfectdreams.pantufa.utils

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import mu.KotlinLogging
import net.dv8tion.jda.api.exceptions.ErrorResponseException
import net.perfectdreams.pantufa.PantufaBot
import net.perfectdreams.pantufa.rpc.GetDiscordUserRequest
import net.perfectdreams.pantufa.rpc.GetDiscordUserResponse
import net.perfectdreams.pantufa.rpc.PantufaRPCRequest
import net.perfectdreams.pantufa.rpc.PantufaRPCResponse
import net.perfectdreams.pantufa.utils.extensions.await
import java.util.*

class APIServer(private val m: PantufaBot) {
    companion object {
        private val logger = KotlinLogging.logger {}
    }

    private var server: ApplicationEngine? = null

    fun start() {
        logger.info { "Starting HTTP Server..." }
        val server = embeddedServer(Netty, port = m.config.rpcPort) {
            routing {
                get("/") {
                    call.respondText("SparklyPower API Web Server")
                }

                post("/rpc") {
                    val jsonPayload = call.receiveText()
                    logger.info { "${call.request.userAgent()} sent a RPC request: $jsonPayload" }
                    val response = when (val request = Json.decodeFromString<PantufaRPCRequest>(jsonPayload)) {
                        is GetDiscordUserRequest -> {
                            val user = try { m.jda.retrieveUserById(request.userId).await() } catch (e: ErrorResponseException) { null }

                            if (user != null) {
                                GetDiscordUserResponse.Success(
                                    user.idLong,
                                    user.name,
                                    user.discriminator,
                                    user.avatarId,
                                    user.isBot,
                                    user.isSystem,
                                    user.flagsRaw
                                )
                            } else {
                                GetDiscordUserResponse.NotFound
                            }
                        }
                    }

                    call.respondText(
                        Json.encodeToString<PantufaRPCResponse>(response),
                        ContentType.Application.Json
                    )
                }
            }
        }

        // If set to "wait = true", the server hangs
        this.server = server.start(wait = false)
        logger.info { "Successfully started HTTP Server!" }
    }
}