package net.perfectdreams.pantufa.utils

import net.dv8tion.jda.api.entities.Guild
import net.perfectdreams.pantufa.PantufaBot
import net.perfectdreams.pantufa.commands.CommandContext
import net.perfectdreams.pantufa.interactions.commands.PantufaCommandContext
import java.awt.Color

object Constants {
	const val LEFT_PADDING = "\uD83D\uDD39"
	const val ERROR = "<:error:412585701054611458>"
	const val PERFECTDREAMS_LOBBY_PORT = 60802
	const val PERFECTDREAMS_SURVIVAL_PORT = 60801
	const val PERFECTDREAMS_BUNGEE_IP = "172.31.255.1"
	const val PERFECTDREAMS_BUNGEE_PORT = 60800
	const val LORITTA_PORT = 10699
	const val LOCAL_HOST = "127.0.0.1"
	const val PERFECTDREAMS_OFFLINE_MESSAGE = "Acho que o SparklyPower está offline..."
	const val SPARKLYPOWER_GUILD_ID = "320248230917046282"
	val SPARKLYPOWER_GUILD: Guild?
		get() = PantufaBot.INSTANCE.jda.getGuildById(SPARKLYPOWER_GUILD_ID)

	const val SPARKLYPOWER_STAFF_CHANNEL_ID = 417059128519819265L

	val PERFECTDREAMS_OFFLINE: ((CommandContext) -> Unit) = { context ->
		context.reply(
				PantufaReply(
						message = Constants.PERFECTDREAMS_OFFLINE_MESSAGE,
						prefix = Constants.ERROR
				)
		)
	}

	val SPARKLYPOWER_OFFLINE: (suspend (PantufaCommandContext) -> Unit) = { context ->
		context.reply(
			PantufaReply(
				message = Constants.PERFECTDREAMS_OFFLINE_MESSAGE,
				prefix = Constants.ERROR
			)
		)
	}

	val LORITTA_AQUA = Color(0, 193, 223)

	val WHITE_SPACE_MULTIPLE_REGEX = Regex(" +")
}