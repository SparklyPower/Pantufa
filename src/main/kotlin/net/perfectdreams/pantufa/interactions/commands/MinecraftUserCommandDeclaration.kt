package net.perfectdreams.pantufa.interactions.commands

import net.perfectdreams.discordinteraktions.declarations.slash.SlashCommandDeclaration
import net.perfectdreams.discordinteraktions.declarations.slash.required

object MinecraftUserCommandDeclaration {
    object Root : SlashCommandDeclaration(
        name = "mcuser",
        description = "Veja a conta associada ao SparklyPower de um usuário"
    ) {
        override val options = Options

        object Options : SlashCommandDeclaration.Options() {
            val minecraftPlayer = subcommand(MinecraftPlayer)
                .register()

            val discordUser = subcommand(DiscordUser)
                .register()
        }
    }

    object MinecraftPlayer : SlashCommandDeclaration(
        name = "player",
        description = "Veja a conta associada ao SparklyPower pelo nome de um player no SparklyPower"
    ) {
        override val options = Options

        object Options : SlashCommandDeclaration.Options() {
            val playerName = string("player_name", "Nome do Player")
                .required()
                .register()
        }
    }

    object DiscordUser : SlashCommandDeclaration(
        name = "user",
        description = "Veja a conta associada ao SparklyPower pela conta no Discord"
    ) {
        override val options = Options

        object Options : SlashCommandDeclaration.Options() {
            val user = user("user", "Usuário no Discord")
                .required()
                .register()
        }
    }
}