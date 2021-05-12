package net.perfectdreams.pantufa.threads

import mu.KotlinLogging
import net.dv8tion.jda.api.entities.Activity
import net.perfectdreams.pantufa.PantufaBot
import net.perfectdreams.pantufa.network.Databases
import net.perfectdreams.pantufa.pantufa
import net.perfectdreams.pantufa.tables.CraftConomyBalance
import net.perfectdreams.pantufa.utils.CraftConomyUtils
import org.jetbrains.exposed.sql.SqlExpressionBuilder
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update

class CheckDreamPresenceThread : Thread("Check Dream Presence Thread") {
	private val logger = KotlinLogging.logger {}

	override fun run() {
		while (true) {
			logger.info { "Verifying member presences..." }
			try {
				for (member in pantufa.jda.guilds.flatMap { it.members }.distinctBy { it.user.id }) {
					val customStatus = member?.activities?.firstOrNull { it.type == Activity.ActivityType.CUSTOM_STATUS }
							?: continue

					if (customStatus.name.contains("mc.sparklypower.net") || customStatus.name.contains("discord.gg/sparklypower")) {
						val discordAccount = PantufaBot.INSTANCE.getDiscordAccountFromUser(member.user) ?: continue

						val serverAccountId = CraftConomyUtils.getCraftConomyAccountId(discordAccount.minecraftId) ?: continue

						logger.info { "Giving out 15 sonhos to ${member.user} (MC UUID is ${discordAccount.minecraftId}; CC account is $serverAccountId), status: ${customStatus.name}" }

						transaction(Databases.craftConomy) {
							CraftConomyBalance.update({ CraftConomyBalance.id eq serverAccountId }) {
								with(SqlExpressionBuilder) {
									it.update(balance, balance + 15.0)
								}
							}
						}
					}
				}
			} catch (e: Exception) {
				logger.warn(e) { "Something went wrong!" }
			}
			Thread.sleep(60_000)
		}
	}
}