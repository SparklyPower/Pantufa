package net.perfectdreams.pantufa.interactions.commands

import net.perfectdreams.discordinteraktions.commands.get
import net.perfectdreams.discordinteraktions.declarations.slash.SlashCommandDeclaration
import net.perfectdreams.pantufa.PantufaBot
import net.perfectdreams.pantufa.api.commands.SilentCommandException
import net.perfectdreams.pantufa.dao.CashInfo
import net.perfectdreams.pantufa.network.Databases
import net.perfectdreams.pantufa.utils.Constants
import net.perfectdreams.pantufa.utils.PantufaReply
import org.jetbrains.exposed.sql.transactions.transaction

class PesadelosCommand(pantufa: PantufaBot) : PantufaInteractionCommand(
    pantufa, this
) {
    companion object : SlashCommandDeclaration(
        name = "pesadelos",
        description = "Veja quantos pesadelos você e outros jogadores do SparklyPower possuem"
    ) {
        override val options = Options

        object Options : SlashCommandDeclaration.Options() {
            val username = string("player_name", "Nome do Player")
                .register()
        }
    }

    override suspend fun executesPantufa(context: PantufaCommandContext) {
        val playerName = options.username.get(context.interactionContext)

        if (playerName != null) {
            val playerData = pantufa.retrieveMinecraftUserFromUsername(playerName) ?: run {
                context.reply(
                    PantufaReply(
                        message = "Player desconhecido!",
                        prefix = Constants.ERROR
                    )
                )
                throw SilentCommandException()
            }
            val playerUniqueId = playerData.id.value

            val cash = transaction(Databases.sparklyPower) {
                CashInfo.findById(playerUniqueId)
            }?.cash ?: 0

            context.reply(
                PantufaReply(
                    message = "**`${playerData.username}`** possui **${cash} Pesadelos**!",
                    prefix = "\uD83D\uDCB5"
                )
            )
        } else {
            val accountInfo = context.retrieveConnectedMinecraftAccountOrFail()
            val playerUniqueId = accountInfo.uniqueId

            val cash = transaction(Databases.sparklyPower) {
                CashInfo.findById(playerUniqueId)
            }?.cash ?: 0

            context.reply(
                PantufaReply(
                    message = "Você possui **${cash} Pesadelos**!",
                    prefix = "\uD83D\uDCB5"
                )
            )
        }
    }
}