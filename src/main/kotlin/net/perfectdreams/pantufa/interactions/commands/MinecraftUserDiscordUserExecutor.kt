package net.perfectdreams.pantufa.interactions.commands

import net.perfectdreams.discordinteraktions.common.context.SlashCommandArguments
import net.perfectdreams.discordinteraktions.declarations.slash.SlashCommandExecutorDeclaration
import net.perfectdreams.discordinteraktions.declarations.slash.options.CommandOptions
import net.perfectdreams.pantufa.PantufaBot
import net.perfectdreams.pantufa.api.commands.SilentCommandException
import net.perfectdreams.pantufa.dao.CashInfo
import net.perfectdreams.pantufa.interactions.commands.MinecraftUserPlayerNameExecutor.Companion.Options.register
import net.perfectdreams.pantufa.network.Databases
import net.perfectdreams.pantufa.utils.Constants
import net.perfectdreams.pantufa.utils.PantufaReply
import org.jetbrains.exposed.sql.transactions.transaction

class MinecraftUserDiscordUserExecutor(pantufa: PantufaBot) : PantufaInteractionCommand(
    pantufa
) {
    companion object : SlashCommandExecutorDeclaration(MinecraftUserDiscordUserExecutor::class) {
        object Options : CommandOptions() {
            val user = user("user", "Conta do Usuário no Discord")
                .register()
        }

        override val options = Options
    }

    override suspend fun executePantufa(context: PantufaCommandContext, args: SlashCommandArguments) {
        val discordUser = args[options.user]

        val minecraftUser = pantufa.getDiscordAccountFromId(discordUser.id.value) ?: run {
            context.reply(
                PantufaReply(
                    "O usuário <@${discordUser.id.value}> não tem uma conta associada!"
                )
            )
            return
        }

        val userInfo = pantufa.getMinecraftUserFromUniqueId(minecraftUser.minecraftId)

        context.reply(
            PantufaReply(
                "**Informações da conta de <@${discordUser.id.value}>**"
            ),
            PantufaReply(
                "**Nome:** `${userInfo?.username}`",
                mentionUser = false
            ),
            PantufaReply(
                "**UUID:** `${minecraftUser.minecraftId}`",
                mentionUser = false
            ),
            PantufaReply(
                "**A conta já foi conectada?** ${minecraftUser.isConnected}",
                mentionUser = false
            )
        )
    }
}