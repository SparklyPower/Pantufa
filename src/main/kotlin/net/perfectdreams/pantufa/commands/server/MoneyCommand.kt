package net.perfectdreams.pantufa.commands.server

import com.github.salomonbrys.kotson.*
import com.google.gson.JsonObject
import net.perfectdreams.pantufa.commands.AbstractCommand
import net.perfectdreams.pantufa.commands.CommandContext
import net.perfectdreams.pantufa.utils.Constants
import net.perfectdreams.pantufa.utils.PantufaReply
import net.perfectdreams.pantufa.utils.PantufaUtils
import net.perfectdreams.pantufa.utils.socket.SocketUtils

class MoneyCommand : AbstractCommand("money", requiresMinecraftAccount = true) {
	override fun run(context: CommandContext) {
		/* val arg0 = context.args.getOrNull(0)

		if (arg0 == "balance") {
			var arg1 = PantufaUtils.getUsernameFromContext(context, 1)

			if (arg1 != null) {
				getBalance(context, arg1)
				return
			}
		}
		if (arg0 == "pay") {
			val to = PantufaUtils.getUsernameFromContext(context, 1)
			val quantity = context.args.getOrNull(2)
			if (to != null && quantity != null) {
				val _quantity = quantity.toDoubleOrNull()

				if (_quantity == null)
					return

				pay(context, context.minecraftUsername!!, to, _quantity)
				return
			}
		}

		getBalance(context, context.minecraftUsername!!) */
	}

	fun getBalance(context: CommandContext, username: String) {
		val jsonObject = JsonObject()
		jsonObject["type"] = "getBalance"
		jsonObject["player"] = username

		SocketUtils.sendAsync(jsonObject, port = Constants.PERFECTDREAMS_SURVIVAL_PORT, success = {
			response ->
			if (username == context.minecraftAccountInfo!!.username) {
				context.reply(
						PantufaReply(
								message = "Você possui **${response["balance"].double} Sonhos**!",
								prefix = "\uD83D\uDCB5"
						)
				)
			} else {
				context.reply(
						PantufaReply(
								message = "**`${username}`** possui **${response["balance"].double} Sonhos**!",
								prefix = "\uD83D\uDCB5"
						)
				)
			}
		}, error = { Constants.PERFECTDREAMS_OFFLINE.invoke(context) })
	}

	fun pay(context: CommandContext, from: String, to: String, balance: Double) {
		val jsonObject = JsonObject()
		jsonObject["type"] = "transferBalance"
		jsonObject["from"] = from
		jsonObject["to"] = to
		jsonObject["quantity"] = balance
		val response = SocketUtils.send(jsonObject, port = Constants.PERFECTDREAMS_SURVIVAL_PORT)

		if (response.contains("api:code")) {
			val error = response["api:code"].int

			if (error == 1) {
				context.reply(
						PantufaReply(
								message = "Você não tem dinheiro suficiente para realizar esta transação... \uD83E\uDD37",
								prefix = Constants.ERROR
						)
				)
				return
			}
			// return
		}

		context.reply(
				PantufaReply(
						message = "Você transferiu **${balance} Sonhos** para **`${to}`**! Agora você tem **${response["fromBalance"].double} Sonhos**!",
						prefix = "\uD83D\uDCB8"
				)
		)
	}
}