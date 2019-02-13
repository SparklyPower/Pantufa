package net.perfectdreams.pantufa.commands.server

import com.github.salomonbrys.kotson.*
import com.google.gson.JsonObject
import net.perfectdreams.pantufa.commands.AbstractCommand
import net.perfectdreams.pantufa.commands.CommandContext
import net.perfectdreams.pantufa.utils.Constants
import net.perfectdreams.pantufa.utils.PantufaReply
import net.perfectdreams.pantufa.utils.PantufaUtils
import net.perfectdreams.pantufa.utils.Server
import net.perfectdreams.pantufa.utils.socket.SocketUtils

class TpsCommand : AbstractCommand("tps") {
	override fun run(context: CommandContext) {
		val serverName = context.args.getOrNull(0)

		val server = Server.getByInternalName(serverName ?: "???")
		if (serverName == null || server == null) {
			context.reply(
					PantufaReply(
							Server.servers.joinToString(", ", transform = { it.internalName })
					)
			)
			return
		}

		val payload = server.send(
				jsonObject(
						"type" to "getTps"
				)
		)

		println(payload)

		val tps = payload["tps"].array

		context.reply(
				PantufaReply(
						"Atualmente ${server.internalName} está com ${tps[0].double} TPS!"
				)
		)
	}
}