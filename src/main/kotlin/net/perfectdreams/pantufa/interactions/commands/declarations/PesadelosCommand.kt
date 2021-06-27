package net.perfectdreams.pantufa.interactions.commands.declarations

import net.perfectdreams.discordinteraktions.declarations.slash.SlashCommandDeclaration
import net.perfectdreams.discordinteraktions.declarations.slash.slashCommand
import net.perfectdreams.pantufa.interactions.commands.MoneyExecutor
import net.perfectdreams.pantufa.interactions.commands.PesadelosExecutor
import net.perfectdreams.pantufa.interactions.commands.PingExecutor
import net.perfectdreams.pantufa.interactions.commands.VIPInfoExecutor

object PesadelosCommand : SlashCommandDeclaration {
    override fun declaration() = slashCommand(
        name = "pesadelos",
        description = "Veja quantos pesadelos você e outros jogadores do SparklyPower possuem"
    ) {
        executor = PesadelosExecutor
    }
}