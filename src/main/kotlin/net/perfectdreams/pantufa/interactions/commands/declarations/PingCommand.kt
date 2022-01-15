package net.perfectdreams.pantufa.interactions.commands.declarations

import net.perfectdreams.discordinteraktions.common.commands.SlashCommandDeclarationWrapper
import net.perfectdreams.discordinteraktions.common.commands.slashCommand
import net.perfectdreams.pantufa.interactions.commands.PingExecutor

object PingCommand : SlashCommandDeclarationWrapper {
    override fun declaration() = slashCommand(
        name = "ping",
        description = "Pong! \uD83C\uDFD3"
    ) {
        executor = PingExecutor
    }
}