package net.perfectdreams.pantufa.interactions.commands.declarations

import net.perfectdreams.discordinteraktions.common.commands.SlashCommandDeclarationWrapper
import net.perfectdreams.discordinteraktions.common.commands.slashCommand
import net.perfectdreams.pantufa.interactions.commands.GuildsExecutor

object GuildsCommand : SlashCommandDeclarationWrapper {
    override fun declaration() = slashCommand(
        name = "guilds",
        description = "Servidores que a Pantufa está"
    ) {
        executor = GuildsExecutor
    }
}