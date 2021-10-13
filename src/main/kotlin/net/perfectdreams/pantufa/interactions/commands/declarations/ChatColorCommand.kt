package net.perfectdreams.pantufa.interactions.commands.declarations

import net.perfectdreams.discordinteraktions.declarations.commands.slash.slashCommand
import net.perfectdreams.discordinteraktions.declarations.commands.wrappers.SlashCommandDeclarationWrapper
import net.perfectdreams.pantufa.interactions.commands.ChatColorExecutor

object ChatColorCommand : SlashCommandDeclarationWrapper {
    override fun declaration() = slashCommand(
        "chatcolor",
        "Transforme uma cor RGB em uma cor que você possa usar no chat (e em outros lugares) do SparklyPower!"
    ) {
        executor = ChatColorExecutor
    }
}