package net.perfectdreams.pantufa.commands.server

import com.github.salomonbrys.kotson.array
import com.github.salomonbrys.kotson.jsonObject
import com.github.salomonbrys.kotson.string
import net.perfectdreams.pantufa.commands.AbstractCommand
import net.perfectdreams.pantufa.commands.CommandContext
import net.perfectdreams.pantufa.network.Databases
import net.perfectdreams.pantufa.tables.LuckPermsPlayers
import net.perfectdreams.pantufa.utils.PantufaReply
import net.perfectdreams.pantufa.utils.Server
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction

open class RemoteCommandExecutorCommand(label: String,
										aliases:List<String> = listOf(),
										val commandToBeExecuted: String,
										val server: Server
) : AbstractCommand(label, aliases, true) {
	override fun run(context: CommandContext) {
		if (context.minecraftAccountInfo == null)
			return

		val entry = transaction(Databases.sparklyPowerLuckPerms) {
			LuckPermsPlayers.select {
				LuckPermsPlayers.id eq context.minecraftAccountInfo.uniqueId.toString()
			}.firstOrNull()
		} ?: run {
			context.reply(
					PantufaReply(
							"eh mole, você não existe na db do LuckPerms!"
					)
			)
			return
		}

		if (entry[LuckPermsPlayers.primaryGroup] !in arrayOf("dono", "admin", "moderador")) {
			context.reply(
					PantufaReply(
							"Você por o acaso tem permissão para fazer isso? Não, né. Então pare de fazer perder meu tempo! Seu grupo é ${entry[LuckPermsPlayers.primaryGroup]}"
					)
			)
			return
		}

		val payload = server.send(
				jsonObject(
						"type" to "executeCommand",
						"player" to context.minecraftAccountInfo.username,
						"command" to "$commandToBeExecuted " + context.args.joinToString(" ")
				)
		)

		val messages = payload["messages"].array
		var isFirst = true
		val replies = messages.map {
			val reply = PantufaReply(it.string, mentionUser = isFirst)
			isFirst = false
			reply
		}

		context.reply(
				*replies.toTypedArray()
		)
	}
}