package net.perfectdreams.pantufa.interactions.commands.declarations

import net.perfectdreams.discordinteraktions.declarations.slash.SlashCommandDeclaration
import net.perfectdreams.discordinteraktions.declarations.slash.slashCommand
import net.perfectdreams.pantufa.interactions.commands.PingExecutor
import net.perfectdreams.pantufa.interactions.commands.VIPInfoExecutor

object VIPInfoCommand : SlashCommandDeclaration {
    override fun declaration() = slashCommand(
        name = "vipinfo",
        description = "Veja quanto tempo falta para o seu VIP acabar"
    ) {
        executor = VIPInfoExecutor
    }
}