package net.perfectdreams.pantufa.interactions.commands

import kotlinx.coroutines.sync.withLock
import net.perfectdreams.discordinteraktions.common.context.SlashCommandArguments
import net.perfectdreams.discordinteraktions.declarations.slash.SlashCommandExecutorDeclaration
import net.perfectdreams.discordinteraktions.declarations.slash.options.CommandOptions
import net.perfectdreams.pantufa.PantufaBot
import net.perfectdreams.pantufa.commands.server.LSXCommand
import net.perfectdreams.pantufa.dao.Ban
import net.perfectdreams.pantufa.dao.Profile
import net.perfectdreams.pantufa.network.Databases
import net.perfectdreams.pantufa.tables.Bans
import net.perfectdreams.pantufa.tables.ChatUsers
import net.perfectdreams.pantufa.utils.Constants
import net.perfectdreams.pantufa.utils.CraftConomyUtils
import net.perfectdreams.pantufa.utils.NumberUtils
import net.perfectdreams.pantufa.utils.PantufaReply
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.or
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction

class LSXExecutor(pantufa: PantufaBot) : PantufaInteractionCommand(
    pantufa
) {
    companion object : SlashCommandExecutorDeclaration(LSXExecutor::class) {
        override val options = Options

        object Options : CommandOptions() {
            val source = string("source", "Fonte dos Sonhos")
                .choice("survival", "SparklyPower Survival")
                .choice("loritta", "Loritta :3")
                .register()

            val destination = string("destination", "Destino dos Sonhos")
                .choice("survival", "SparklyPower Survival")
                .choice("loritta", "Loritta :3")
                .register()

            val quantity = string("quantity", "Quantidade de sonhos que você deseja transferir")
                .register()
        }
    }

    private fun getLorittaProfile(userId: Long): Profile {
        return transaction(Databases.loritta) {
            Profile.findById(userId)
        } ?: throw RuntimeException()
    }

    override suspend fun executePantufa(context: PantufaCommandContext, args: SlashCommandArguments) {
        val arg0 = args[options.source]
        val arg1 = args[options.destination]
        val arg2 = args[options.quantity]

        val profile = getLorittaProfile(context.senderId)
        val bannedState = profile.getBannedState()

        if (bannedState != null) {
            context.reply(
                PantufaReply(
                    "Você está banido de utilizar a Loritta!"
                )
            )
            return
        }

        val accountInfo = context.retrieveConnectedMinecraftAccountOrFail()

        // Check if the user is banned
        val userBan = transaction(Databases.sparklyPower) {
            Ban.find {
                Bans.player eq accountInfo.uniqueId and
                        (
                                Bans.temporary eq false or (
                                        Bans.temporary eq true and
                                                (Bans.expiresAt.isNotNull()) and
                                                (Bans.expiresAt greaterEq System.currentTimeMillis())
                                        )
                                )
            }.firstOrNull()
        }

        if (userBan != null) {
            context.reply(
                PantufaReply(
                    "Você está banido do SparklyPower!"
                )
            )
            return
        }

        val chatUser = transaction(Databases.sparklyPower) {
            ChatUsers.select {
                ChatUsers._id eq accountInfo.uniqueId
            }.firstOrNull()
        }

        if (chatUser == null || (86400 * 20) >= chatUser[ChatUsers.playOneMinute] ?: 0) {
            context.reply(
                PantufaReply(
                    "Você precisa ter mais de 24 horas online no SparklyPower antes de poder transferir sonhos!",
                    "\uD83D\uDCB5"
                )
            )
            return
        }

        if (arg0 == null) {
            val serverAccountId = CraftConomyUtils.getCraftConomyAccountId(accountInfo.uniqueId)!!
            val ccBalance = CraftConomyUtils.getCraftConomyBalance(serverAccountId)

            context.reply(
                PantufaReply(
                    "**LorittaLand Sonhos Exchange Service (LSX)**",
                    "\uD83D\uDCB5"
                ),
                PantufaReply(
                    "`-transferir Fonte Destino Quantidade`",
                    mentionUser = false
                ),
                PantufaReply(
                    "**Câmbio de Sonhos:**"
                ),
                PantufaReply(
                    "Um sonho da `loritta` equivalem a ${LSXCommand.loriToSparklyExchangeTax} sonhos no `survival`"
                ),
                PantufaReply(
                    "*Locais disponíveis para transferência...*",
                    mentionUser = false
                ),
                PantufaReply(
                    "**Loritta** `loritta` (*${profile.money} sonhos*)",
                    "<:sparklyPower:331179879582269451>",
                    mentionUser = false
                ),
                PantufaReply(
                    "**SparklyPower Survival** `survival` (*$ccBalance sonhos*)",
                    "<:pocketdreams:333655151871000576>",
                    mentionUser = false
                )
            )
            return
        } else {
            if (arg1 != null) {
                LSXCommand.mutex.withLock {
                    val from = LSXCommand.TransferOptions.values().firstOrNull { it.codename == arg0 }
                    val to = LSXCommand.TransferOptions.values().firstOrNull { it.codename == arg1 }

                    if (from != null && to != null && arg2 != null) {
                        val quantity = NumberUtils.convertShortenedNumberToLong(arg2)

                        if (quantity == null) {
                            context.reply(
                                PantufaReply(
                                    "Quantidade inválida!",
                                    Constants.ERROR
                                )
                            )
                            return@withLock
                        }

                        if (from == to)
                            return@withLock

                        if (0 >= quantity)
                            return@withLock

                        val fromBalance = LSXCommand.withdraw(from, profile, accountInfo.uniqueId, quantity)

                        if (fromBalance == null) {
                            context.reply(
                                PantufaReply(
                                    "Atualmente nós não suportamos transferências de `${from.fancyName}`...",
                                    Constants.ERROR
                                )
                            )
                            return@withLock
                        }

                        if (!fromBalance) {
                            context.reply(
                                PantufaReply(
                                    "Você não possui dinheiro suficiente em `${from.fancyName}` para transferência!",
                                    Constants.ERROR
                                )
                            )
                            return@withLock
                        }

                        val correctGivenBalance = if (from == LSXCommand.TransferOptions.LORITTA && to == LSXCommand.TransferOptions.PERFECTDREAMS_SURVIVAL) {
                            quantity * LSXCommand.loriToSparklyExchangeTax
                        } else {
                            quantity / LSXCommand.loriToSparklyExchangeTax
                        }

                        val toBalance = LSXCommand.give(to, profile, accountInfo.uniqueId, correctGivenBalance)

                        context.reply(
                            PantufaReply(
                                "Você transferiu **${arg2} Sonhos** (Valor final: $correctGivenBalance) de `${from.fancyName}` para `${to.fancyName}`!",
                                "\uD83D\uDCB8"
                            )
                        )
                        return@withLock
                    }
                }
            }
        }
    }
}