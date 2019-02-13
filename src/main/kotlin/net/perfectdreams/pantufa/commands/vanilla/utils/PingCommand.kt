package net.perfectdreams.pantufa.commands.vanilla.utils

import net.perfectdreams.pantufa.commands.AbstractCommand
import net.perfectdreams.pantufa.commands.CommandContext
import net.perfectdreams.pantufa.jda
import net.perfectdreams.pantufa.utils.PantufaReply

class PingCommand : AbstractCommand("ping") {
	override fun run(context: CommandContext) {
		context.reply(
				PantufaReply(
						message = "**Pong!** `${jda.gatewayPing}ms`",
						prefix = "\uD83C\uDFD3"
				)
		)
	}
}