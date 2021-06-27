package net.perfectdreams.pantufa.interactions.commands

import net.perfectdreams.discordinteraktions.common.context.SlashCommandArguments
import net.perfectdreams.discordinteraktions.declarations.slash.SlashCommandDeclaration
import net.perfectdreams.discordinteraktions.declarations.slash.SlashCommandExecutorDeclaration
import net.perfectdreams.pantufa.PantufaBot
import net.perfectdreams.pantufa.utils.PantufaReply

class GuildsExecutor(pantufa: PantufaBot) : PantufaInteractionCommand(pantufa) {
    companion object : SlashCommandExecutorDeclaration(GuildsExecutor::class)

    override suspend fun executePantufa(context: PantufaCommandContext, args: SlashCommandArguments) {
        context.reply(
            PantufaReply(
                pantufa.jda.guilds.map { it.name }.joinToString()
            )
        )
    }
}