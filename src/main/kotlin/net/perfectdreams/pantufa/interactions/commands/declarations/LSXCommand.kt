package net.perfectdreams.pantufa.interactions.commands.declarations

import net.perfectdreams.discordinteraktions.declarations.slash.SlashCommandDeclaration
import net.perfectdreams.discordinteraktions.declarations.slash.slashCommand
import net.perfectdreams.pantufa.interactions.commands.LSXExecutor
import net.perfectdreams.pantufa.interactions.commands.MinecraftUserDiscordUserExecutor
import net.perfectdreams.pantufa.interactions.commands.MinecraftUserPlayerNameExecutor
import net.perfectdreams.pantufa.interactions.commands.PingExecutor
import net.perfectdreams.pantufa.interactions.commands.VIPInfoExecutor

object LSXCommand : SlashCommandDeclaration {
    override fun declaration() = slashCommand(
        name = "transferir",
        description = "LorittaLand Sonhos Exchange Service: Transfira sonhos da Loritta para o SparklyPower e vice-versa!"
    ) {
        executor = LSXExecutor
    }
}