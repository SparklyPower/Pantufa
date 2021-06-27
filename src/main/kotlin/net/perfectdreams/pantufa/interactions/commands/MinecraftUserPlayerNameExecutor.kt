package net.perfectdreams.pantufa.interactions.commands

import net.perfectdreams.discordinteraktions.common.context.SlashCommandArguments
import net.perfectdreams.discordinteraktions.declarations.slash.SlashCommandExecutorDeclaration
import net.perfectdreams.discordinteraktions.declarations.slash.options.CommandOptions
import net.perfectdreams.pantufa.PantufaBot
import net.perfectdreams.pantufa.api.commands.SilentCommandException
import net.perfectdreams.pantufa.dao.CashInfo
import net.perfectdreams.pantufa.network.Databases
import net.perfectdreams.pantufa.utils.Constants
import net.perfectdreams.pantufa.utils.PantufaReply
import org.jetbrains.exposed.sql.transactions.transaction

class MinecraftUserPlayerNameExecutor(pantufa: PantufaBot) : PantufaInteractionCommand(
    pantufa
) {
    companion object : SlashCommandExecutorDeclaration(MinecraftUserPlayerNameExecutor::class) {
        object Options : CommandOptions() {
            val playerName = string("player_name", "Nome do Player")
                .register()
        }

        override val options = Options
    }

    override suspend fun executePantufa(context: PantufaCommandContext, args: SlashCommandArguments) {
        val playerName = args[options.playerName]

        val minecraftUser = pantufa.getMinecraftUserFromUsername(playerName) ?: run {
            context.reply(
                PantufaReply(
                    "O usuário não tem uma conta associada!"
                )
            )
            return
        }

        val userInfo = pantufa.getDiscordAccountFromUniqueId(minecraftUser.id.value)

        context.reply(
            PantufaReply(
                "**Informações da conta de <@${userInfo?.discordId}>**"
            ),
            PantufaReply(
                "**Nome:** `${minecraftUser.username}`",
                mentionUser = false
            ),
            PantufaReply(
                "**UUID:** `${userInfo?.minecraftId}`",
                mentionUser = false
            ),
            PantufaReply(
                "**A conta já foi conectada?** ${userInfo?.isConnected}",
                mentionUser = false
            )
        )
    }
}