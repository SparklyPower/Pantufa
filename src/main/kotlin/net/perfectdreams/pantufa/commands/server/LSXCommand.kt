package net.perfectdreams.pantufa.commands.server

import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import net.perfectdreams.loritta.cinnamon.common.utils.SparklyPowerLSXTransactionEntryAction
import net.perfectdreams.loritta.cinnamon.pudding.tables.SonhosTransactionsLog
import net.perfectdreams.loritta.cinnamon.pudding.tables.SparklyPowerLSXSonhosTransactionsLog
import net.perfectdreams.pantufa.commands.AbstractCommand
import net.perfectdreams.pantufa.commands.CommandContext
import net.perfectdreams.pantufa.dao.Ban
import net.perfectdreams.pantufa.dao.Profile
import net.perfectdreams.pantufa.network.Databases
import net.perfectdreams.pantufa.tables.*
import net.perfectdreams.pantufa.utils.*
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.Instant
import java.util.*

class LSXCommand : AbstractCommand("transferir", listOf("transfer", "lsx", "llsx", "lsxs", "llsxs"), requiresMinecraftAccount = true) {
	companion object {
		val mutex = Mutex()
		var loriToSparklyExchangeRate = 2L

		private fun getLorittaProfile(context: CommandContext): Profile {
			return transaction(Databases.loritta) {
				Profile.findById(context.discordAccount!!.discordId)
			} ?: throw RuntimeException()
		}

		fun withdrawFromLoritta(profile: Profile, playerName: String, playerUniqueId: UUID, quantity: Long, sparklyPowerQuantity: Long): Boolean {
			return if (quantity > profile.money)
				false
			else {
				transaction(Databases.loritta) {
					Profiles.update({ Profiles.id eq profile.userId }) {
						with (SqlExpressionBuilder) {
							it.update(Profiles.money, Profiles.money - quantity)
						}
					}

					val now = Instant.now()

					val timestampLogId = SonhosTransactionsLog.insertAndGetId {
						it[SonhosTransactionsLog.user] = profile.id
						it[SonhosTransactionsLog.timestamp] = now
					}

					SparklyPowerLSXSonhosTransactionsLog.insert {
						it[SparklyPowerLSXSonhosTransactionsLog.timestampLog] = timestampLogId
						it[SparklyPowerLSXSonhosTransactionsLog.action] = SparklyPowerLSXTransactionEntryAction.EXCHANGED_TO_SPARKLYPOWER
						it[SparklyPowerLSXSonhosTransactionsLog.sonhos] = quantity
						it[SparklyPowerLSXSonhosTransactionsLog.sparklyPowerSonhos] = sparklyPowerQuantity
						it[SparklyPowerLSXSonhosTransactionsLog.playerName] = playerName
						it[SparklyPowerLSXSonhosTransactionsLog.playerUniqueId] = playerUniqueId
						it[SparklyPowerLSXSonhosTransactionsLog.exchangeRate] = loriToSparklyExchangeRate.toDouble()
					}
				}
				true
			}
		}

		fun withdrawFromSparklyPower(playerUniqueId: UUID, quantity: Long): Boolean {
			val serverAccountId = CraftConomyUtils.getCraftConomyAccountId(playerUniqueId)!!
			val ccBalance = CraftConomyUtils.getCraftConomyBalance(serverAccountId)

			return if (quantity > ccBalance)
				false
			else {
				transaction(Databases.craftConomy) {
					CraftConomyBalance.update({ CraftConomyBalance.id eq serverAccountId }) {
						with(SqlExpressionBuilder) {
							it.update(balance, balance - quantity.toDouble())
						}
					}
				}
				true
			}
		}

		fun giveToLoritta(profile: Profile, playerName: String, playerUniqueId: UUID, quantity: Long, sparklyPowerQuantity: Long): Boolean? {
			transaction(Databases.loritta) {
				Profiles.update({ Profiles.id eq profile.userId }) {
					with (SqlExpressionBuilder) {
						it.update(Profiles.money, Profiles.money + quantity)
					}
				}

				val now = Instant.now()

				val timestampLogId = SonhosTransactionsLog.insertAndGetId {
					it[SonhosTransactionsLog.user] = profile.id
					it[SonhosTransactionsLog.timestamp] = now
				}

				SparklyPowerLSXSonhosTransactionsLog.insert {
					it[SparklyPowerLSXSonhosTransactionsLog.timestampLog] = timestampLogId
					it[SparklyPowerLSXSonhosTransactionsLog.action] = SparklyPowerLSXTransactionEntryAction.EXCHANGED_FROM_SPARKLYPOWER
					it[SparklyPowerLSXSonhosTransactionsLog.sonhos] = quantity
					it[SparklyPowerLSXSonhosTransactionsLog.sparklyPowerSonhos] = sparklyPowerQuantity
					it[SparklyPowerLSXSonhosTransactionsLog.playerName] = playerName
					it[SparklyPowerLSXSonhosTransactionsLog.playerUniqueId] = playerUniqueId
					it[SparklyPowerLSXSonhosTransactionsLog.exchangeRate] = loriToSparklyExchangeRate.toDouble()
				}
			}
			return true
		}

		fun giveToSparklyPower(playerUniqueId: UUID, quantity: Long): Boolean {
			val serverAccountId = CraftConomyUtils.getCraftConomyAccountId(playerUniqueId)!!

			transaction(Databases.craftConomy) {
				CraftConomyBalance.update({ CraftConomyBalance.id eq serverAccountId }) {
					with(SqlExpressionBuilder) {
						it.update(balance, balance + quantity.toDouble())
					}
				}
			}
			return true
		}
	}

	override fun run(context: CommandContext) {
		val arg0 = context.args.getOrNull(0)
		val arg1 = context.args.getOrNull(1)
		val arg2 = context.args.getOrNull(2)

		val profile = getLorittaProfile(context)
		val bannedState = profile.getBannedState()

		if (bannedState != null) {
			context.reply(
				PantufaReply(
					"Você está banido de utilizar a Loritta!"
				)
			)
			return
		}

		// Check if the user is banned
		val userBan = transaction(Databases.sparklyPower) {
			Ban.find {
				Bans.player eq context.minecraftAccountInfo!!.uniqueId and
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
				ChatUsers._id eq context.minecraftAccountInfo!!.uniqueId
			}.firstOrNull()
		}

		if (chatUser == null || (86400 * 20) >= chatUser[ChatUsers.playOneMinute] ?: 0) {
			context.sendMessage(
				PantufaReply(
					"Você precisa ter mais de 24 horas online no SparklyPower antes de poder transferir sonhos!",
					"\uD83D\uDCB5"
				)
			)
			return
		}

		if (arg0 == null) {
			val serverAccountId = CraftConomyUtils.getCraftConomyAccountId(context)

			val ccBalance = CraftConomyUtils.getCraftConomyBalance(serverAccountId)

			context.sendMessage(
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
					"Um sonho da `loritta` equivalem a $loriToSparklyExchangeRate sonhos no `survival`"
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
				runBlocking {
					mutex.withLock {
						val from = TransferOptions.values().firstOrNull { it.codename == arg0 }
						val to = TransferOptions.values().firstOrNull { it.codename == arg1 }

						if (from != null && to != null && arg2 != null) {
							val quantity = NumberUtils.convertShortenedNumberToLong(arg2)

							if (quantity == null) {
								context.sendMessage(
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

							if (from == TransferOptions.LORITTA && to == TransferOptions.PERFECTDREAMS_SURVIVAL) {
								val sparklyPowerQuantity = quantity * loriToSparklyExchangeRate

								val fromBalance = withdrawFromLoritta(
									profile,
									context.minecraftAccountInfo!!.username,
									context.minecraftAccountInfo!!.uniqueId,
									quantity,
									sparklyPowerQuantity
								)

								if (!fromBalance) {
									context.sendMessage(
										PantufaReply(
											"Você não possui dinheiro suficiente em `${from.fancyName}` para transferência!",
											Constants.ERROR
										)
									)
									return@withLock
								}

								giveToSparklyPower(
									context.minecraftAccountInfo!!.uniqueId,
									sparklyPowerQuantity
								)

								context.sendMessage(
									PantufaReply(
										"Você transferiu **${arg2} Sonhos** (Valor final: $sparklyPowerQuantity) de `${from.fancyName}` para `${to.fancyName}`!",
										"\uD83D\uDCB8"
									)
								)
							} else if (from == TransferOptions.PERFECTDREAMS_SURVIVAL && to == TransferOptions.LORITTA) {
								val lorittaQuantity = quantity / loriToSparklyExchangeRate

								val fromBalance = withdrawFromSparklyPower(
									context.minecraftAccountInfo!!.uniqueId,
									quantity
								)

								if (!fromBalance) {
									context.sendMessage(
										PantufaReply(
											"Você não possui dinheiro suficiente em `${from.fancyName}` para transferência!",
											Constants.ERROR
										)
									)
									return@withLock
								}

								giveToLoritta(
									profile,
									context.minecraftAccountInfo!!.username,
									context.minecraftAccountInfo!!.uniqueId,
									lorittaQuantity,
									quantity
								)

								context.sendMessage(
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

	enum class TransferOptions(val fancyName: String, val codename: String) {
		LORITTA("Loritta", "loritta"),
		PERFECTDREAMS_SURVIVAL("SparklyPower Survival", "survival")
	}
}