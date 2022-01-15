package net.perfectdreams.pantufa.interactions.commands.declarations

import net.perfectdreams.discordinteraktions.common.commands.SlashCommandDeclarationWrapper
import net.perfectdreams.discordinteraktions.common.commands.slashCommand
import net.perfectdreams.pantufa.interactions.commands.RegistrarExecutor

object RegistrarCommand : SlashCommandDeclarationWrapper {
    override fun declaration() = slashCommand(
        name = "registrar",
        description = "Conecte a sua conta do Discord com a do SparklyPower para expandir a sua experiência de jogo!"
    ) {
        executor = RegistrarExecutor
    }
}