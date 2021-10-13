package net.perfectdreams.pantufa.interactions.commands.declarations

import net.perfectdreams.discordinteraktions.declarations.commands.slash.slashCommand
import net.perfectdreams.discordinteraktions.declarations.commands.wrappers.SlashCommandDeclarationWrapper
import net.perfectdreams.pantufa.interactions.commands.MoneyExecutor

object MoneyCommand : SlashCommandDeclarationWrapper {
    override fun declaration() = slashCommand(
        name = "money",
        description = "Veja quantos sonhos você e outros jogadores do SparklyPower possuem"
    ) {
        executor = MoneyExecutor
    }
}