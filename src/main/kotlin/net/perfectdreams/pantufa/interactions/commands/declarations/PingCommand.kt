package net.perfectdreams.pantufa.interactions.commands.declarations

import net.perfectdreams.discordinteraktions.common.commands.SlashCommandDeclarationWrapper
import net.perfectdreams.discordinteraktions.common.commands.slashCommand
import net.perfectdreams.pantufa.PantufaBot
import net.perfectdreams.pantufa.interactions.commands.PingExecutor

class PingCommand(val m: PantufaBot)  : SlashCommandDeclarationWrapper {
    override fun declaration() = slashCommand(
        name = "ping",
        description = "Pong! \uD83C\uDFD3"
    ) {
        executor = PingExecutor(m)
    }
}