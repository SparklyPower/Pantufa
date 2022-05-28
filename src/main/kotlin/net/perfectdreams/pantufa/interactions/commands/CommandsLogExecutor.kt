package net.perfectdreams.pantufa.interactions.commands

import net.perfectdreams.discordinteraktions.common.commands.SlashCommandExecutorDeclaration
import net.perfectdreams.discordinteraktions.common.commands.options.ApplicationCommandOptions
import net.perfectdreams.discordinteraktions.common.commands.options.SlashCommandArguments
import net.perfectdreams.pantufa.PantufaBot
import net.perfectdreams.pantufa.dao.Command
import net.perfectdreams.pantufa.interactions.components.utils.MessagePanelType
import net.perfectdreams.pantufa.interactions.components.utils.buildCommandsLogMessage
import net.perfectdreams.pantufa.interactions.components.utils.invalidPageMessage
import net.perfectdreams.pantufa.interactions.components.utils.saveAndCreateData
import org.jetbrains.exposed.sql.transactions.transaction

class CommandsLogExecutor(pantufa: PantufaBot) : PantufaInteractionCommand(pantufa) {
    companion object : SlashCommandExecutorDeclaration(CommandsLogExecutor::class) {
        object Options : ApplicationCommandOptions() {
            val player = optionalString("player", "Nome do jogador").register()
            val world = optionalString("world", "Mundo em que o comando foi usado").register()
            val alias = optionalString("alias", "Comando usado").register()
            val page = optionalInteger("page", "A página que você quer visualizar").register()
            var args = optionalString("args", "Argumentos usados dentro do comando").register()
        }

        override val options = Options
    }

    override suspend fun executePantufa(context: PantufaCommandContext, args: SlashCommandArguments) {
        val fetchedCommands = Command.fetchCommands(
            args[options.player],
            args[options.world],
            args[options.alias],
            args[options.args]
        )

        val size = transaction { fetchedCommands.count() }

        val page = args[options.page]?.let {
            if (it < 1 || it * MessagePanelType.COMMANDS_LOG.entriesPerPage > size) return context.reply(invalidPageMessage)
            it - 1
        } ?: 0

        val arguments = mutableListOf<String>().apply {
            args[options.player]?.let { add(":person_bowing: **Jogador**: `$it`") }
            args[options.world]?.let { add(":earth_americas: **Mundo**: `$it`") }
            args[options.alias]?.let { add(":computer: **Comando**: `/$it`") }
            args[options.args]?.let { add(":pencil: **Argumentos**: `$it`") }
        }

        val messageData = saveAndCreateData(
            size,
            context.sender.id,
            MessagePanelType.COMMANDS_LOG,
            fetchedCommands,
            arguments
        )

        context.sendMessage {
            messageData.buildCommandsLogMessage(page).let {
                embeds = it.embeds
                components = it.components
            }
        }
    }
}