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
import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.requests.GatewayIntent
import net.perfectdreams.discordinteraktions.common.DiscordInteraKTions
import net.perfectdreams.pantufa.commands.CommandManager
import net.perfectdreams.pantufa.commands.server.*
import net.perfectdreams.pantufa.commands.vanilla.utils.PingCommand
import net.perfectdreams.pantufa.dao.DiscordAccount
import net.perfectdreams.pantufa.interactions.commands.*
import net.perfectdreams.pantufa.interactions.commands.administration.*
import net.perfectdreams.pantufa.interactions.commands.declarations.SayCommand
import net.perfectdreams.pantufa.interactions.commands.say.SayEditMessageCommand
import net.perfectdreams.pantufa.interactions.commands.say.SayEditMessageExecutor
import net.perfectdreams.pantufa.interactions.commands.say.SayEditModalSubmitExecutor
import net.perfectdreams.pantufa.interactions.commands.say.SaySendExecutor
import net.perfectdreams.pantufa.interactions.commands.say.SaySendModalSubmitExecutor
import net.perfectdreams.pantufa.listeners.DiscordListener
import net.perfectdreams.pantufa.listeners.InteractionListener
import net.perfectdreams.pantufa.network.Databases
import net.perfectdreams.pantufa.tables.DiscordAccounts
import net.perfectdreams.pantufa.tables.NotifyPlayersOnline
import net.perfectdreams.pantufa.tables.Users
import net.perfectdreams.pantufa.utils.CachedGraphManager
import net.perfectdreams.pantufa.utils.Constants
import net.perfectdreams.pantufa.utils.config.PantufaConfig
import net.perfectdreams.pantufa.utils.discord.DiscordCommandMap
import net.perfectdreams.pantufa.utils.parallax.ParallaxEmbed
import net.perfectdreams.pantufa.utils.socket.SocketHandler
import net.perfectdreams.pantufa.utils.socket.SocketServer
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.Transaction
import org.jetbrains.exposed.sql.transactions.transaction
import java.awt.image.BufferedImage
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

	val applicationId = Snowflake(390927821997998081L)
	val interaKTions = DiscordInteraKTions(config.token, applicationId)
	val legacyCommandManager = CommandManager()
	val legacyCommandMap = DiscordCommandMap(this)
	val executors = Executors.newCachedThreadPool()
	val coroutineDispatcher = executors.asCoroutineDispatcher()
	lateinit var jda: JDA
	val rest = RestClient(config.token)
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
	val playersOnlineGraph = CachedGraphManager(config.grafana.token, "${config.grafana.url}/render/d-solo/JeZauCDnk/sparklypower-network?orgId=1&var-sparklypower_server=sparklypower_survival&var-world=All&panelId=87&width=800&height=300&tz=America%2FSao_Paulo")

	fun start() {
		INSTANCE = this
		logger.info { "Starting Pantufa..." }

		initPostgreSql()

		logger.info { "Registering Application Commands..." }

		interaKTions.manager.register(net.perfectdreams.pantufa.interactions.commands.declarations.ChatColorCommand(this))
		interaKTions.manager.register(net.perfectdreams.pantufa.interactions.commands.declarations.GuildsCommand(this))
		interaKTions.manager.register(net.perfectdreams.pantufa.interactions.commands.declarations.LSXCommand(this))
		interaKTions.manager.register(net.perfectdreams.pantufa.interactions.commands.declarations.MinecraftUserCommand(this))
		interaKTions.manager.register(net.perfectdreams.pantufa.interactions.commands.declarations.MoneyCommand(this))
		interaKTions.manager.register(net.perfectdreams.pantufa.interactions.commands.declarations.OnlineCommand(this))
		interaKTions.manager.register(net.perfectdreams.pantufa.interactions.commands.declarations.PesadelosCommand(this))
		interaKTions.manager.register(net.perfectdreams.pantufa.interactions.commands.declarations.PingCommand(this))
		interaKTions.manager.register(net.perfectdreams.pantufa.interactions.commands.declarations.RegistrarCommand(this))
		interaKTions.manager.register(net.perfectdreams.pantufa.interactions.commands.declarations.VIPInfoCommand(this))
		interaKTions.manager.register(net.perfectdreams.pantufa.interactions.commands.declarations.CommandsLogCommand(this))
		interaKTions.manager.register(net.perfectdreams.pantufa.interactions.commands.declarations.TransactionsCommand(this))
		interaKTions.manager.register(
			net.perfectdreams.pantufa.interactions.components.ChangePageButtonClickExecutor,
			net.perfectdreams.pantufa.interactions.components.ChangePageButtonClickExecutor()
		)

		interaKTions.manager.register(
			net.perfectdreams.pantufa.interactions.components.TransactionFilterSelectMenuExecutor,
			net.perfectdreams.pantufa.interactions.components.TransactionFilterSelectMenuExecutor()
		)

		interaKTions.manager.register(
			net.perfectdreams.pantufa.interactions.commands.declarations.AdminConsoleBungeeCommand(this)
		)

		interaKTions.manager.register(SayCommand(this))

		interaKTions.manager.register(SayEditMessageCommand(this))

		interaKTions.manager.register(
			SaySendModalSubmitExecutor,
			SaySendModalSubmitExecutor(this)
		)

		interaKTions.manager.register(
			SayEditModalSubmitExecutor,
			SayEditModalSubmitExecutor(this)
		)

		interaKTions.manager.register(net.perfectdreams.pantufa.interactions.commands.declarations.ChangePassCommand(this))

		logger.info { "Starting JDA..." }
		jda = JDABuilder.create(EnumSet.allOf(GatewayIntent::class.java))
			.addEventListeners(
				InteractionListener(
					rest,
					Snowflake(390927821997998081L),
					interaKTions
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

		runBlocking {
			if (config.discordInteractions.registerGlobally) {
				logger.info { "Updating Pantufa's Application Commands Globally..." }
				interaKTions.updateAllGlobalCommands()
			} else {
				for (id in config.discordInteractions.guildsToBeRegistered) {
					logger.info { "Updating Pantufa's Application Commands on Guild $id..." }
					interaKTions.updateAllCommandsInGuild(Snowflake(id))
				}
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

								messageBuilder.setEmbeds(discordEmbed)
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