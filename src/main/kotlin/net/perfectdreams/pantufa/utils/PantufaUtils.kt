package net.perfectdreams.pantufa.utils

import net.dv8tion.jda.api.entities.User
import net.perfectdreams.pantufa.commands.CommandContext
import net.perfectdreams.pantufa.pantufa

object PantufaUtils {
	fun getUserFromContext(context: CommandContext, argument: Int): User? {
		val arg = context.args.getOrNull(argument)

		if (arg != null) {
			for (user in context.event.message.mentionedUsers) {
				if (arg.replace("!", "") == user.asMention) {
					return user
				}
			}
		}
		return null
	}

	/* fun getUsernameFromContext(context: CommandContext, argument: Int): String? {
		val username = context.args.getOrNull(argument)

		if (username != null) {
			val user = getUserFromContext(context, argument)

			if (user != null)
				return pantufa.getDiscordAccountFromUser(user)?.minecraftUsername
		}

		return username
	} */
}