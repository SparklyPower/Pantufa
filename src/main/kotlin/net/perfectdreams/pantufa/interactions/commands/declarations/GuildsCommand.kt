package net.perfectdreams.pantufa.interactions.commands.declarations

import net.perfectdreams.discordinteraktions.declarations.commands.slash.slashCommand
import net.perfectdreams.discordinteraktions.declarations.commands.wrappers.SlashCommandDeclarationWrapper
import net.perfectdreams.pantufa.interactions.commands.GuildsExecutor

object GuildsCommand : SlashCommandDeclarationWrapper {
    override fun declaration() = slashCommand(
        name = "guilds",
        description = "Servidores que a Pantufa está"
    ) {
        executor = GuildsExecutor
    }
}