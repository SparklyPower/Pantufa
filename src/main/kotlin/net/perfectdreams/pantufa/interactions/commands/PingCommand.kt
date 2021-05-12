package net.perfectdreams.pantufa.interactions.commands

import net.perfectdreams.discordinteraktions.declarations.slash.SlashCommandDeclaration
import net.perfectdreams.pantufa.PantufaBot
import net.perfectdreams.pantufa.utils.PantufaReply

class PingCommand(pantufa: PantufaBot) : PantufaInteractionCommand(
    pantufa,
    this
) {
    companion object : SlashCommandDeclaration(
        name = "ping",
        description = "Pong! \uD83C\uDFD3"
    )

    override suspend fun executesPantufa(context: PantufaCommandContext) {
        context.reply(
            PantufaReply(
                "Pong! \uD83C\uDFD3"
            )
        )
    }
}