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
		val adminRole = guild.getRoleById(roleId)
		val membersWithAdminRole = guild.getMembersWithRoles(adminRole)

		membersWithAdminRole.forEach {
			transaction(Databases.sparklyPower) {
				val accountOfTheUser = DiscordAccount.find {
					DiscordAccounts.discordId eq it.user.idLong
				}.firstOrNull()

				if (accountOfTheUser == null || !accountOfTheUser.isConnected || !eligibleUniqueIds.contains(accountOfTheUser.minecraftId)) {
					guild.controller.removeSingleRoleFromMember(it, adminRole).queue()
				}
			}
		}

		eligibleUniqueIds.forEach {
			transaction(Databases.sparklyPower) {
				val accountOfTheUser = DiscordAccount.find {
					DiscordAccounts.minecraftId eq it
				}.firstOrNull()

				if (accountOfTheUser?.isConnected == true) {
					val member = guild.getMemberById(accountOfTheUser.discordId)

					if (member != null && !member.roles.contains(adminRole))
						guild.controller.addSingleRoleToMember(member, adminRole).queue()
				}
			}
		}
	}

	override fun run() {
		while (true) {
			val guild = Constants.SPARKLYPOWER_GUILD

			if (guild != null) {
				val discordAccounts = transaction(Databases.sparklyPower) {
					DiscordAccount.find {
						DiscordAccounts.isConnected eq true
					}.toMutableList()
				}

				val role = guild.getRoleById("393468942959509507")

				val usersWithSparklyMemberRole = guild.getMembersWithRoles(role)

				usersWithSparklyMemberRole.forEach {
					if (!discordAccounts.any { account -> account.isConnected && account.discordId == it.user.idLong })
						guild.controller.removeSingleRoleFromMember(it, role).queue()
				}

				for (discordAccount in discordAccounts) {
					val member = guild.getMemberById(discordAccount.discordId) ?: continue

					if (!member.roles.contains(role))
						guild.controller.addSingleRoleToMember(member, role).queue()
				}

				val owners = getPlayersWithGroup("dono")
				val admins = getPlayersWithGroup("admin")
				val moderators = getPlayersWithGroup("moderador")
				val supports = getPlayersWithGroup("suporte")
				val vips = getPlayersWithGroup("vip", "vip+", "vip++", "sonhador", "sonhador+", "sonhador++")
				val youtubers = getPlayersWithGroup("youtuber")

				syncRolesEligibleForUsers(guild, "333601725862641664", owners)
				syncRolesEligibleForUsers(guild, "333602159998271489", admins)
				syncRolesEligibleForUsers(guild, "333602209621344267", moderators)
				syncRolesEligibleForUsers(guild, "333602241564901378", supports)
				syncRolesEligibleForUsers(guild, "332650495522897920", owners.toMutableList() + admins.toMutableList() + moderators.toMutableList() + supports.toMutableList())
				syncRolesEligibleForUsers(guild, "332652664544428044", vips)
				syncRolesEligibleForUsers(guild, "373548131016507393", youtubers)
			}

			Thread.sleep(5000)
		}
	}
}