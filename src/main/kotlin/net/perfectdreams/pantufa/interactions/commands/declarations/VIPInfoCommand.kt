package net.perfectdreams.pantufa.interactions.commands.declarations

import net.perfectdreams.discordinteraktions.common.commands.SlashCommandDeclarationWrapper
import net.perfectdreams.discordinteraktions.common.commands.slashCommand
import net.perfectdreams.pantufa.interactions.commands.VIPInfoExecutor

object VIPInfoCommand : SlashCommandDeclarationWrapper {
    override fun declaration() = slashCommand(
        name = "vipinfo",
        description = "Veja quanto tempo falta para o seu VIP acabar"
    ) {
        executor = VIPInfoExecutor
    }
}