package net.perfectdreams.pantufa.interactions.commands.say

import dev.kord.common.entity.Snowflake
import net.perfectdreams.discordinteraktions.common.modals.ModalSubmitContext
import net.perfectdreams.discordinteraktions.common.modals.ModalSubmitExecutorDeclaration
import net.perfectdreams.discordinteraktions.common.modals.ModalSubmitWithDataExecutor
import net.perfectdreams.discordinteraktions.common.modals.components.ModalArguments
import net.perfectdreams.discordinteraktions.common.modals.components.ModalComponents
import net.perfectdreams.pantufa.PantufaBot

class SayEditModalSubmitExecutor(val m: PantufaBot) : ModalSubmitWithDataExecutor {
    companion object : ModalSubmitExecutorDeclaration("say_edit") {
        object Options : ModalComponents() {
            val text = textInput("text")
                .register()
        }

        override val options = Options
    }

    override suspend fun onModalSubmit(context: ModalSubmitContext, args: ModalArguments, data: String) {
        val (channelIdAsString, messageIdAsString) = data.split(":")

        m.rest.channel.editMessage(
            Snowflake(channelIdAsString),
            Snowflake(messageIdAsString)
        ) {
            content = args[options.text]
        }

        context.sendEphemeralMessage {
            content = "Mensagem Editada!"
        }
    }
}