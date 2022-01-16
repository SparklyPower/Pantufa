package net.perfectdreams.pantufa.threads

import mu.KotlinLogging
import net.dv8tion.jda.api.entities.Activity
import net.perfectdreams.pantufa.PantufaBot
import net.perfectdreams.pantufa.dao.DiscordAccount
import net.perfectdreams.pantufa.network.Databases
import net.perfectdreams.pantufa.pantufa
import net.perfectdreams.pantufa.tables.CraftConomyAccounts
import net.perfectdreams.pantufa.tables.CraftConomyBalance
import net.perfectdreams.pantufa.tables.DiscordAccounts
import net.perfectdreams.pantufa.utils.CraftConomyUtils
import org.jetbrains.exposed.sql.SqlExpressionBuilder
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update

class CheckDreamPresenceThread : Thread("Check Dream Presence Thread") {
	private val logger = KotlinLogging.logger {}

	override fun run() {
		while (true) {
			logger.info { "Verifying member presences..." }
			try {
				val membersWithSparklyStatus = pantufa.jda.guilds
					.flatMap { it.members }
					.asSequence()
					.distinctBy { it.user.id }
					.filter {
						val activity = it?.activities?.firstOrNull { it.type == Activity.ActivityType.CUSTOM_STATUS }
						activity?.name?.contains("mc.sparklypower.net") == true || activity?.name?.contains("discord.gg/sparklypower") == true
					}
					.toList()

				logger.info { "There are ${membersWithSparklyStatus.size} members with SparklyPower's status!" }

				val discordAccounts = transaction(Databases.sparklyPower) {
					DiscordAccount.find { DiscordAccounts.discordId inList membersWithSparklyStatus.map { it.idLong } }
						.toList()
				}

				logger.info { "From the ${membersWithSparklyStatus.size} members that has SparklyPower's status, ${discordAccounts.size} associated their account with SparklyPower!" }

				transaction(Databases.craftConomy) {
					(CraftConomyBalance.innerJoin(CraftConomyAccounts))
						.update({
							CraftConomyAccounts.uuid inList (discordAccounts.map { it.minecraftId.toString() })
						}) {
							with(SqlExpressionBuilder) {
								it.update(CraftConomyBalance.balance, CraftConomyBalance.balance + 15.0)
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