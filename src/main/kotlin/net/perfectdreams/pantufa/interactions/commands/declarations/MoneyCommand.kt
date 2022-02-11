package net.perfectdreams.pantufa.interactions.commands.declarations

import net.perfectdreams.discordinteraktions.common.commands.SlashCommandDeclarationWrapper
import net.perfectdreams.discordinteraktions.common.commands.slashCommand
import net.perfectdreams.pantufa.interactions.commands.MoneyExecutor

object MoneyCommand : SlashCommandDeclarationWrapper {
    override fun declaration() = slashCommand(
        name = "money",
        description = "Veja quantas sonecas você e outros jogadores do SparklyPower possuem"
    ) {
        executor = MoneyExecutor
    }
}