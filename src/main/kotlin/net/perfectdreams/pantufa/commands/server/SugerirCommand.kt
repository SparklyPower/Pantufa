package net.perfectdreams.pantufa.commands.server

import net.perfectdreams.pantufa.PantufaBot
import net.perfectdreams.pantufa.api.commands.command
import net.perfectdreams.pantufa.commands.AbstractCommand
import net.perfectdreams.pantufa.commands.CommandContext
import net.perfectdreams.pantufa.dao.CashInfo
import net.perfectdreams.pantufa.network.Databases
import net.perfectdreams.pantufa.tables.LuckPermsUserPermissions
import net.perfectdreams.pantufa.utils.Constants
import net.perfectdreams.pantufa.utils.DateUtils
import net.perfectdreams.pantufa.utils.PantufaReply
import net.perfectdreams.pantufa.utils.extensions.await
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import java.awt.Color

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
				suggestionChannel.sendMessage("**Sugestão de ${this.sender.asMention}:**\n\n" + this.args.joinToString(" "))
					.allowedMentions(listOf())
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