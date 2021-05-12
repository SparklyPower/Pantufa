package net.perfectdreams.pantufa.interactions.commands

import net.perfectdreams.discordinteraktions.commands.get
import net.perfectdreams.discordinteraktions.declarations.slash.SlashCommandDeclaration
import net.perfectdreams.discordinteraktions.declarations.slash.required
import net.perfectdreams.pantufa.PantufaBot
import net.perfectdreams.pantufa.api.commands.SilentCommandException
import net.perfectdreams.pantufa.dao.DiscordAccount
import net.perfectdreams.pantufa.dao.User
import net.perfectdreams.pantufa.tables.DiscordAccounts
import net.perfectdreams.pantufa.tables.Users
import net.perfectdreams.pantufa.utils.Constants
import net.perfectdreams.pantufa.utils.PantufaReply
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.deleteWhere

class RegistrarCommand(pantufa: PantufaBot) : PantufaInteractionCommand(
    pantufa,
    this
) {
    companion object : SlashCommandDeclaration(
        name = "registrar",
        description = "Conecte a sua conta do Discord com a do SparklyPower para expandir a sua experiência de jogo!"
    ) {
        override val options = Options

        object Options : SlashCommandDeclaration.Options() {
            val username = string("username", "Seu nome no SparklyPower (ou seja, da sua conta do Minecraft)")
                .required()
                .register()
        }
    }

    override suspend fun executesPantufa(context: PantufaCommandContext) {
        val arg0 = options.username.get(context.interactionContext)

        pantufa.transactionOnSparklyPowerDatabase {
            DiscordAccounts.deleteWhere { DiscordAccounts.discordId eq context.senderId }
        }

        val accountStatus = pantufa.transactionOnSparklyPowerDatabase {
            val user = User.find { Users.username eq arg0 }.firstOrNull()
                ?: return@transactionOnSparklyPowerDatabase AccountResult.UNKNOWN_PLAYER

            val connectedAccounts = DiscordAccount.find {
                DiscordAccounts.minecraftId eq user.id.value and (DiscordAccounts.isConnected eq true)
            }.count()

            if (connectedAccounts != 0L)
                return@transactionOnSparklyPowerDatabase AccountResult.ALREADY_REGISTERED

            DiscordAccount.new {
                this.minecraftId = user.id.value
                this.discordId = context.senderId
                this.isConnected = false
            }
            return@transactionOnSparklyPowerDatabase AccountResult.OK
        }

        if (accountStatus == AccountResult.UNKNOWN_PLAYER) {
            context.reply(
                PantufaReply(
                    "Usuário inexistente, você tem certeza que você colocou o nome certo?",
                    Constants.ERROR
                )
            )
            throw SilentCommandException()
        }

        if (accountStatus == AccountResult.ALREADY_REGISTERED) {
            context.reply(
                PantufaReply(
                    "A conta que você deseja conectar já tem uma conta conectada no Discord! Para desregistrar, utilize `/discord desregistrar` no servidor!",
                    Constants.ERROR
                )
            )
            throw SilentCommandException()
        }

        context.reply(
            PantufaReply(
                "Falta pouco! Para terminar a integração, vá no SparklyPower e utilize `/discord registrar` para terminar o registro!",
                "<:lori_wow:626942886432473098>"
            )
        )
    }

    enum class AccountResult {
        OK,
        UNKNOWN_PLAYER,
        ALREADY_REGISTERED
    }
}