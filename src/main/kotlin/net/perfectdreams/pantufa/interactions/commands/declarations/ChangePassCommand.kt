package net.perfectdreams.pantufa.interactions.commands.declarations

import net.perfectdreams.discordinteraktions.common.commands.SlashCommandDeclarationWrapper
import net.perfectdreams.discordinteraktions.common.commands.slashCommand
import net.perfectdreams.pantufa.interactions.commands.ChangePassExecutor

object ChangePassCommand : SlashCommandDeclarationWrapper {
    override fun declaration() = slashCommand(
        "changepass",
        "Altera a sua senha do SparklyPower"
    ) {
        executor = ChangePassExecutor
    }
}