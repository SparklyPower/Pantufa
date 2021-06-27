package net.perfectdreams.pantufa.interactions.commands

import net.perfectdreams.discordinteraktions.common.context.SlashCommandContext
import net.perfectdreams.pantufa.PantufaBot
import net.perfectdreams.pantufa.api.commands.SilentCommandException
import net.perfectdreams.pantufa.commands.AbstractCommand
import net.perfectdreams.pantufa.dao.User
import net.perfectdreams.pantufa.network.Databases
import net.perfectdreams.pantufa.tables.Users
import net.perfectdreams.pantufa.utils.Constants
import net.perfectdreams.pantufa.utils.PantufaReply
import org.jetbrains.exposed.sql.transactions.transaction

class PantufaCommandContext(val pantufa: PantufaBot, val interactionContext: SlashCommandContext) {
    val sender = interactionContext.sender
    val senderId = sender.id.value

    suspend fun retrieveConnectedDiscordAccount() =
        pantufa.retrieveDiscordAccountFromUser(senderId)

    suspend fun retrieveConnectedMinecraftAccount(): AbstractCommand.MinecraftAccountInfo? {
        val discordAccount = retrieveConnectedDiscordAccount() ?: return null

        val user = transaction(Databases.sparklyPower) {
            User.find { Users.id eq discordAccount.minecraftId }.firstOrNull()
        }

        if (user == null) {
            interactionContext.sendMessage {
                content = "${Constants.ERROR} **|** <@$senderId> Parece que você tem uma conta associada, mas não existe o seu username salvo no banco de dados! Bug?"
            }
            return null
        }

        return AbstractCommand.MinecraftAccountInfo(
            discordAccount.minecraftId,
            user.username
        )
    }

    suspend fun retrieveConnectedMinecraftAccountOrFail(): AbstractCommand.MinecraftAccountInfo {
        return retrieveConnectedMinecraftAccount() ?: run {
            reply(
                PantufaReply(
                    "Você precisa associar a sua conta do SparklyPower antes de poder usar este comando! Para associar, use `-registrar NomeNoServidor`!",
                    Constants.ERROR
                )
            )

            throw SilentCommandException()
        }
    }

    suspend fun reply(vararg pantufaReplies: PantufaReply) {
        val message = StringBuilder()
        for (pantufaReply in pantufaReplies) {
            message.append(pantufaReply.build(sender.id.value) + "\n")
        }
        interactionContext.sendMessage {
            content = message.toString()
        }
    }
}