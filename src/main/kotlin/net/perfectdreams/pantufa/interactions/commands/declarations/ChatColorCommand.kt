package net.perfectdreams.pantufa.interactions.commands.declarations

import net.perfectdreams.discordinteraktions.declarations.slash.SlashCommandDeclaration
import net.perfectdreams.discordinteraktions.declarations.slash.slashCommand
import net.perfectdreams.pantufa.interactions.commands.ChatColorExecutor

object ChatColorCommand : SlashCommandDeclaration {
    override fun declaration() = slashCommand(
        "chatcolor",
        "Transforme uma cor RGB em uma cor que você possa usar no chat (e em outros lugares) do SparklyPower!"
    ) {
        executor = ChatColorExecutor
    }
}