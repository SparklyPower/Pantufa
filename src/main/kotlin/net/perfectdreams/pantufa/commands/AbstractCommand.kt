package net.perfectdreams.pantufa.commands

import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.perfectdreams.pantufa.Pantufa
import net.perfectdreams.pantufa.pantufa
import net.perfectdreams.pantufa.utils.Constants

abstract class AbstractCommand(val label: String, val aliases: List<String> = listOf(), val requiresMinecraftAccount: Boolean = false) {
	fun matches(event: MessageReceivedEvent): Boolean {
		val message = event.message.contentDisplay

		val args = message.split(" ").toMutableList()
		val command = args[0]
		args.removeAt(0)

		val labels = mutableListOf(label)
		labels.addAll(aliases)

		val valid = labels.any { command == Pantufa.PREFIX + it }

		if (!valid)
			return false

		event.channel.sendTyping().complete()

		/* val minecraftUsername = pantufa.getDiscordAccountFromId(event.author.id)?.minecraftUsername

		if (requiresMinecraftAccount && minecraftUsername == null) {
			event.textChannel.sendMessage("${Constants.ERROR} **|** ${event.author.asMention} Você precisa associar a sua conta do SparklyPower antes de poder usar este comando! Para associar, use `-registrar NomeNoServidor`!").complete()
			return true
		} */

		run(CommandContext(event, "???"))
		return true
	}

	abstract fun run(context: CommandContext)
}