package net.perfectdreams.pantufa.interactions.commands.declarations

import net.perfectdreams.discordinteraktions.common.commands.SlashCommandDeclarationWrapper
import net.perfectdreams.discordinteraktions.common.commands.slashCommand
import net.perfectdreams.pantufa.PantufaBot
import net.perfectdreams.pantufa.interactions.commands.PesadelosExecutor

class PesadelosCommand(val m: PantufaBot)  : SlashCommandDeclarationWrapper {
    override fun declaration() = slashCommand(
        name = "pesadelos",
        description = "Veja quantos pesadelos você e outros jogadores do SparklyPower possuem"
    ) {
        executor = PesadelosExecutor(m)
    }
}