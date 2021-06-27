package net.perfectdreams.pantufa.interactions.commands.declarations

import net.perfectdreams.discordinteraktions.declarations.slash.SlashCommandDeclaration
import net.perfectdreams.discordinteraktions.declarations.slash.slashCommand
import net.perfectdreams.pantufa.interactions.commands.MoneyExecutor
import net.perfectdreams.pantufa.interactions.commands.PesadelosExecutor
import net.perfectdreams.pantufa.interactions.commands.PingExecutor
import net.perfectdreams.pantufa.interactions.commands.RegistrarExecutor
import net.perfectdreams.pantufa.interactions.commands.VIPInfoExecutor

object RegistrarCommand : SlashCommandDeclaration {
    override fun declaration() = slashCommand(
        name = "registrar",
        description = "Conecte a sua conta do Discord com a do SparklyPower para expandir a sua experiência de jogo!"
    ) {
        executor = RegistrarExecutor
    }
}