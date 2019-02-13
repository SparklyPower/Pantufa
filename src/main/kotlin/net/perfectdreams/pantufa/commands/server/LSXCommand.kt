package net.perfectdreams.pantufa.commands.server

import com.github.kevinsawicki.http.HttpRequest
import com.github.salomonbrys.kotson.nullString
import com.github.salomonbrys.kotson.obj
import com.github.salomonbrys.kotson.set
import com.google.gson.JsonObject
import net.perfectdreams.pantufa.commands.AbstractCommand
import net.perfectdreams.pantufa.commands.CommandContext
import net.perfectdreams.pantufa.jsonParser
import net.perfectdreams.pantufa.utils.Constants
import net.perfectdreams.pantufa.utils.PantufaReply
import net.perfectdreams.pantufa.utils.socket.SocketUtils

class LSXCommand : AbstractCommand("transferir", listOf("transfer", "lsx", "llsx", "lsxs", "llsxs"), requiresMinecraftAccount = true) {
	override fun run(context: CommandContext) {
		if (true)
			return

		val arg0 = context.args.getOrNull(0)
		val arg1 = context.args.getOrNull(1)
		val arg2 = context.args.getOrNull(2)

		if (arg0 == null) {
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
							"*Locais disponíveis para transferência...*",
							mentionUser = false
					),
					PantufaReply(
							"Loritta `loritta`",
							"<:sparklyPower:331179879582269451>",
							mentionUser = false
					),
					PantufaReply(
							"SparklyPower Survival `survival`",
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

					val toBalance = give(to, context, quantity)

					context.sendMessage(
							PantufaReply(
									"Você transferiu **${arg2} Sonhos** de `${from.fancyName}` para `${to.fancyName}`!",
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
				val json = JsonObject()
				json["userId"] = context.user.id
				json["quantity"] = quantity
				json["reason"] = "Transferindo do SparklyPower - ${context.minecraftUsername}/${context.user.id}"
				json["guildId"] = context.event.guild.id
				val balanceBody = HttpRequest.get("https://loritta.website/api/v1/economy/withdraw-balance")
						.userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:60.0) Gecko/20100101 Firefox/60.0")
						.header("Lori-Authentication", "dummy")
						.send(json.toString())
						.body()

				val payload = jsonParser.parse(balanceBody).obj
				println(payload)
				return payload["api:message"].nullString != "INSUFFICIENT_FUNDS"
			}
			TransferOptions.PERFECTDREAMS_SURVIVAL -> {
				val jsonObject = JsonObject()
				jsonObject["type"] = "withdrawBalance"
				jsonObject["player"] = context.minecraftUsername
				jsonObject["quantity"] = quantity
				val response = SocketUtils.send(jsonObject, port = Constants.PERFECTDREAMS_SURVIVAL_PORT)
				println(response)
				return response["api:message"].nullString != "INSUFFICIENT_FUNDS"
			}
			else -> null
		}
	}

	fun give(option: TransferOptions, context: CommandContext, quantity: Double): Boolean? {
		return when (option) {
			TransferOptions.LORITTA -> {
				val json = JsonObject()
				json["userId"] = context.user.id
				json["quantity"] = quantity
				val balanceBody = HttpRequest.get("https://loritta.website/api/v1/economy/give-balance")
						.userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:60.0) Gecko/20100101 Firefox/60.0")
						.header("Lori-Authentication", "dummy")
						.send(json.toString())
						.body()

				val payload = jsonParser.parse(balanceBody).obj
				println(payload)
				return true
			}
			TransferOptions.PERFECTDREAMS_SURVIVAL -> {
				val jsonObject = JsonObject()
				jsonObject["type"] = "giveBalance"
				jsonObject["player"] = context.minecraftUsername
				jsonObject["quantity"] = quantity
				val response = SocketUtils.send(jsonObject, port = Constants.PERFECTDREAMS_SURVIVAL_PORT)
				println(response)
				return true
			}
			else -> null
		}
	}

	enum class TransferOptions(val fancyName: String, val codename: String) {
		LORITTA("Loritta", "sparklyPower"),
		PERFECTDREAMS_SURVIVAL("SparklyPower Survival", "survival")
	}
}