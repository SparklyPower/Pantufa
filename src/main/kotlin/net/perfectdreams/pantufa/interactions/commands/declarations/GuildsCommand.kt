package net.perfectdreams.pantufa.interactions.commands.declarations

import net.perfectdreams.discordinteraktions.declarations.slash.SlashCommandDeclaration
import net.perfectdreams.discordinteraktions.declarations.slash.slashCommand
import net.perfectdreams.pantufa.interactions.commands.GuildsExecutor

object GuildsCommand : SlashCommandDeclaration {
    override fun declaration() = slashCommand(
        name = "guilds",
        description = "Servidores que a Pantufa está"
    ) {
        executor = GuildsExecutor
    }
}