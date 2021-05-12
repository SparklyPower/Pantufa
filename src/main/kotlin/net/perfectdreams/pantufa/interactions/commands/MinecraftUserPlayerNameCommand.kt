package net.perfectdreams.pantufa.interactions.commands

import net.perfectdreams.discordinteraktions.commands.get
import net.perfectdreams.pantufa.PantufaBot
import net.perfectdreams.pantufa.utils.PantufaReply

class MinecraftUserPlayerNameCommand(pantufa: PantufaBot) : PantufaInteractionCommand(
    pantufa,
    MinecraftUserCommandDeclaration.MinecraftPlayer,
    MinecraftUserCommandDeclaration.Root
) {
    override suspend fun executesPantufa(context: PantufaCommandContext) {
        val playerName = MinecraftUserCommandDeclaration.MinecraftPlayer.options.playerName.get(context.interactionContext)

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