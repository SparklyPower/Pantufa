package net.perfectdreams.pantufa.interactions.commands.say

import dev.kord.common.entity.Snowflake
import dev.kord.common.entity.TextInputStyle
import net.perfectdreams.discordinteraktions.common.commands.SlashCommandExecutorDeclaration
import net.perfectdreams.discordinteraktions.common.commands.options.ApplicationCommandOptions
import net.perfectdreams.discordinteraktions.common.commands.options.SlashCommandArguments
import net.perfectdreams.discordinteraktions.common.modals.components.textInput
import net.perfectdreams.pantufa.PantufaBot
import net.perfectdreams.pantufa.interactions.commands.PantufaCommandContext
import net.perfectdreams.pantufa.interactions.commands.PantufaInteractionCommand

class SaySendExecutor(pantufa: PantufaBot) : PantufaInteractionCommand(pantufa) {
    companion object : SlashCommandExecutorDeclaration(SaySendExecutor::class) {
        object Options : ApplicationCommandOptions() {
            val channel = channel("channel", "Canal onde será enviado a mensagem").register()
        }

        override val options = Options
    }

    private val staffRoleId = Snowflake(332650495522897920)

    override suspend fun executePantufa(context: PantufaCommandContext, args: SlashCommandArguments) {
        if (staffRoleId !in context.interactionContext.member.roles) {
            context.sendEphemeralMessage {
                content = "<:pantufa_analise:853048446813470762> **|** Você não pode usar esse comando."
            }
            return
        }

        context.interactionContext.sendModal(
            SaySendModalSubmitExecutor,
            args[Options.channel].id.toString()
        ) {
            actionRow {
                textInput(SaySendModalSubmitExecutor.options.text, TextInputStyle.Paragraph, "Texto da Mensagem") {
                    this.placeholder = "Pantufa é muit fof"
                    this.required = true
                }
            }
        }
    }
}