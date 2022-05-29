package net.perfectdreams.pantufa.interactions.components

import net.perfectdreams.discordinteraktions.common.components.ButtonClickExecutorDeclaration
import net.perfectdreams.discordinteraktions.common.components.ButtonClickWithDataExecutor
import net.perfectdreams.discordinteraktions.common.components.ComponentContext
import net.perfectdreams.discordinteraktions.common.entities.User
import net.perfectdreams.pantufa.interactions.components.utils.*
import net.perfectdreams.pantufa.pantufa

class ChangePageButtonClickExecutor : ButtonClickWithDataExecutor {
    companion object : ButtonClickExecutorDeclaration("0001")

    override suspend fun onClick(user: User, context: ComponentContext, data: String) {
        val minecraftId = pantufa.retrieveDiscordAccountFromUser(user.id.value.toLong())?.minecraftId

        val decoded = data.decoded?.also {
            if (it.first.userId != user.id) return context.notForYou()
        } ?: return context.invalid()

        val messageData = decoded.first
        val page = decoded.second

        context.updateMessage(
            if (messageData.type == MessagePanelType.TRANSACTIONS)
                messageData.buildTransactionsMessage(page, minecraftId)
            else
                messageData.buildCommandsLogMessage(page)
        )
    }
}