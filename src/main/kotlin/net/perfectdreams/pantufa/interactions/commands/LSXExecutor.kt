package net.perfectdreams.pantufa.interactions.commands

import kotlinx.coroutines.sync.withLock
import net.perfectdreams.discordinteraktions.common.commands.SlashCommandExecutorDeclaration
import net.perfectdreams.discordinteraktions.common.commands.options.ApplicationCommandOptions
import net.perfectdreams.discordinteraktions.common.commands.options.SlashCommandArguments
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

        object Options : ApplicationCommandOptions() {
            val source = optionalString("source", "Fonte dos Sonhos")
                .choice("survival", "SparklyPower Survival")
                .choice("loritta", "Loritta :3")
                .register()

            val destination = optionalString("destination", "Destino dos Sonhos")
                .choice("survival", "SparklyPower Survival")
                .choice("loritta", "Loritta :3")
                .register()

            val quantity = optionalString("quantity", "Quantidade de sonhos que você deseja transferir")
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

        val profile = getLorittaProfile(context.senderId.toLong())
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
                    "Um sonho da `loritta` equivalem a ${LSXCommand.loriToSparklyExchangeRate} sonhos no `survival`"
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

                        if (from == LSXCommand.TransferOptions.LORITTA && to == LSXCommand.TransferOptions.PERFECTDREAMS_SURVIVAL) {
                            val sparklyPowerQuantity = quantity * LSXCommand.loriToSparklyExchangeRate

                            val fromBalance = LSXCommand.withdrawFromLoritta(
                                profile,
                                accountInfo.username,
                                accountInfo.uniqueId,
                                quantity,
                                sparklyPowerQuantity
                            )

                            if (!fromBalance) {
                                context.reply(
                                    PantufaReply(
                                        "Você não possui dinheiro suficiente em `${from.fancyName}` para transferência!",
                                        Constants.ERROR
                                    )
                                )
                                return@withLock
                            }

                            LSXCommand.giveToSparklyPower(
                                accountInfo.uniqueId,
                                sparklyPowerQuantity
                            )

                            context.reply(
                                PantufaReply(
                                    "Você transferiu **${arg2} Sonhos** (Valor final: $sparklyPowerQuantity) de `${from.fancyName}` para `${to.fancyName}`!",
                                    "\uD83D\uDCB8"
                                )
                            )
                        } else if (from == LSXCommand.TransferOptions.PERFECTDREAMS_SURVIVAL && to == LSXCommand.TransferOptions.LORITTA) {
                            val lorittaQuantity = quantity / LSXCommand.loriToSparklyExchangeRate

                            val fromBalance = LSXCommand.withdrawFromSparklyPower(
                                accountInfo.uniqueId,
                                quantity
                            )

                            if (!fromBalance) {
                                context.reply(
                                    PantufaReply(
                                        "Você não possui dinheiro suficiente em `${from.fancyName}` para transferência!",
                                        Constants.ERROR
                                    )
                                )
                                return@withLock
                            }

                            LSXCommand.giveToLoritta(
                                profile,
                                accountInfo.username,
                                accountInfo.uniqueId,
                                lorittaQuantity,
                                quantity
                            )

                            context.reply(
                                PantufaReply(
                                    "Você transferiu **${arg2} Sonhos** (Valor final: $lorittaQuantity) de `${from.fancyName}` para `${to.fancyName}`!",
                                    "\uD83D\uDCB8"
                                )
                            )
                        }
                        return@withLock
                    }
                }
            }
        }
    }
}