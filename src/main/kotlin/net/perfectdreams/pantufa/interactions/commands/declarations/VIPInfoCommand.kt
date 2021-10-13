package net.perfectdreams.pantufa.interactions.commands.declarations

import net.perfectdreams.discordinteraktions.declarations.commands.slash.slashCommand
import net.perfectdreams.discordinteraktions.declarations.commands.wrappers.SlashCommandDeclarationWrapper
import net.perfectdreams.pantufa.interactions.commands.VIPInfoExecutor

object VIPInfoCommand : SlashCommandDeclarationWrapper {
    override fun declaration() = slashCommand(
        name = "vipinfo",
        description = "Veja quanto tempo falta para o seu VIP acabar"
    ) {
        executor = VIPInfoExecutor
    }
}