package net.perfectdreams.pantufa.threads

import com.github.salomonbrys.kotson.double
import com.github.salomonbrys.kotson.jsonObject
import net.perfectdreams.pantufa.dao.DiscordAccount
import net.perfectdreams.pantufa.network.Databases
import net.perfectdreams.pantufa.pantufa
import net.perfectdreams.pantufa.tables.DiscordAccounts
import net.perfectdreams.pantufa.utils.Constants
import net.perfectdreams.pantufa.utils.PantufaReply
import net.perfectdreams.pantufa.utils.socket.SocketUtils
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.transactions.transaction

class CheckDreamPresenceThread : Thread("Check Dream Presence Thread") {
	override fun run() {
		while (true) {
			for (member in pantufa.jda.guilds.flatMap { it.members }.distinctBy { it.user.id }) {
				val game = member.activities.firstOrNull() ?: continue

				if (game.isRich) {
					val richPresence = game.asRichPresence()

					if (richPresence.applicationIdLong == 415617983411388428L) {
						if (richPresence.details?.contains("mc.sparklypower.net") == true) {
							println("${member.user.name}#${member.user.discriminator} - ${member.user.id} está usando a rich presence do SparklyPower!")

							val account = transaction(Databases.sparklyPower) {
								DiscordAccount.find {
									DiscordAccounts.discordId eq member.user.idLong and (DiscordAccounts.isConnected eq true)
								}.firstOrNull()
							} ?: continue

							val jsonObject = jsonObject(
									"type" to "giveBalanceUuid",
									"player" to account.minecraftId.toString(),
									"quantity" to 1.25
							)

							SocketUtils.sendAsync(jsonObject, port = Constants.PERFECTDREAMS_SURVIVAL_PORT, success = {
								response ->

							}, error = { println("Error while giving money!") })
						}
					}
				}
			}
			Thread.sleep(60_000)
		}
	}
}