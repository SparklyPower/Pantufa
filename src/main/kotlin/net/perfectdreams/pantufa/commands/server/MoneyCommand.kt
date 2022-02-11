package net.perfectdreams.pantufa.commands.server

import net.perfectdreams.pantufa.PantufaBot
import net.perfectdreams.pantufa.api.commands.SilentCommandException
import net.perfectdreams.pantufa.api.commands.command
import net.perfectdreams.pantufa.utils.Constants
import net.perfectdreams.pantufa.utils.CraftConomyUtils
import net.perfectdreams.pantufa.utils.PantufaReply
import net.perfectdreams.pantufa.utils.formatToTwoDecimalPlaces

object MoneyCommand {
	fun create(pantufa: PantufaBot) = command(pantufa, "MoneyCommand", listOf("money", "dinheiro", "bal", "balance")) {
		executes {
			val playerName = args.getOrNull(0)

			if (playerName != null) {
				val playerData = pantufa.retrieveMinecraftUserFromUsername(playerName) ?: run {
					reply(
						PantufaReply(
							content = "Player desconhecido!",
							prefix = Constants.ERROR
						)
					)
					throw SilentCommandException()
				}
				val playerUniqueId = playerData.id.value

				val serverAccountId = CraftConomyUtils.getCraftConomyAccountId(playerUniqueId) ?: throw SilentCommandException()
				val ccBalance = CraftConomyUtils.getCraftConomyBalance(serverAccountId)

				reply(
					PantufaReply(
						content = "**`${playerData.username}`** possui **${ccBalance.formatToTwoDecimalPlaces()} Sonecas**!",
						prefix = "\uD83D\uDCB5"
					)
				)
			} else {
				val accountInfo = retrieveConnectedMinecraftAccountOrFail()

				val serverAccountId = CraftConomyUtils.getCraftConomyAccountId(accountInfo.uniqueId) ?: throw SilentCommandException()
				val ccBalance = CraftConomyUtils.getCraftConomyBalance(serverAccountId)

				reply(
					PantufaReply(
						content = "Você possui **${ccBalance.formatToTwoDecimalPlaces()} Sonecas**!",
						prefix = "\uD83D\uDCB5"
					)
				)
			}
		}
	}
}