package net.perfectdreams.pantufa.utils

import net.dv8tion.jda.api.entities.User
import net.perfectdreams.pantufa.commands.CommandContext

class PantufaReply(
		val message: String = " ",
		val prefix: String? = null,
		val hasPadding: Boolean = true,
		val mentionUser: Boolean = true
) {
	fun build(commandContext: CommandContext) = build(commandContext.user)

	fun build(commandContext: net.perfectdreams.pantufa.api.commands.CommandContext) = build(commandContext.sender)

	fun build(user: User): String {
		var send = ""
		if (prefix != null) {
			send = "$prefix **|** "
		} else if (hasPadding) {
			send = Constants.LEFT_PADDING + " **|** "
		}
		if (mentionUser) {
			send = send + user.asMention + " "
		}
		send += message
		return send
	}

	fun build(userId: Long): String {
		var send = ""
		if (prefix != null) {
			send = "$prefix **|** "
		} else if (hasPadding) {
			send = Constants.LEFT_PADDING + " **|** "
		}
		if (mentionUser) {
			send = send + "<@${userId}>" + " "
		}
		send += message
		return send
	}
}