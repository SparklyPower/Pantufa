package net.perfectdreams.pantufa.threads

import net.dv8tion.jda.api.entities.Guild
import net.perfectdreams.pantufa.dao.DiscordAccount
import net.perfectdreams.pantufa.network.Databases
import net.perfectdreams.pantufa.tables.DiscordAccounts
import net.perfectdreams.pantufa.tables.LuckPermsPlayers
import net.perfectdreams.pantufa.tables.LuckPermsUserPermissions
import net.perfectdreams.pantufa.utils.Constants
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.or
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.*

class SyncRolesThread : Thread("Sync Dream Roles Thread") {
	fun getPlayersWithGroup(vararg primaryGroup: String): List<UUID> {
		val primaryGroupPlayers = transaction(Databases.sparklyPowerLuckPerms) {
			LuckPermsPlayers.select {
				LuckPermsPlayers.primaryGroup inList primaryGroup.toMutableList()
			}.map {
				UUID.fromString(it[LuckPermsPlayers.id])
			}.toMutableList()
		}

		val secondaryGroupPlayers = transaction(Databases.sparklyPowerLuckPerms) {
			LuckPermsUserPermissions.select {
				(LuckPermsUserPermissions.permission inList primaryGroup.map { "group.$it" }) and
						((LuckPermsUserPermissions.expiry greater (System.currentTimeMillis() / 1000).toInt())
								or (LuckPermsUserPermissions.expiry eq 0))
			}.map {
				UUID.fromString(it[LuckPermsUserPermissions.uuid])
			}.toMutableList()
		}

		return (primaryGroupPlayers + secondaryGroupPlayers).distinct()
	}

	fun syncRolesEligibleForUsers(guild: Guild, roleId: String, eligibleUniqueIds: List<UUID>) {
		val adminRole = guild.getRoleById(roleId)!!
		val membersWithAdminRole = guild.getMembersWithRoles(adminRole)
		val discordAccountsOfTheUsers = transaction(Databases.sparklyPower) {
			DiscordAccount.find {
				DiscordAccounts.discordId inList membersWithAdminRole.map { it.user.idLong }
			}
		}

		membersWithAdminRole.forEach {
			val accountOfTheUser = discordAccountsOfTheUsers.firstOrNull { account -> account.discordId == it.user.idLong }

			if (accountOfTheUser == null || !accountOfTheUser.isConnected || !eligibleUniqueIds.contains(accountOfTheUser.minecraftId)) {
				guild.removeRoleFromMember(it, adminRole).queue()
			}
		}

		val discordAccountsOfEligibleUniqueIds = transaction(Databases.sparklyPower) {
			DiscordAccount.find {
				DiscordAccounts.minecraftId inList eligibleUniqueIds
			}
		}

		eligibleUniqueIds.forEach {
			val accountOfTheUser = discordAccountsOfEligibleUniqueIds.firstOrNull { account -> account.minecraftId == it }

			if (accountOfTheUser?.isConnected == true) {
				val member = guild.getMemberById(accountOfTheUser.discordId)

				if (member != null && !member.roles.contains(adminRole))
					guild.addRoleToMember(member, adminRole).queue()
			}
		}
	}

	override fun run() {
		while (true) {
			try {
				val guild = Constants.SPARKLYPOWER_GUILD

				if (guild != null) {
					val discordAccounts = transaction(Databases.sparklyPower) {
						DiscordAccount.find {
							DiscordAccounts.isConnected eq true
						}.toMutableList()
					}

					val role = guild.getRoleById("393468942959509507")!!

					val usersWithSparklyMemberRole = guild.getMembersWithRoles(role)

					usersWithSparklyMemberRole.forEach {
						if (!discordAccounts.any { account -> account.isConnected && account.discordId == it.user.idLong })
							guild.removeRoleFromMember(it, role).queue()
					}

					for (discordAccount in discordAccounts) {
						val member = guild.getMemberById(discordAccount.discordId) ?: continue

						if (!member.roles.contains(role))
							guild.addRoleToMember(member, role).queue()
					}

					val owners = getPlayersWithGroup("dono")
					val admins = getPlayersWithGroup("admin")
					val moderators = getPlayersWithGroup("moderador")
					val coordenadores = getPlayersWithGroup("coordenador")
					val supports = getPlayersWithGroup("suporte")
					val builders = getPlayersWithGroup("construtor")
					val vips = getPlayersWithGroup("vip", "vip+", "vip++", "sonhador", "sonhador+", "sonhador++")
					val youtubers = getPlayersWithGroup("youtuber")

					syncRolesEligibleForUsers(guild, "333601725862641664", owners)
					syncRolesEligibleForUsers(guild, "333602159998271489", admins)
					syncRolesEligibleForUsers(guild, "693606685943660545", coordenadores)
					syncRolesEligibleForUsers(guild, "333602209621344267", moderators)
					syncRolesEligibleForUsers(guild, "333602241564901378", supports)
					syncRolesEligibleForUsers(guild, "359014713599983625", builders)
					syncRolesEligibleForUsers(guild, "332650495522897920", owners.toMutableList() + admins.toMutableList() + coordenadores.toMutableList() + moderators.toMutableList() + supports.toMutableList() + builders.toMutableList())
					syncRolesEligibleForUsers(guild, "332652664544428044", vips)
					syncRolesEligibleForUsers(guild, "373548131016507393", youtubers)
				}
			} catch (e: Exception) {
				e.printStackTrace()
			}

			Thread.sleep(5000)
		}
	}
}