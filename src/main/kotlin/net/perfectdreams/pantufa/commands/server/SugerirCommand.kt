package net.perfectdreams.pantufa.commands.server

import net.dv8tion.jda.api.entities.emoji.Emoji
import net.perfectdreams.pantufa.PantufaBot
import net.perfectdreams.pantufa.api.commands.command
import net.perfectdreams.pantufa.utils.Constants
import net.perfectdreams.pantufa.utils.PantufaReply
import net.perfectdreams.pantufa.utils.extensions.await

object SugerirCommand {
	fun create(pantufa: PantufaBot) = command(pantufa, "SugerirCommand", listOf("sugerir")) {
		executes {
			val suggestionChannel = pantufa.jda.getTextChannelById(840302862084734996L)!!

			if (this.args.isEmpty()) {
				reply(
					PantufaReply(
						"Você precisa escrever a sua sugestão junto com o comando!",
						Constants.ERROR
					)
				)
			} else {
				val message = suggestionChannel.sendMessage("**Sugestão de ${this.sender.asMention}:**\n\n" + this.args.joinToString(" "))
					.allowedMentions(listOf())
					.await()

				message.addReaction(Emoji.fromCustom("pantufa_thumbsup", 53048446826840104, false))
					.await()
				message.addReaction(Emoji.fromCustom("pantufa_analise", 853048446813470762, false))
					.await()

				reply(
					PantufaReply(
						"Sugestão enviada com sucesso!"
					)
				)
			}
		}
	}
}