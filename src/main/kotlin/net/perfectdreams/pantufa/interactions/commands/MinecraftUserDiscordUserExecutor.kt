package net.perfectdreams.pantufa.interactions.commands

import net.perfectdreams.discordinteraktions.common.commands.options.ApplicationCommandOptions
import net.perfectdreams.discordinteraktions.common.commands.options.SlashCommandArguments
import net.perfectdreams.pantufa.PantufaBot
import net.perfectdreams.pantufa.network.Databases
import net.perfectdreams.pantufa.utils.PantufaReply
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.Duration
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class MinecraftUserDiscordUserExecutor(pantufa: PantufaBot) : PantufaInteractionCommand(
    pantufa
) {
    inner class Options : ApplicationCommandOptions() {
        val user = user("user", "Conta do Usuário no Discord")
    }

    override val options = Options()

    override suspend fun executePantufa(context: PantufaCommandContext, args: SlashCommandArguments) {
        val discordUser = args[options.user]

        val minecraftUser = pantufa.getDiscordAccountFromId(discordUser.id.value.toLong()) ?: run {
            context.reply(
                PantufaReply(
                    "O usuário <@${discordUser.id.value}> não tem uma conta associada!"
                )
            )
            return
        }

        val userInfo = pantufa.getMinecraftUserFromUniqueId(minecraftUser.minecraftId)

        val minecraftId = minecraftUser.minecraftId
        val timestamp = DateTimeFormatter.ISO_OFFSET_DATE_TIME.format(
            OffsetDateTime.now(ZoneId.of("America/Sao_Paulo"))
                .minusMonths(1L)
        )

        val survivalTrackedOnlineHoursDuration = run {
            var survivalTrackedOnlineHours: Duration? = null

            transaction(Databases.sparklyPower) {
                exec("select extract(epoch FROM SUM(logged_out - logged_in)) from survival_trackedonlinehours where player = '${minecraftId}' and logged_out >= '$timestamp'") {
                    while (it.next()) {
                        survivalTrackedOnlineHours = Duration.ofSeconds(it.getLong(1))
                    }
                }
            }

            survivalTrackedOnlineHours
        }

        val survivalTrackedOnlineHours = if (survivalTrackedOnlineHoursDuration != null) {
            val input = survivalTrackedOnlineHoursDuration.seconds
            val numberOfDays = input / 86400
            val numberOfHours = input % 86400 / 3600
            val numberOfMinutes = input % 86400 % 3600 / 60

            "$numberOfDays dias, $numberOfHours horas e $numberOfMinutes minutos"
        } else { "Desconhecido" }

        context.reply(
            PantufaReply(
                "**Informações da conta de <@${discordUser.id.value}>**"
            ),
            PantufaReply(
                "**Nome:** `${userInfo?.username}`",
                mentionUser = false
            ),
            PantufaReply(
                "**UUID:** `${minecraftUser.minecraftId}`",
                mentionUser = false
            ),
            PantufaReply(
                "**A conta já foi conectada?** ${minecraftUser.isConnected}",
                mentionUser = false
            ),
            PantufaReply(
                "**Tempo Online no SparklyPower Survival nos Últimos 30 Dias:** $survivalTrackedOnlineHours",
                mentionUser = false
            )
        )
    }
}