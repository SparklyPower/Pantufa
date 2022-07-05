package net.perfectdreams.pantufa.interactions.commands.say

import net.perfectdreams.discordinteraktions.common.commands.MessageCommandDeclarationWrapper
import net.perfectdreams.discordinteraktions.common.commands.messageCommand

object SayEditMessageCommand : MessageCommandDeclarationWrapper {
    override fun declaration() = messageCommand("Editar Mensagem", SayEditMessageExecutor)
}