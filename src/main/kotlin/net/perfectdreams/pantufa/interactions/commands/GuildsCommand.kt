package net.perfectdreams.pantufa.interactions.commands

import net.perfectdreams.discordinteraktions.declarations.slash.SlashCommandDeclaration
import net.perfectdreams.pantufa.PantufaBot
import net.perfectdreams.pantufa.utils.PantufaReply

class GuildsCommand(pantufa: PantufaBot) : PantufaInteractionCommand(
    pantufa, this
) {
    companion object : SlashCommandDeclaration(
        name = "guilds",
        description = "Servidores que a Pantufa está"
    )

    override suspend fun executesPantufa(context: PantufaCommandContext) {
        context.reply(
            PantufaReply(
                pantufa.jda.guilds.map { it.name }.joinToString()
            )
        )
    }
}