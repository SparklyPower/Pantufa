package net.perfectdreams.pantufa.interactions.commands.declarations

import net.perfectdreams.discordinteraktions.common.commands.SlashCommandDeclarationWrapper
import net.perfectdreams.discordinteraktions.common.commands.slashCommand
import net.perfectdreams.pantufa.interactions.commands.CommandsLogExecutor

object CommandsLogCommand : SlashCommandDeclarationWrapper {
    override fun declaration() = slashCommand(
        "commands",
        "Lista dos comandos usados com base nos critérios escolhidos"
    ) {
        subcommand("log", "Lista dos comandos usados com base nos critérios escolhidos") {
            executor = CommandsLogExecutor
        }
    }
}