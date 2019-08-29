package net.perfectdreams.pantufa.commands.server

import com.github.salomonbrys.kotson.*
import com.google.gson.JsonObject
import net.perfectdreams.pantufa.commands.AbstractCommand
import net.perfectdreams.pantufa.commands.CommandContext
import net.perfectdreams.pantufa.utils.Constants
import net.perfectdreams.pantufa.utils.PantufaReply
import net.perfectdreams.pantufa.utils.socket.SocketUtils

class OnlineCommand : AbstractCommand("online") {
	override fun run(context: CommandContext) {
		val jsonObject = JsonObject()
		jsonObject["type"] = "getOnlinePlayersInfo"
		val response = SocketUtils.sendAsync(jsonObject, port = Constants.PERFECTDREAMS_BUNGEE_PORT, success = { response ->
			val servers = response["servers"].array

			val replies = mutableListOf<PantufaReply>()

			val totalPlayersOnline = servers.sumBy {
				it["players"].array.size()
			}
			replies.add(
					PantufaReply(
							message = "**Players Online no SparklyPower Network ($totalPlayersOnline players online)**",
							prefix = "<:pocketdreams:333655151871000576>"
					)
			)
			servers.forEach {
				val obj = it.obj
				val name = obj["name"].string
				val players = obj["players"].array.map {
					it["name"].string
				}

				if (players.isNotEmpty()) {
					replies.add(
							PantufaReply(
									"`${name}` (${players.size}): ${players.joinToString(", ", transform = { "**`$it`**" })}",
									mentionUser = false
							)
					)
				} else {
					replies.add(
							PantufaReply(
									"`${name}` (0): Ninguém online... \uD83D\uDE2D",
									mentionUser = false
							)
					)
				}
			}

			context.sendMessage(*replies.toTypedArray())
		}, error = { Constants.PERFECTDREAMS_OFFLINE.invoke(context) })
	}
}