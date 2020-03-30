package net.perfectdreams.pantufa

import com.github.salomonbrys.kotson.*
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import net.dv8tion.jda.api.*
import net.dv8tion.jda.api.entities.Activity
import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.internal.entities.EntityBuilder
import net.perfectdreams.pantufa.commands.CommandManager
import net.perfectdreams.pantufa.dao.DiscordAccount
import net.perfectdreams.pantufa.listeners.DiscordListener
import net.perfectdreams.pantufa.network.Databases
import net.perfectdreams.pantufa.tables.DiscordAccounts
import net.perfectdreams.pantufa.tables.Users
import net.perfectdreams.pantufa.threads.CheckDreamPresenceThread
import net.perfectdreams.pantufa.threads.SyncRolesThread
import net.perfectdreams.pantufa.utils.Constants
import net.perfectdreams.pantufa.utils.PantufaConfig
import net.perfectdreams.pantufa.utils.Server
import net.perfectdreams.pantufa.utils.parallax.ParallaxEmbed
import net.perfectdreams.pantufa.utils.socket.SocketHandler
import net.perfectdreams.pantufa.utils.socket.SocketServer
import net.perfectdreams.pantufa.utils.webhook.DiscordMessage
import net.perfectdreams.pantufa.utils.webhook.DiscordWebhook
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import java.awt.image.BufferedImage
import java.util.concurrent.Executors
import kotlin.concurrent.thread

class Pantufa(val config: PantufaConfig) {
	companion object {
		const val PREFIX = "-"
		lateinit var INSTANCE: Pantufa
	}

	val commandManager = CommandManager()
	val executors = Executors.newCachedThreadPool()
	lateinit var jda: JDA

	val temporaryPanelaChannels = mutableListOf<PanelaChannel>()

	fun start() {
		INSTANCE = this
		println("Iniciando Pantufa...")

		initPostgreSql()

		jda = JDABuilder(AccountType.BOT)
				.setStatus(OnlineStatus.ONLINE)
				.setToken(config.token)
				.build()

		val entityBuilder = EntityBuilder(jda)

		println("Olá, mundo!")

		SyncRolesThread().start()
		CheckDreamPresenceThread().start()

		thread {
			var idx = 0
			while (true) {
				try {
					val response = Server.PERFECTDREAMS_BUNGEE.send(
							jsonObject("type" to "getOnlinePlayers")
					)
					val plural = if (response["players"].array.size() == 1) "" else "s"
					println("Player Count: ${response["players"].array.size()}")

					val prefix = when (response["players"].array.size()) {
						in 45 until 50 -> "\uD83D\uDE18"
						in 40 until 45 -> "\uD83D\uDE0E"
						in 35 until 40 -> "\uD83D\uDE06"
						in 30 until 35 -> "\uD83D\uDE04"
						in 25 until 30 -> "\uD83D\uDE03"
						in 20 until 25 -> "\uD83D\uDE0B"
						in 15 until 20 -> "\uD83D\uDE09"
						in 10 until 15 -> "\uD83D\uDE43"
						in 5 until 10 -> "\uD83D\uDE0A"
						in 1 until 5 -> "\uD83D\uDE42"
						0 -> "\uD83D\uDE34"
						else -> "\uD83D\uDE0D"
					}

					if (idx % 2 == 0) {
						jda.presence.activity = Activity.watching("$prefix ${response["players"].array.size()} player$plural online no SparklyPower! | \uD83C\uDFAE mc.sparklypower.net")
					} else {
						jda.presence.activity = Activity.playing("A Loritta é a minha melhor amiga!")
					}

					idx++
				} catch (e: Exception) {
					e.printStackTrace()

					jda.presence.activity = Activity.playing("\uD83D\uDEAB SparklyPower está offline \uD83D\uDE2D | \uD83C\uDFAE mc.sparklypower.net")
				}
				Thread.sleep(15000) // rate limit de presence é 5 a cada 60 segundos
			}
		}

		thread {
			val socket = SocketServer(60799)
			socket.socketHandler = object: SocketHandler {
				override fun onSocketReceived(json: JsonObject, response: JsonObject) {
					println("RECEIVED: " + json)
					val type = json["type"].nullString ?: return

					println("Type: $type")
					if (type == "sendMessage") {
						val textChannelId = json["textChannelId"].string
						val message = json["message"].string
						val base64Image = json["image"].nullString

						val bufferedImage: BufferedImage? = null
						if (base64Image != null) {
							// TODO: Parse image
						}

						val messageBuilder = MessageBuilder()
								.setContent(message)

						val embed = json["embed"].nullObj

						if (embed != null) {
							val parallaxEmbed = gson.fromJson<ParallaxEmbed>(embed)

							val discordEmbed = parallaxEmbed.toDiscordEmbed(true) // tá safe

							messageBuilder.setEmbed(discordEmbed)
						}

						val textChannel = jda.getTextChannelById(textChannelId)

						println("textChannelId: ${textChannelId}")

						textChannel?.sendMessage(messageBuilder.build())?.complete()
						return
					}
					if (type == "panelinhaPrivateMessage") {
						val tag = json["panelinha"].string
						val username = json["username"].string
						val content = json["content"].string

						val panelaChannel = temporaryPanelaChannels.firstOrNull { it.cleanTag == tag } ?: return

						val textChannelId = panelaChannel.textChannelId
						val textChannel = jda.getTextChannelById(textChannelId) ?: return

						val webhook = textChannel.retrieveWebhooks().complete().firstOrNull() ?: textChannel.createWebhook("$tag Relay").complete()
						val url = webhook.url
						val webhookClient = DiscordWebhook(url)
						webhookClient.send(
								DiscordMessage(
										username = username,
										avatar = "https://i.imgur.com/QDntdpA.png",
										content = content
								)
						)
					}
					if (type == "sendEventStart") {
						val guild = Constants.SPARKLYPOWER_GUILD
						if (guild != null) {
							val roleId = json["roleId"].string
							val channelId = json["channelId"].string
							val eventName = json["eventName"].string

							val role = guild.getRoleById(roleId) ?: return
							val channel = guild.getTextChannelById(channelId) ?: return

							channel.sendMessage("${role.asMention} Evento $eventName irá iniciar em 60 segundos!").queue()
						}
					}
				}
			}
			socket.start()
		}

		jda.addEventListener(DiscordListener(this))
	}

	fun initPostgreSql() {
		transaction(Databases.sparklyPower) {
			SchemaUtils.createMissingTablesAndColumns(
					DiscordAccounts,
					Users
			)
		}
	}

	fun getDiscordAccountFromUser(user: User): DiscordAccount? {
		return getDiscordAccountFromId(user.idLong)
	}

	fun getDiscordAccountFromId(id: Long): DiscordAccount? {
		return transaction(Databases.sparklyPower) {
			DiscordAccount.find { DiscordAccounts.discordId eq id }.firstOrNull()
		}
	}

	/* fun getDiscordAccountFromUsername(username: String): DiscordAccount? {
		return discordAccounts.find(
				Filters.and(
						Filters.eq(
								"minecraftUsername", username
						),
						Filters.eq(
								"connected", true
						)
				)
		).firstOrNull()
	} */

	class PanelaChannel(val voiceChannelId: String, val textChannelId: String, val cleanTag: String)
}

val pantufa get() = Pantufa.INSTANCE
val jda get() = Pantufa.INSTANCE.jda
val jsonParser = JsonParser()
val gson = Gson()