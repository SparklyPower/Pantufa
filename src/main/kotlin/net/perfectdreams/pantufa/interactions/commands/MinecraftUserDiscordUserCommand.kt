package net.perfectdreams.pantufa.interactions.commands

import net.perfectdreams.discordinteraktions.commands.get
import net.perfectdreams.pantufa.PantufaBot
import net.perfectdreams.pantufa.utils.PantufaReply

class MinecraftUserDiscordUserCommand(pantufa: PantufaBot) : PantufaInteractionCommand(
    pantufa,
    MinecraftUserCommandDeclaration.DiscordUser,
    MinecraftUserCommandDeclaration.Root
) {
    override suspend fun executesPantufa(context: PantufaCommandContext) {
        val discordUser = MinecraftUserCommandDeclaration.DiscordUser.options.user.get(context.interactionContext)

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