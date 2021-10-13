package net.perfectdreams.pantufa.interactions.commands.declarations

import net.perfectdreams.discordinteraktions.declarations.commands.slash.slashCommand
import net.perfectdreams.discordinteraktions.declarations.commands.wrappers.SlashCommandDeclarationWrapper
import net.perfectdreams.pantufa.interactions.commands.OnlineExecutor

object OnlineCommand : SlashCommandDeclarationWrapper {
    override fun declaration() = slashCommand(
        name = "online",
        description = "Veja os players online no SparklyPower! Será que o seu amig@ está online?"
    ) {
        executor = OnlineExecutor
    }
}