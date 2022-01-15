package net.perfectdreams.pantufa.interactions.commands

import net.perfectdreams.discordinteraktions.common.commands.SlashCommandExecutorDeclaration
import net.perfectdreams.discordinteraktions.common.commands.options.ApplicationCommandOptions
import net.perfectdreams.discordinteraktions.common.commands.options.SlashCommandArguments
import net.perfectdreams.pantufa.PantufaBot
import net.perfectdreams.pantufa.utils.PantufaReply

class MinecraftUserPlayerNameExecutor(pantufa: PantufaBot) : PantufaInteractionCommand(
    pantufa
) {
    companion object : SlashCommandExecutorDeclaration(MinecraftUserPlayerNameExecutor::class) {
        object Options : ApplicationCommandOptions() {
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