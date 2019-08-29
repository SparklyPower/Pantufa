package net.perfectdreams.pantufa.commands.server

import com.github.kevinsawicki.http.HttpRequest
import com.github.salomonbrys.kotson.nullString
import com.github.salomonbrys.kotson.obj
import com.github.salomonbrys.kotson.set
import com.google.gson.JsonObject
import net.perfectdreams.pantufa.commands.AbstractCommand
import net.perfectdreams.pantufa.commands.CommandContext
import net.perfectdreams.pantufa.dao.Profile
import net.perfectdreams.pantufa.jsonParser
import net.perfectdreams.pantufa.network.Databases
import net.perfectdreams.pantufa.pantufa
import net.perfectdreams.pantufa.tables.CraftConomyAccounts
import net.perfectdreams.pantufa.tables.CraftConomyBalance
import net.perfectdreams.pantufa.utils.Constants
import net.perfectdreams.pantufa.utils.PantufaReply
import net.perfectdreams.pantufa.utils.socket.SocketUtils
import org.jetbrains.exposed.sql.SqlExpressionBuilder
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update

class LSXCommand : AbstractCommand("transferir", listOf("transfer", "lsx", "llsx", "lsxs", "llsxs"), requiresMinecraftAccount = true) {
	private fun getLorittaProfile(context: CommandContext): Profile {
		return transaction(Databases.loritta) {
			Profile.findById(context.discordAccount!!.discordId)
		} ?: throw RuntimeException()
	}

	private fun getCraftConomyAccountId(context: CommandContext): Int {
		return transaction(Databases.craftConomy) {
			CraftConomyAccounts.select {
				CraftConomyAccounts.uuid eq context.discordAccount!!.minecraftId.toString()
			}.firstOrNull()?.get(CraftConomyAccounts.id)
		} ?: throw RuntimeException()
	}

	private fun getCraftConomyBalance(accountId: Int): Double {
		return transaction(Databases.craftConomy) {
			CraftConomyBalance.select {
				CraftConomyBalance.id eq accountId
			}.firstOrNull()?.get(CraftConomyBalance.balance)
		} ?: throw RuntimeException()
	}

	override fun run(context: CommandContext) {
		val arg0 = context.args.getOrNull(0)
		val arg1 = context.args.getOrNull(1)
		val arg2 = context.args.getOrNull(2)

		if (arg0 == null) {
			val profile = getLorittaProfile(context)

			val serverAccountId = getCraftConomyAccountId(context)

			val ccBalance = getCraftConomyBalance(serverAccountId)

			context.sendMessage(
					PantufaReply(
							"**LorittaLand Sonhos Exchange Service (LSX)**",
							"\uD83D\uDCB5"
					),
					PantufaReply(
							"`-transferir Fonte Destinação Quantidade`",
							mentionUser = false
					),
					PantufaReply(
							"**Câmbio de Sonhos:**"
					),
					PantufaReply(
							"Um sonho da `loritta` equivalem a três sonhos no `survival`"
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
				val from = TransferOptions.values().firstOrNull { it.codename == arg0 }
				val to = TransferOptions.values().firstOrNull { it.codename == arg1 }

				if (from != null && to != null && arg2 != null) {
					val quantity = arg2.toDouble()

					if (from == to)
						return

					if (quantity.isInfinite() || quantity.isNaN())
						return

					if (0 >= quantity)
						return

					val fromBalance = withdraw(from, context, quantity)

					if (fromBalance == null) {
						context.sendMessage(
								PantufaReply(
										"Atualmente nós não suportamos transferências de `${from.fancyName}`...",
										Constants.ERROR
								)
						)
						return
					}

					if (!fromBalance) {
						context.sendMessage(
								PantufaReply(
										"Você não possui dinheiro suficiente em `${from.fancyName}` para transferência!",
										Constants.ERROR
								)
						)
						return
					}

					val correctGivenBalance = if (from == TransferOptions.LORITTA && to == TransferOptions.PERFECTDREAMS_SURVIVAL) {
						quantity * 3
					} else {
						quantity / 3
					}

					val toBalance = give(to, context, correctGivenBalance)

					context.sendMessage(
							PantufaReply(
									"Você transferiu **${arg2} Sonhos** (Valor final: $correctGivenBalance) de `${from.fancyName}` para `${to.fancyName}`!",
									"\uD83D\uDCB8"
							)
					)
					return
				}
			}
		}
	}

	fun withdraw(option: TransferOptions, context: CommandContext, quantity: Double): Boolean? {
		return when (option) {
			TransferOptions.LORITTA -> {
				val profile = getLorittaProfile(context)

				return if (quantity > profile.money)
					false
				else {
					transaction(Databases.loritta) {
						profile.money -= quantity
					}
					true
				}
			}
			TransferOptions.PERFECTDREAMS_SURVIVAL -> {
				val serverAccountId = getCraftConomyAccountId(context)
				val ccBalance = getCraftConomyBalance(serverAccountId)

				return if (quantity > ccBalance)
					false
				else {
					transaction(Databases.craftConomy) {
						CraftConomyBalance.update({ CraftConomyBalance.id eq serverAccountId }) {
							with(SqlExpressionBuilder) {
								it.update(balance, balance - quantity)
							}
						}
					}
					true
				}
			}
			else -> null
		}
	}

	fun give(option: TransferOptions, context: CommandContext, quantity: Double): Boolean? {
		return when (option) {
			TransferOptions.LORITTA -> {
				val profile = getLorittaProfile(context)

				transaction(Databases.loritta) {
					profile.money += quantity
				}
				return true
			}
			TransferOptions.PERFECTDREAMS_SURVIVAL -> {
				val serverAccountId = getCraftConomyAccountId(context)

				transaction(Databases.craftConomy) {
					CraftConomyBalance.update({ CraftConomyBalance.id eq serverAccountId }) {
						with(SqlExpressionBuilder) {
							it.update(balance, balance + quantity)
						}
					}
				}
				return true
			}
			else -> null
		}
	}

	enum class TransferOptions(val fancyName: String, val codename: String) {
		LORITTA("Loritta", "loritta"),
		PERFECTDREAMS_SURVIVAL("SparklyPower Survival", "survival")
	}
}