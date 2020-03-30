package net.perfectdreams.pantufa.commands.server

import com.github.salomonbrys.kotson.*
import com.google.gson.JsonObject
import net.perfectdreams.pantufa.commands.AbstractCommand
import net.perfectdreams.pantufa.commands.CommandContext
import net.perfectdreams.pantufa.utils.Constants
import net.perfectdreams.pantufa.utils.CraftConomyUtils
import net.perfectdreams.pantufa.utils.PantufaReply
import net.perfectdreams.pantufa.utils.PantufaUtils
import net.perfectdreams.pantufa.utils.socket.SocketUtils

class MoneyCommand : AbstractCommand("money", requiresMinecraftAccount = true) {
	override fun run(context: CommandContext) {
		val serverAccountId = CraftConomyUtils.getCraftConomyAccountId(context)
		val ccBalance = CraftConomyUtils.getCraftConomyBalance(serverAccountId)

		context.reply(
				PantufaReply(
						message = "Você possui **${ccBalance} Sonhos**!",
						prefix = "\uD83D\uDCB5"
				)
		)
	}
}