package net.perfectdreams.pantufa.threads

import net.perfectdreams.pantufa.dao.DiscordAccount
import net.perfectdreams.pantufa.network.Databases
import net.perfectdreams.pantufa.pantufa
import net.perfectdreams.pantufa.tables.DiscordAccounts
import net.perfectdreams.pantufa.utils.Constants
import org.jetbrains.exposed.sql.transactions.transaction

class SyncRolesThread : Thread("Sync Dream Roles Thread") {
	override fun run() {
		while (true) {
			val guild = Constants.SPARKLYPOWER_GUILD

			if (guild != null) {
				val discordAccounts = transaction(Databases.sparklyPower) {
					DiscordAccount.find {
						DiscordAccounts.isConnected eq true
					}.toMutableList()
				}

				for (discordAccount in discordAccounts) {
					val member = guild.getMemberById(discordAccount.discordId) ?: continue

					val role = guild.getRoleById("393468942959509507")

					if (!member.roles.contains(role))
						guild.controller.addSingleRoleToMember(member, role).queue()
				}
			}

			Thread.sleep(5000)
		}
	}
}