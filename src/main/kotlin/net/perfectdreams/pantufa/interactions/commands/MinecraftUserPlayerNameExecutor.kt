package net.perfectdreams.pantufa.interactions.commands

import net.perfectdreams.discordinteraktions.common.commands.SlashCommandExecutorDeclaration
import net.perfectdreams.discordinteraktions.common.commands.options.ApplicationCommandOptions
import net.perfectdreams.discordinteraktions.common.commands.options.SlashCommandArguments
import net.perfectdreams.pantufa.PantufaBot
import net.perfectdreams.pantufa.network.Databases
import net.perfectdreams.pantufa.utils.PantufaReply
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.Duration
import java.time.format.DateTimeFormatter

class MinecraftUserPlayerNameExecutor(pantufa: PantufaBot) : PantufaInteractionCommand(
    pantufa
) {
    companion object : SlashCommandExecutorDeclaration(MinecraftUserPlayerNameExecutor::class) {
        object Options : ApplicationCommandOptions() {
            val playerName = string("player_name", "Nome do Player")
                .register()
        }

        override val options = Options
    }

    override suspend fun executePantufa(context: PantufaCommandContext, args: SlashCommandArguments) {
        val playerName = args[options.playerName]

        val minecraftUser = pantufa.getMinecraftUserFromUsername(playerName) ?: run {
            context.reply(
                PantufaReply(
                    "O usuário não tem uma conta associada!"
                )
            )
            return
        }

        val userInfo = pantufa.getDiscordAccountFromUniqueId(minecraftUser.id.value)

        val minecraftId = userInfo?.minecraftId
        val timestamp = DateTimeFormatter.ISO_INSTANT.format(
            java.time.Instant.now()
                .minusSeconds(86400 * 30)
        )

        val survivalTrackedOnlineHoursDuration = if (minecraftId != null) {
            var survivalTrackedOnlineHours: Duration? = null

            transaction(Databases.sparklyPower) {
                exec("select extract(epoch FROM SUM(logged_out - logged_in)) from survival_trackedonlinehours where player = '${minecraftId}' and logged_out >= '$timestamp'") {
                    while (it.next()) {
                        survivalTrackedOnlineHours = Duration.ofSeconds(it.getLong(1))
                    }
                }
            }

            survivalTrackedOnlineHours
        } else { null }

        val survivalTrackedOnlineHours = if (survivalTrackedOnlineHoursDuration != null) {
            val input = survivalTrackedOnlineHoursDuration.seconds
            val numberOfDays = input / 86400
            val numberOfHours = input % 86400 / 3600
            val numberOfMinutes = input % 86400 % 3600 / 60

            "$numberOfDays dias, $numberOfHours horas e $numberOfMinutes minutos"
        } else { "Desconhecido" }

        context.reply(
            PantufaReply(
                "**Informações da conta de <@${userInfo?.discordId}>**"
            ),
            PantufaReply(
                "**Nome:** `${minecraftUser.username}`",
                mentionUser = false
            ),
            PantufaReply(
                "**UUID:** `${userInfo?.minecraftId}`",
                mentionUser = false
            ),
            PantufaReply(
                "**A conta já foi conectada?** ${userInfo?.isConnected}",
                mentionUser = false
            ),
            PantufaReply(
                "**Tempo Online no SparklyPower Survival:** $survivalTrackedOnlineHours",
                mentionUser = false
            )
        )
    }
}