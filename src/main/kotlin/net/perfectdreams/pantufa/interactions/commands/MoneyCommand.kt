package net.perfectdreams.pantufa.interactions.commands

import net.perfectdreams.discordinteraktions.commands.get
import net.perfectdreams.discordinteraktions.declarations.slash.SlashCommandDeclaration
import net.perfectdreams.pantufa.PantufaBot
import net.perfectdreams.pantufa.api.commands.SilentCommandException
import net.perfectdreams.pantufa.utils.Constants
import net.perfectdreams.pantufa.utils.CraftConomyUtils
import net.perfectdreams.pantufa.utils.PantufaReply
import net.perfectdreams.pantufa.utils.formatToTwoDecimalPlaces

class MoneyCommand(pantufa: PantufaBot) : PantufaInteractionCommand(
    pantufa,
    this
) {
    companion object : SlashCommandDeclaration(
        name = "money",
        description = "Veja quantos sonhos você e outros jogadores do SparklyPower possuem"
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

            val serverAccountId = CraftConomyUtils.getCraftConomyAccountId(playerUniqueId) ?: throw SilentCommandException()
            val ccBalance = CraftConomyUtils.getCraftConomyBalance(serverAccountId)

            context.reply(
                PantufaReply(
                    message = "**`${playerData.username}`** possui **${ccBalance.formatToTwoDecimalPlaces()} Sonhos**!",
                    prefix = "\uD83D\uDCB5"
                )
            )
        } else {
            val accountInfo = context.retrieveConnectedMinecraftAccountOrFail()

            val serverAccountId = CraftConomyUtils.getCraftConomyAccountId(accountInfo.uniqueId) ?: throw SilentCommandException()
            val ccBalance = CraftConomyUtils.getCraftConomyBalance(serverAccountId)

            context.reply(
                PantufaReply(
                    message = "Você possui **${ccBalance.formatToTwoDecimalPlaces()} Sonhos**!",
                    prefix = "\uD83D\uDCB5"
                )
            )
        }
    }
}