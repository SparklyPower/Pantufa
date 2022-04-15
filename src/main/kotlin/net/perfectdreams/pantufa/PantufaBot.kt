package net.perfectdreams.pantufa

import com.github.salomonbrys.kotson.*
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import dev.kord.common.entity.Snowflake
import dev.kord.rest.service.RestClient
import io.ktor.client.*
import kotlinx.coroutines.*
import mu.KotlinLogging
import net.dv8tion.jda.api.*
import net.dv8tion.jda.api.entities.Activity
import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.requests.GatewayIntent
import net.perfectdreams.discordinteraktions.common.builder.message.MessageBuilder
import net.perfectdreams.discordinteraktions.platforms.kord.commands.KordCommandRegistry
import net.perfectdreams.pantufa.commands.CommandManager
import net.perfectdreams.pantufa.commands.server.*
import net.perfectdreams.pantufa.commands.vanilla.utils.PingCommand
import net.perfectdreams.pantufa.dao.DiscordAccount
import net.perfectdreams.pantufa.interactions.commands.ChangePassExecutor
import net.perfectdreams.pantufa.interactions.commands.ChatColorExecutor
import net.perfectdreams.pantufa.interactions.commands.GuildsExecutor
import net.perfectdreams.pantufa.interactions.commands.LSXExecutor
import net.perfectdreams.pantufa.interactions.commands.MinecraftUserDiscordUserExecutor
import net.perfectdreams.pantufa.interactions.commands.MinecraftUserPlayerNameExecutor
import net.perfectdreams.pantufa.interactions.commands.MoneyExecutor
import net.perfectdreams.pantufa.interactions.commands.OnlineExecutor
import net.perfectdreams.pantufa.interactions.commands.PesadelosExecutor
import net.perfectdreams.pantufa.interactions.commands.PingExecutor
import net.perfectdreams.pantufa.interactions.commands.RegistrarExecutor
import net.perfectdreams.pantufa.interactions.commands.VIPInfoExecutor
import net.perfectdreams.pantufa.interactions.commands.administration.*
import net.perfectdreams.pantufa.listeners.DiscordListener
import net.perfectdreams.pantufa.listeners.InteractionListener
import net.perfectdreams.pantufa.network.Databases
import net.perfectdreams.pantufa.tables.DiscordAccounts
import net.perfectdreams.pantufa.tables.NotifyPlayersOnline
import net.perfectdreams.pantufa.tables.Users
import net.perfectdreams.pantufa.threads.CheckDreamPresenceThread
import net.perfectdreams.pantufa.threads.SyncRolesThread
import net.perfectdreams.pantufa.utils.CachedGraphManager
import net.perfectdreams.pantufa.utils.Constants
import net.perfectdreams.pantufa.utils.Server
import net.perfectdreams.pantufa.utils.config.PantufaConfig
import net.perfectdreams.pantufa.utils.discord.DiscordCommandMap
import net.perfectdreams.pantufa.utils.parallax.ParallaxEmbed
import net.perfectdreams.pantufa.utils.socket.SocketHandler
import net.perfectdreams.pantufa.utils.socket.SocketServer
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

		val http = HttpClient {
			expectSuccess = false
		}

		private val logger = KotlinLogging.logger {}
	}

	val commandManager = net.perfectdreams.discordinteraktions.common.commands.CommandManager()
	val legacyCommandManager = CommandManager()
	val legacyCommandMap = DiscordCommandMap(this)
	val executors = Executors.newCachedThreadPool()
	val coroutineDispatcher = executors.asCoroutineDispatcher()
	lateinit var jda: JDA
	val rest = RestClient(config.token)
	var previousPlayers: List<String>? = null
	val whitelistedGuildIds = listOf(
		Snowflake(265632341530116097L), // SparklyPower (Old)
		Snowflake(268353819409252352L), // Ideias Aleatórias
		Snowflake(297732013006389252L), // Apartamento da Loritta
		Snowflake(602640402830589954L), // Furdúncios Artísticos
		Snowflake(320248230917046282), // SparklyPower (New)
		Snowflake(420626099257475072L), // Loritta's Apartment
		Snowflake(340165396692729883L), // Loritta's Emoji Server
		Snowflake(538506322212159578L), // Loritta's Emoji Server 2
		Snowflake(392482696720416775L), // Pantufa's Emoji Server
		Snowflake(626604568741937199L) // Loritta's Emoji Server 4
	)
	val applicationId = Snowflake(390927821997998081L)
	val playersOnlineGraph = CachedGraphManager(config.grafana.token, "${config.grafana.url}/render/d-solo/JeZauCDnk/sparklypower-network?orgId=1&var-sparklypower_server=sparklypower_survival&var-world=All&panelId=87&width=800&height=300&tz=America%2FSao_Paulo")

	fun start() {
		INSTANCE = this
		logger.info { "Starting Pantufa..." }

		initPostgreSql()

		logger.info { "Registering Application Commands..." }

		commandManager.register(
			net.perfectdreams.pantufa.interactions.commands.declarations.ChatColorCommand,
			ChatColorExecutor(this)
		)

		commandManager.register(
			net.perfectdreams.pantufa.interactions.commands.declarations.GuildsCommand,
			GuildsExecutor(this)
		)

		commandManager.register(
			net.perfectdreams.pantufa.interactions.commands.declarations.LSXCommand,
			LSXExecutor(this)
		)

		commandManager.register(
			net.perfectdreams.pantufa.interactions.commands.declarations.MinecraftUserCommand,
			MinecraftUserPlayerNameExecutor(this),
			MinecraftUserDiscordUserExecutor(this)
		)

		commandManager.register(
			net.perfectdreams.pantufa.interactions.commands.declarations.MoneyCommand,
			MoneyExecutor(this)
		)

		commandManager.register(
			net.perfectdreams.pantufa.interactions.commands.declarations.OnlineCommand,
			OnlineExecutor(this)
		)

		commandManager.register(
			net.perfectdreams.pantufa.interactions.commands.declarations.PesadelosCommand,
			PesadelosExecutor(this)
		)

		commandManager.register(
			net.perfectdreams.pantufa.interactions.commands.declarations.PingCommand,
			PingExecutor(this)
		)

		commandManager.register(
			net.perfectdreams.pantufa.interactions.commands.declarations.RegistrarCommand,
			RegistrarExecutor(this)
		)

		commandManager.register(
			net.perfectdreams.pantufa.interactions.commands.declarations.VIPInfoCommand,
			VIPInfoExecutor(this)
		)

		commandManager.register(
			net.perfectdreams.pantufa.interactions.commands.declarations.AdminConsoleBungeeCommand,
			AdvDupeIpExecutor(this),
			BanExecutor(this),
			CheckBanExecutor(this),
			DupeIpExecutor(this),
			FingerprintExecutor(this),
			GeoIpExecutor(this),
			IpBanExecutor(this),
			IpUnbanExecutor(this),
			KickExecutor(this),
			UnbanExecutor(this),
			UnwarnExecutor(this),
			WarnExecutor(this)
		)

		/* commandManager.register(
			net.perfectdreams.pantufa.interactions.commands.declarations.ChangePassCommand,
			ChangePassExecutor(this),
		) */

		logger.info { "Starting JDA..." }
		jda = JDABuilder.create(EnumSet.allOf(GatewayIntent::class.java))
			.addEventListeners(
				InteractionListener(
					rest,
					Snowflake(390927821997998081L),
					commandManager
				)
			)
			.setRawEventsEnabled(true) // Required for InteractionListener
			.setStatus(OnlineStatus.ONLINE)
			.setToken(config.token)
			.build()

		logger.info { "Registering legacy commands..." }
		legacyCommandMap.register(PingCommand.create(this))
		legacyCommandMap.register(MinecraftUserCommand.create(this))
		legacyCommandMap.register(NotificarPlayerCommand.create(this))
		legacyCommandMap.register(NotificarEventoCommand.create(this))
		legacyCommandMap.register(ChatColorCommand.create(this))
		legacyCommandMap.register(VerificarStatusCommand.create(this))
		legacyCommandMap.register(PesadelosCommand.create(this))
		legacyCommandMap.register(VIPInfoCommand.create(this))
		legacyCommandMap.register(MoneyCommand.create(this))
		legacyCommandMap.register(SugerirCommand.create(this))

		logger.info { "Starting SyncRolesThread..." }
		SyncRolesThread().start()

		logger.info { "Starting CheckDreamPresenceThread..." }
		CheckDreamPresenceThread().start()

		runBlocking {
			val registry = KordCommandRegistry(applicationId, rest, commandManager)
			if (config.discordInteractions.registerGlobally) {
				logger.info { "Updating Pantufa's Application Commands Globally..." }
				registry.updateAllGlobalCommands()
			} else {
				for (id in config.discordInteractions.guildsToBeRegistered) {
					logger.info { "Updating Pantufa's Application Commands on Guild $id..." }
					registry.updateAllCommandsInGuild(Snowflake(id))
				}
			}
		}

		logger.info { "Starting Pantufa's Discord Activity Updater thread..." }
		thread {
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
				} catch (e: Exception) {
					e.printStackTrace()

					jda.presence.activity = Activity.playing("\uD83D\uDEAB SparklyPower está offline \uD83D\uDE2D | \uD83C\uDFAE mc.sparklypower.net")
				}

				Thread.sleep(15000) // rate limit de presence é 5 a cada 60 segundos
			}
		}

		logger.info { "Starting Pantufa's SocketServer thread..." }
		thread {
			val socket = SocketServer(60799)
			socket.socketHandler = object: SocketHandler {
				override fun onSocketReceived(json: JsonObject, response: JsonObject) {
					println("RECEIVED: " + json)
					val type = json["type"].nullString ?: return

					println("Type: $type")

					when (type) {
						"sendMessage" -> {
							// TODO: This should be replaced with webhooks within the applications that use this method
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

						"sendEventStart" -> {
							// TODO: This should be replaced with webhooks
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
			}
			socket.start()
		}

		logger.info { "Adding Event Listener..." }
		jda.addEventListener(DiscordListener(this))

		logger.info { "Starting Pantufa Tasks..." }
		PantufaTasks(this).start()

		logger.info { "Done! :3" }
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

	fun launch(block: suspend CoroutineScope.() -> Unit): Job {
		val job = GlobalScope.launch(coroutineDispatcher, block = block)
		return job
	}
}

val pantufa get() = PantufaBot.INSTANCE
val jda get() = PantufaBot.INSTANCE.jda
val jsonParser = JsonParser()
val gson = Gson()