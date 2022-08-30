package net.perfectdreams.pantufa.interactions.commands

import net.perfectdreams.discordinteraktions.common.commands.options.SlashCommandArguments
import net.perfectdreams.pantufa.PantufaBot
import net.perfectdreams.pantufa.utils.PantufaReply

class PingExecutor(pantufa: PantufaBot) : PantufaInteractionCommand(
    pantufa
) {
    override suspend fun executePantufa(context: PantufaCommandContext, args: SlashCommandArguments) {
        context.reply(
            PantufaReply(
                "Pong! \uD83C\uDFD3"
            )
        )
    }
}