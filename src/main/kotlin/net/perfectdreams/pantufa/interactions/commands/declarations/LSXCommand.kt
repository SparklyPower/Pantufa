package net.perfectdreams.pantufa.interactions.commands.declarations

import net.perfectdreams.discordinteraktions.declarations.commands.slash.slashCommand
import net.perfectdreams.discordinteraktions.declarations.commands.wrappers.SlashCommandDeclarationWrapper
import net.perfectdreams.pantufa.interactions.commands.LSXExecutor

object LSXCommand : SlashCommandDeclarationWrapper {
    override fun declaration() = slashCommand(
        name = "transferir",
        description = "LorittaLand Sonhos Exchange Service: Transfira sonhos da Loritta para o SparklyPower e vice-versa!"
    ) {
        executor = LSXExecutor
    }
}