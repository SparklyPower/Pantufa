package net.perfectdreams.pantufa.commands.vanilla.utils

import net.perfectdreams.pantufa.PantufaBot
import net.perfectdreams.pantufa.api.commands.command
import net.perfectdreams.pantufa.utils.PantufaReply
import net.perfectdreams.pantufa.utils.extensions.await

object PingCommand {
	fun create(pantufa: PantufaBot) = command(pantufa, "PingCommand", listOf("ping")) {
		executes {
			val time = System.currentTimeMillis()

			val replies = mutableListOf(
					PantufaReply(
							message = "**Pong!**",
							prefix = ":ping_pong:"
					),
					PantufaReply(
							message = "**Gateway Ping:** `${pantufa.jda.gatewayPing}ms`",
							prefix = ":stopwatch:",
							mentionUser = false
					),
					PantufaReply(
							message = "**API Ping:** `...ms`",
							prefix = ":stopwatch:",
							mentionUser = false
					)
			)

			val message = reply(*replies.toTypedArray())

			replies.removeAt(2) // remova o último
			replies.add(
					PantufaReply(
							message = "**API Ping:** `${System.currentTimeMillis() - time}ms`",
							prefix = ":zap:",
							mentionUser = false
					)
			)

			message.editMessage(replies.joinToString(separator = "\n", transform = { it.build(this) })).await()
		}
	}
}