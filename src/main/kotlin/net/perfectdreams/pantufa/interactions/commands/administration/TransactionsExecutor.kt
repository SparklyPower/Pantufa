package net.perfectdreams.pantufa.interactions.commands.administration

import net.perfectdreams.discordinteraktions.common.commands.SlashCommandExecutorDeclaration
import net.perfectdreams.discordinteraktions.common.commands.options.ApplicationCommandOptions
import net.perfectdreams.discordinteraktions.common.commands.options.SlashCommandArguments
import net.perfectdreams.pantufa.PantufaBot
import net.perfectdreams.pantufa.dao.Transaction
import net.perfectdreams.pantufa.interactions.commands.PantufaCommandContext
import net.perfectdreams.pantufa.interactions.commands.PantufaInteractionCommand
import net.perfectdreams.pantufa.interactions.components.utils.*
import net.perfectdreams.pantufa.network.Databases
import net.perfectdreams.pantufa.utils.extensions.uuid
import org.jetbrains.exposed.sql.transactions.transaction

class TransactionsExecutor(pantufa: PantufaBot) : PantufaInteractionCommand(pantufa) {
    companion object : SlashCommandExecutorDeclaration(TransactionsExecutor::class) {
        object Options : ApplicationCommandOptions() {
            val currency = optionalString("currency", "Nome da moeda").apply {
                choice("MONEY", "Sonecas")
                choice("CASH", "Pesadelos")
            }.register()
            val payer = optionalString("source", "Nome do usuário de onde o dinheiro saiu").register()
            val receiver = optionalString("destination", "Nome do usuário que recebeu o dinheiro").register()
            val page = optionalInteger("page", "A página que você quer visualizar").register()
        }

        override val options = Options
    }

    override suspend fun executePantufa(context: PantufaCommandContext, args: SlashCommandArguments) {
        val selfId = pantufa.retrieveDiscordAccountFromUser(context.sender.id.value.toLong())?.minecraftId

        val payer = args[options.payer]?.uuid()
        val receiver = args[options.receiver]?.uuid()
        val currency = args[options.currency]?.let(TransactionCurrency::valueOf)

        /**
         * If the user has a connected Minecraft Account and does not specify either payer or receiver, we will
         * just fetch any transactions that they are a part of
         */
        val fetchedTransactions =
            if (selfId != null && payer == null && receiver == null)
                Transaction.fetchTransactionsFromSingleUser(selfId, currency)
            else
                Transaction.fetchTransactions(payer, receiver, currency)

        val size = transaction(Databases.sparklyPower) { fetchedTransactions.count() }

        val page = args[options.page]?.let {
            if (it < 1 || it * MessagePanelType.TRANSACTIONS.entriesPerPage > size) return context.reply(invalidPageMessage)
            it - 1
        } ?: 0

        val arguments = mutableListOf<String>().apply {
            args[options.payer]?.let { add("<:lori_card:956406538887634985> **Remetente**: `$it`") }
            args[options.receiver]?.let { add("<:pantufa_coffee:853048446981111828> **Destinatário**: `$it`") }
            currency?.let { add(":coin: **Moeda**: `${currency.displayName.replaceFirstChar { it.uppercase() }}`") }
        }

        val messageData = saveAndCreateData(
            size,
            context.sender.id,
            MessagePanelType.TRANSACTIONS,
            fetchedTransactions,
            arguments
        ).apply {
            minecraftId = selfId
            options = Triple(payer, receiver, currency)
        }

        context.interactionContext.sendMessage {
            messageData.buildTransactionsMessage(page, selfId).let {
                embeds = it.embeds
                components = it.components
            }
        }
    }
}
