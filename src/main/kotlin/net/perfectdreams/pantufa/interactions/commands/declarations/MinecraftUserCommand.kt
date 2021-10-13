package net.perfectdreams.pantufa.interactions.commands.declarations

import net.perfectdreams.discordinteraktions.declarations.commands.slash.slashCommand
import net.perfectdreams.discordinteraktions.declarations.commands.wrappers.SlashCommandDeclarationWrapper
import net.perfectdreams.pantufa.interactions.commands.MinecraftUserDiscordUserExecutor
import net.perfectdreams.pantufa.interactions.commands.MinecraftUserPlayerNameExecutor

object MinecraftUserCommand : SlashCommandDeclarationWrapper {
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