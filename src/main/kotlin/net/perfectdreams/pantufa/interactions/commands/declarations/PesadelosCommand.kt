package net.perfectdreams.pantufa.interactions.commands.declarations

import net.perfectdreams.discordinteraktions.declarations.commands.slash.slashCommand
import net.perfectdreams.discordinteraktions.declarations.commands.wrappers.SlashCommandDeclarationWrapper
import net.perfectdreams.pantufa.interactions.commands.PesadelosExecutor

object PesadelosCommand : SlashCommandDeclarationWrapper {
    override fun declaration() = slashCommand(
        name = "pesadelos",
        description = "Veja quantos pesadelos você e outros jogadores do SparklyPower possuem"
    ) {
        executor = PesadelosExecutor
    }
}