package net.perfectdreams.pantufa.interactions.commands.declarations

import net.perfectdreams.discordinteraktions.declarations.slash.SlashCommandDeclaration
import net.perfectdreams.discordinteraktions.declarations.slash.slashCommand
import net.perfectdreams.pantufa.interactions.commands.OnlineExecutor
import net.perfectdreams.pantufa.interactions.commands.PingExecutor
import net.perfectdreams.pantufa.interactions.commands.VIPInfoExecutor

object OnlineCommand : SlashCommandDeclaration {
    override fun declaration() = slashCommand(
        name = "online",
        description = "Veja os players online no SparklyPower! Será que o seu amig@ está online?"
    ) {
        executor = OnlineExecutor
    }
}