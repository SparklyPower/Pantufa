package net.perfectdreams.pantufa.interactions.commands.declarations

import net.perfectdreams.discordinteraktions.declarations.slash.SlashCommandDeclaration
import net.perfectdreams.discordinteraktions.declarations.slash.slashCommand
import net.perfectdreams.pantufa.interactions.commands.MinecraftUserDiscordUserExecutor
import net.perfectdreams.pantufa.interactions.commands.MinecraftUserPlayerNameExecutor
import net.perfectdreams.pantufa.interactions.commands.PingExecutor
import net.perfectdreams.pantufa.interactions.commands.VIPInfoExecutor

object MinecraftUserCommand : SlashCommandDeclaration {
    override fun declaration() = slashCommand(
        name = "mcuser",
        description = "Veja a conta associada ao SparklyPower de um usuário"
    ) {
        subcommand("player", "Veja a conta associada ao SparklyPower pelo nome de um player no SparklyPower") {
            executor = MinecraftUserPlayerNameExecutor
        }

        subcommand("user", "Veja a conta associada ao SparklyPower pela conta no Discord") {
            executor = MinecraftUserDiscordUserExecutor
        }
    }
}