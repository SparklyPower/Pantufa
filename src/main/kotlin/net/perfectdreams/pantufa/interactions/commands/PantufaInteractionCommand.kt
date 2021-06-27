package net.perfectdreams.pantufa.interactions.commands

import net.perfectdreams.discordinteraktions.common.commands.SlashCommandExecutor
import net.perfectdreams.discordinteraktions.common.context.GuildSlashCommandContext
import net.perfectdreams.discordinteraktions.common.context.SlashCommandArguments
import net.perfectdreams.discordinteraktions.common.context.SlashCommandContext
import net.perfectdreams.discordinteraktions.declarations.slash.SlashCommandDeclaration
import net.perfectdreams.pantufa.PantufaBot
import net.perfectdreams.pantufa.api.commands.SilentCommandException

abstract class PantufaInteractionCommand(
    val pantufa: PantufaBot
) : SlashCommandExecutor() {
    override suspend fun execute(context: SlashCommandContext, args: SlashCommandArguments) {
        try {
            if (context !is GuildSlashCommandContext || context.guild.id.value !in pantufa.whitelistedGuildIds) {
                context.sendMessage {
                    content = "Comandos apenas podem ser utilizados em nosso servidor oficial! https://discord.gg/sparklypower"
                    isEphemeral = true
                }
                return
            }

            executePantufa(PantufaCommandContext(pantufa, context), args)
        } catch (e: SilentCommandException) {
            println("Caught *silent* cmd exception!")
        }
    }

    abstract suspend fun executePantufa(context: PantufaCommandContext, args: SlashCommandArguments)
}