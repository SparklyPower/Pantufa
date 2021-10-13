package net.perfectdreams.pantufa.interactions.commands.declarations

import net.perfectdreams.discordinteraktions.declarations.commands.slash.slashCommand
import net.perfectdreams.discordinteraktions.declarations.commands.wrappers.SlashCommandDeclarationWrapper
import net.perfectdreams.pantufa.interactions.commands.PingExecutor

object PingCommand : SlashCommandDeclarationWrapper {
    override fun declaration() = slashCommand(
        name = "ping",
        description = "Pong! \uD83C\uDFD3"
    ) {
        executor = PingExecutor
    }
}