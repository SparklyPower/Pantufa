package net.perfectdreams.pantufa.interactions.commands

import net.perfectdreams.discordinteraktions.common.commands.SlashCommandExecutorDeclaration
import net.perfectdreams.discordinteraktions.common.commands.options.ApplicationCommandOptions
import net.perfectdreams.discordinteraktions.common.commands.options.SlashCommandArguments
import net.perfectdreams.pantufa.PantufaBot
import net.perfectdreams.pantufa.api.commands.SilentCommandException
import net.perfectdreams.pantufa.utils.Constants
import net.perfectdreams.pantufa.utils.CraftConomyUtils
import net.perfectdreams.pantufa.utils.PantufaReply
import net.perfectdreams.pantufa.utils.formatToTwoDecimalPlaces

class MoneyExecutor(pantufa: PantufaBot) : PantufaInteractionCommand(
    pantufa
) {
    companion object : SlashCommandExecutorDeclaration(MoneyExecutor::class) {
        object Options : ApplicationCommandOptions() {
            val playerName = optionalString("player_name", "Nome do Player")
                .register()
        }

        override val options = Options
    }

    override suspend fun executePantufa(context: PantufaCommandContext, args: SlashCommandArguments) {
        val playerName = args[options.playerName]

        if (playerName != null) {
            val playerData = pantufa.retrieveMinecraftUserFromUsername(playerName) ?: run {
                context.reply(
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

            context.reply(
                PantufaReply(
                    content = "**`${playerData.username}`** possui **${ccBalance.formatToTwoDecimalPlaces()} Sonhos**!",
                    prefix = "\uD83D\uDCB5"
                )
            )
        } else {
            val accountInfo = context.retrieveConnectedMinecraftAccountOrFail()

            val serverAccountId = CraftConomyUtils.getCraftConomyAccountId(accountInfo.uniqueId) ?: throw SilentCommandException()
            val ccBalance = CraftConomyUtils.getCraftConomyBalance(serverAccountId)

            context.reply(
                PantufaReply(
                    content = "Você possui **${ccBalance.formatToTwoDecimalPlaces()} Sonhos**!",
                    prefix = "\uD83D\uDCB5"
                )
            )
        }
    }
}