package net.perfectdreams.pantufa.listeners

import com.github.salomonbrys.kotson.jsonObject
import com.github.salomonbrys.kotson.set
import com.google.gson.JsonObject
import net.dv8tion.jda.api.events.guild.GuildBanEvent
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceLeaveEvent
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import net.perfectdreams.pantufa.PantufaBot
import net.perfectdreams.pantufa.dao.User
import net.perfectdreams.pantufa.jda
import net.perfectdreams.pantufa.network.Databases
import net.perfectdreams.pantufa.utils.Constants
import net.perfectdreams.pantufa.utils.Server
import net.perfectdreams.pantufa.utils.extensions.await
import net.perfectdreams.pantufa.utils.socket.SocketUtils
import org.jetbrains.exposed.sql.transactions.transaction

class DiscordListener(val m: PantufaBot) : ListenerAdapter() {
	override fun onGuildBan(event: GuildBanEvent) {
		m.launch {
			val user = m.getDiscordAccountFromUser(event.user) ?: return@launch
			val sparklyUsername = transaction(Databases.sparklyPower) { User.findById(user.minecraftId)?.username } ?: return@launch

			val userBan = try {
				event.guild.retrieveBan(event.user)
						.await()
			} catch (e: Exception) { return@launch }

			Server.PERFECTDREAMS_BUNGEE.send(
					jsonObject(
							"type" to "executeCommand",
							"player" to "Pantufa",
							"command" to "ban $sparklyUsername Banido no Discord do SparklyPower: ${userBan.reason}"
					)
			)
		}
	}

	override fun onMessageReceived(event: MessageReceivedEvent) {
		m.launch {
			if (m.commandMap.dispatch(event))
				return@launch

			for (command in m.commandManager.commands)
				if (command.matches(event))
					return@launch
		}

		// Se nenhum comando vanilla foi executado...
		if (false && event.message.contentDisplay.startsWith(PantufaBot.PREFIX)) { // E inicia com o prefixo...
			// hora de tentar executar um comando do servidor!
			event.textChannel.sendTyping().queue()

			val jsonObject = JsonObject()
			jsonObject["type"] = "executeCommand"

			jsonObject["username"] = "MrPowerGamerBR"
			jsonObject["userId"] = event.author.id
			jsonObject["textChannelId"] = event.textChannel.id
			jsonObject["guildId"] = event.guild.id
			jsonObject["command"] = event.message.contentDisplay.replaceFirst("-", "")

			SocketUtils.send(jsonObject, port = 60801)
		}
	}

	override fun onGuildMessageReceived(event: GuildMessageReceivedEvent) {
		if (event.author.isBot)
			return

		if (event.channel.id == "417059128519819265") {
			val payload = JsonObject()
			payload["type"] = "sendAdminChat"
			payload["player"] = event.author.name
			payload["message"] = event.message.contentRaw

			SocketUtils.sendAsync(payload, host = Constants.PERFECTDREAMS_BUNGEE_IP, port = Constants.PERFECTDREAMS_BUNGEE_PORT)
		}
		/* val panelaChannel = m.temporaryPanelaChannels.firstOrNull { it.textChannelId == event.channel.id }
		if (panelaChannel != null) {
			val cleanTag = panelaChannel.cleanTag

			val minecraftUsername = pantufa.getDiscordAccountFromUser(event.author)?.minecraftUsername ?: return

			val payload = JsonObject()
			payload["type"] = "panelinhaPrivateMessage"
			payload["panelinha"] = cleanTag
			payload["username"] = minecraftUsername
			payload["content"] = event.message.contentDisplay

			SocketUtils.sendAsync(payload, port = 60801)
		} */
	}

	/* override fun onGuildVoiceJoin(event: GuildVoiceJoinEvent) {
		if (event.channelJoined.id != "392344970998317057")
			return

		val minecraftUsername = m.getDiscordAccountFromUser(event.member.user)?.minecraftUsername

		if (minecraftUsername == null) {
			println("Usuário não possui conta associada!")
			return
		}

		m.executors.execute {
			val panelinha = m.databaseSurvival.getCollection("panelinhas")
					.find(
							Filters.eq(
									"members.player", minecraftUsername
							)
					).firstOrNull()

			if (panelinha == null) {
				println("Usuário não possui uma panelinah associada!")
				return@execute
			}

			val cleanTag = panelinha.getString("_id")
			val name = panelinha.getString("name")

			println("Criando panela de $name")

			val members = panelinha["members"] as List<Document>

			val nicks = members.map { it.getString("player") }

			val category = event.guild.getCategoryById("378314817519353856")

			val voiceChannelAction = category
					.createVoiceChannel("\uD83C\uDF72 | «$name»")
			val textChannelAction = category
					.createTextChannel(name.toLowerCase().replace(Regex("[^A-Za-z0-9 ]"), "").replace(" ", "-"))

			voiceChannelAction.addPermissionOverride(event.guild.publicRole, 0, Permission.ALL_VOICE_PERMISSIONS)
			voiceChannelAction.addPermissionOverride(event.guild.publicRole, listOf(), listOf(Permission.VIEW_CHANNEL))
			textChannelAction.addPermissionOverride(event.guild.publicRole, 0, Permission.ALL_TEXT_PERMISSIONS)
			textChannelAction.addPermissionOverride(event.guild.publicRole, listOf(), listOf(Permission.VIEW_CHANNEL))

			val messageBuilder = MessageBuilder()

			for (nick in nicks) {
				val id = m.getDiscordAccountFromUsername(nick)?.userId ?: continue
				val member = event.guild.getMemberById(id) ?: continue

				messageBuilder.append(member)
				voiceChannelAction.addPermissionOverride(member, listOf(Permission.VOICE_CONNECT, Permission.VOICE_SPEAK, Permission.VOICE_USE_VAD, Permission.VIEW_CHANNEL), listOf())
				textChannelAction.addPermissionOverride(member, listOf(Permission.VIEW_CHANNEL, Permission.MESSAGE_ADD_REACTION, Permission.MESSAGE_ATTACH_FILES, Permission.MESSAGE_EMBED_LINKS, Permission.MESSAGE_HISTORY, Permission.MESSAGE_READ, Permission.MESSAGE_WRITE), listOf())
			}

			val voiceChannel = voiceChannelAction.complete() as VoiceChannel
			val textChannel = textChannelAction.complete() as TextChannel

			val panelaChannel = Pantufa.PanelaChannel(voiceChannel.id, textChannel.id, cleanTag)

			m.temporaryPanelaChannels.add(panelaChannel)

			event.guild.controller.moveVoiceMember(event.member, voiceChannel).complete()

			// Hora de enviar o bulletin board!
			if (panelinha.containsKey("bulletinBoard")) {
				val bulletinBoard = panelinha["bulletinBoard"] as List<Document>

				if (bulletinBoard.isNotEmpty()) {
					var bulletinBoardAsText = ""
					for (bbEntry in bulletinBoard) {
						val content = bbEntry.getString("content")

						if (bbEntry.containsKey("name")) {
							val name = bbEntry.getString("name")
							bulletinBoardAsText += "\uD83D\uDDE3 **`$name`** » $content\n"
						} else {
							bulletinBoardAsText += "\uD83D\uDC81 $content\n"
						}
					}

					textChannel.sendFile(File("D:\\Pictures\\PocketDreams\\Pantufa\\bulletin_board_header.png"), messageBuilder.build()).complete()
					textChannel.sendMessage(bulletinBoardAsText).complete()
				}
			}
		}
	} */

	override fun onGuildVoiceLeave(event: GuildVoiceLeaveEvent) {
		val panelaChannel = m.temporaryPanelaChannels.firstOrNull { it.voiceChannelId == event.channelLeft.id }
		if (event.channelLeft.members.isEmpty() && panelaChannel != null) {
			m.temporaryPanelaChannels.remove(panelaChannel)
			// Remover canal de voz criado
			event.channelLeft.delete().complete()

			// Remover canal de texto criado
			val textChannelId = panelaChannel.textChannelId
			val textChannel = jda.getTextChannelById(textChannelId) ?: return
			textChannel.delete().complete()
		}
	}
}