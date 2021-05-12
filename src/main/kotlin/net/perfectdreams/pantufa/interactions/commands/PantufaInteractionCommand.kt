package net.perfectdreams.pantufa.interactions.commands

import dev.kord.common.entity.MessageFlag
import dev.kord.common.entity.MessageFlags
import net.perfectdreams.discordinteraktions.commands.SlashCommand
import net.perfectdreams.discordinteraktions.context.GuildSlashCommandContext
import net.perfectdreams.discordinteraktions.context.SlashCommandContext
import net.perfectdreams.discordinteraktions.declarations.slash.SlashCommandDeclaration
import net.perfectdreams.pantufa.PantufaBot
import net.perfectdreams.pantufa.api.commands.SilentCommandException

abstract class PantufaInteractionCommand(
    val pantufa: PantufaBot,
    declaration: SlashCommandDeclaration,
    parent: SlashCommandDeclaration = declaration
) : SlashCommand(declaration, parent) {
    override suspend fun executes(context: SlashCommandContext) {
        try {
            if (context !is GuildSlashCommandContext || context.request.guildId.value?.value !in pantufa.whitelistedGuildIds) {
                context.sendMessage {
                    content = "Comandos apenas podem ser utilizados em nosso servidor oficial! https://discord.gg/sparklypower"
                    flags = MessageFlags(MessageFlag.Ephemeral)
                }
                return
            }

            executesPantufa(PantufaCommandContext(pantufa, context))
        } catch (e: SilentCommandException) {
            println("Caught *silent* cmd exception!")
        }
    }

    abstract suspend fun executesPantufa(context: PantufaCommandContext)
}