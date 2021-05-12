package net.perfectdreams.pantufa

import com.github.salomonbrys.kotson.*
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import dev.kord.common.entity.Snowflake
import kotlinx.coroutines.*
import net.dv8tion.jda.api.*
import net.dv8tion.jda.api.entities.Activity
import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.requests.GatewayIntent
import net.perfectdreams.pantufa.commands.CommandManager
import net.perfectdreams.pantufa.commands.server.*
import net.perfectdreams.pantufa.commands.vanilla.utils.PingCommand
import net.perfectdreams.pantufa.dao.DiscordAccount
import net.perfectdreams.pantufa.listeners.DiscordListener
import net.perfectdreams.pantufa.network.Databases
import net.perfectdreams.pantufa.tables.DiscordAccounts
import net.perfectdreams.pantufa.tables.NotifyPlayersOnline
import net.perfectdreams.pantufa.tables.Users
import net.perfectdreams.pantufa.threads.CheckDreamPresenceThread
import net.perfectdreams.pantufa.threads.SyncRolesThread
import net.perfectdreams.pantufa.utils.Constants
import net.perfectdreams.pantufa.utils.Server
import net.perfectdreams.pantufa.utils.discord.DiscordCommandMap
import net.perfectdreams.pantufa.utils.parallax.ParallaxEmbed
import net.perfectdreams.pantufa.utils.socket.SocketHandler
import net.perfectdreams.pantufa.utils.socket.SocketServer
import net.perfectdreams.pantufa.utils.webhook.DiscordMessage
import net.perfectdreams.pantufa.utils.webhook.DiscordWebhook
import net.perfectdreams.discordinteraktions.InteractionsServer
import net.perfectdreams.pantufa.utils.config.PantufaConfig
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.Transaction
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import java.awt.image.BufferedImage
import java.time.Instant
import java.util.*
import java.util.concurrent.Executors
import kotlin.concurrent.thread


class PantufaBot(val config: PantufaConfig) {
	companion object {
		const val PREFIX = "-"
		lateinit var INSTANCE: PantufaBot
	}

	val commandManager = CommandManager()
	val commandMap = DiscordCommandMap(this)
	val executors = Executors.newCachedThreadPool()
	val coroutineDispatcher = executors.asCoroutineDispatcher()
	lateinit var jda: JDA

	val temporaryPanelaChannels = mutableListOf<PanelaChannel>()
	var previousPlayers: List<String>? = null
	val whitelistedGuildIds = listOf(
		265632341530116097L, // SparklyPower (Old)
		268353819409252352L, // Ideias Aleatórias
		297732013006389252L, // Apartamento da Loritta
		602640402830589954L, // Furdúncios Artísticos
		320248230917046282, // SparklyPower (New)
		420626099257475072L, // Loritta's Apartment
		340165396692729883L, // Loritta's Emoji Server
		538506322212159578L, // Loritta's Emoji Server 2
		392482696720416775L, // Pantufa's Emoji Server
		626604568741937199L // Loritta's Emoji Server 4
	)

	fun start() {
		INSTANCE = this
		println("Iniciando Pantufa...")

		initPostgreSql()

		jda = JDABuilder.create(EnumSet.allOf(GatewayIntent::class.java))
			.setStatus(OnlineStatus.ONLINE)
			.setToken(config.token)
			.build()

		commandMap.register(PingCommand.create(this))
		commandMap.register(MinecraftUserCommand.create(this))
		commandMap.register(NotificarPlayerCommand.create(this))
		commandMap.register(NotificarEventoCommand.create(this))
		commandMap.register(ChatColorCommand.create(this))
		commandMap.register(VerificarStatusCommand.create(this))
		commandMap.register(PesadelosCommand.create(this))
		commandMap.register(VIPInfoCommand.create(this))
		commandMap.register(MoneyCommand.create(this))
		commandMap.register(SugerirCommand.create(this))

		SyncRolesThread().start()
		CheckDreamPresenceThread().start()
		val interactionsServer = InteractionsServer(
			390927821997998081L,
			config.discordInteractions.publicKey,
			config.token
		)

		interactionsServer.commandManager.commands.addAll(
			listOf(
				net.perfectdreams.pantufa.interactions.commands.ChatColorCommand(this),
				net.perfectdreams.pantufa.interactions.commands.LSXCommand(this),
				net.perfectdreams.pantufa.interactions.commands.MinecraftUserPlayerNameCommand(this),
				net.perfectdreams.pantufa.interactions.commands.MinecraftUserDiscordUserCommand(this),
				net.perfectdreams.pantufa.interactions.commands.MoneyCommand(this),
				net.perfectdreams.pantufa.interactions.commands.OnlineCommand(this),
				net.perfectdreams.pantufa.interactions.commands.PesadelosCommand(this),
				net.perfectdreams.pantufa.interactions.commands.PingCommand(this),
				net.perfectdreams.pantufa.interactions.commands.RegistrarCommand(this),
				net.perfectdreams.pantufa.interactions.commands.VIPInfoCommand(this),
				net.perfectdreams.pantufa.interactions.commands.GuildsCommand(this),
			)
		)

		runBlocking {
			if (config.discordInteractions.registerGlobally) {
				interactionsServer.commandManager.updateAllGlobalCommands(
					deleteUnknownCommands = true
				)
			} else {
				for (id in config.discordInteractions.guildsToBeRegistered) {
					interactionsServer.commandManager.updateAllCommandsInGuild(
						Snowflake(268353819409252352L),
						deleteUnknownCommands = true
					)
				}
			}
		}

		thread {
			interactionsServer.start()
		}

		thread {
			var idx = 0
			while (true) {
				try {
					val response = Server.PERFECTDREAMS_BUNGEE.send(
						jsonObject("type" to "getOnlinePlayers")
					)
					val plural = if (response["players"].array.size() == 1) "" else "s"
					println("Player Count: ${response["players"].array.size()}")

					val oldPreviousPlayers = previousPlayers
					val currentPlayers = response["players"].array.map { it.string }
					if (oldPreviousPlayers != null) {
						val joinedPlayers = currentPlayers.toMutableList().also { it.removeAll(oldPreviousPlayers) }
						println("Newly joined players: $joinedPlayers")

						for (joinedPlayer in joinedPlayers) {
							val uniqueId = UUID.nameUUIDFromBytes("OfflinePlayer:$joinedPlayer".toByteArray())

							val trackedEntries = transaction(Databases.sparklyPower) {
								NotifyPlayersOnline.select { NotifyPlayersOnline.tracked eq uniqueId }
									.toList()
							}

							println("Users tracking ${joinedPlayer} ($uniqueId): $trackedEntries")

							for (trackedEntry in trackedEntries) {
								val minecraftUser = getMinecraftUserFromUniqueId(trackedEntry[NotifyPlayersOnline.player])

								if (minecraftUser == null) {
									println("There is a $trackedEntry, but there isn't a minecraft user!")
									continue
								}

								if (minecraftUser.username in currentPlayers) {
									println("There is a $trackedEntry, but the tracking player is already online!")
									continue
								}

								val account = getDiscordAccountFromUniqueId(minecraftUser.id.value)

								if (account == null) {
									println("There is a $trackedEntry, but there isn't a discord account!")
									continue
								}

								val user = jda.getUserById(account.discordId)

								if (user == null) {
									println("There is a $trackedEntry, but I wasn't able to find the user!")
									continue
								}

								user.openPrivateChannel().queue {
									it.sendMessage(
										EmbedBuilder()
											.setTitle("<a:lori_pat:706263175892566097> Seu amigx está online no SparklyPower!")
											.setDescription("Seu amigx `${joinedPlayer}` acabou de entrar no SparklyPower! Que tal entrar para fazer companhia para elx?")
											.setColor(Constants.LORITTA_AQUA)
											.setThumbnail("https://sparklypower.net/api/v1/render/avatar?name=${joinedPlayer}&scale=16")
											.setTimestamp(Instant.now())
											.build()
									).queue()
								}
							}
						}
					}

					previousPlayers = currentPlayers
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

					// if (idx % 2 == 0) {
					val payload = Server.PERFECTDREAMS_SURVIVAL.send(
						jsonObject(
							"type" to "getTps"
						)
					)

					println(payload)

					val tps = payload["tps"].array
					val currentTps = tps[0].double

					val status = if (currentTps > 19.2) {
						OnlineStatus.ONLINE
					} else if (currentTps > 17.4) {
						OnlineStatus.IDLE
					} else {
						OnlineStatus.DO_NOT_DISTURB
					}

					jda.presence.setPresence(
						status,
						Activity.watching(
							"$prefix ${response["players"].array.size()} player$plural online no SparklyPower! | \uD83C\uDFAE mc.sparklypower.net | TPS: ${
								"%.2f".format(
									currentTps
								)
							}"
						)
					)

					// } else {
					// 	jda.presence.activity = Activity.playing("A Loritta é a minha melhor amiga!")
					// }

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

						val webhook = textChannel.retrieveWebhooks().complete().firstOrNull() ?: textChannel.createWebhook(
							"$tag Relay"
						).complete()
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
				Users,
				NotifyPlayersOnline
			)
		}
	}

	suspend fun <T> transactionOnSparklyPowerDatabase(statement: Transaction.() -> T): T {
		return withContext(Dispatchers.IO) {
			transaction(Databases.sparklyPower) {
				statement.invoke(this)
			}
		}
	}

	suspend fun <T> transactionOnLuckPermsDatabase(statement: Transaction.() -> T): T {
		return withContext(Dispatchers.IO) {
			transaction(Databases.sparklyPowerLuckPerms) {
				statement.invoke(this)
			}
		}
	}

	suspend fun retrieveDiscordAccountFromUser(user: User): DiscordAccount? {
		return getDiscordAccountFromId(user.idLong)
	}

	suspend fun retrieveDiscordAccountFromUser(id: Long): DiscordAccount? {
		return transactionOnSparklyPowerDatabase {
			DiscordAccount.find { DiscordAccounts.discordId eq id }.firstOrNull()
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

	fun getDiscordAccountFromUniqueId(uniqueId: UUID): DiscordAccount? {
		return transaction(Databases.sparklyPower) {
			DiscordAccount.find { DiscordAccounts.minecraftId eq uniqueId }.firstOrNull()
		}
	}

	fun getMinecraftUserFromUniqueId(uniqueId: UUID) = transaction(Databases.sparklyPower) {
		net.perfectdreams.pantufa.dao.User.findById(uniqueId)
	}

	suspend fun retrieveMinecraftUserFromUsername(username: String) = transactionOnSparklyPowerDatabase {
		net.perfectdreams.pantufa.dao.User.find { Users.username eq username }.firstOrNull()
	}

	fun getMinecraftUserFromUsername(username: String) = transaction(Databases.sparklyPower) {
		net.perfectdreams.pantufa.dao.User.find { Users.username eq username }.firstOrNull()
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

	fun launch(block: suspend CoroutineScope.() -> Unit): Job {
		val job = GlobalScope.launch(coroutineDispatcher, block = block)
		return job
	}
}

val pantufa get() = PantufaBot.INSTANCE
val jda get() = PantufaBot.INSTANCE.jda
val jsonParser = JsonParser()
val gson = Gson()