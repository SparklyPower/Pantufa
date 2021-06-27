package net.perfectdreams.pantufa.interactions.commands.declarations

import net.perfectdreams.discordinteraktions.declarations.slash.SlashCommandDeclaration
import net.perfectdreams.discordinteraktions.declarations.slash.slashCommand
import net.perfectdreams.pantufa.interactions.commands.PingExecutor

object PingCommand : SlashCommandDeclaration {
    override fun declaration() = slashCommand(
        name = "ping",
        description = "Pong! \uD83C\uDFD3"
    ) {
        executor = PingExecutor
    }
}