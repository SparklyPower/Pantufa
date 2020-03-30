package net.perfectdreams.pantufa.utils

import net.perfectdreams.pantufa.commands.CommandContext
import net.perfectdreams.pantufa.network.Databases
import net.perfectdreams.pantufa.tables.CraftConomyAccounts
import net.perfectdreams.pantufa.tables.CraftConomyBalance
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction

object CraftConomyUtils {
	fun getCraftConomyAccountId(context: CommandContext): Int {
		return transaction(Databases.craftConomy) {
			CraftConomyAccounts.select {
				CraftConomyAccounts.uuid eq context.discordAccount!!.minecraftId.toString()
			}.firstOrNull()?.get(CraftConomyAccounts.id)
		} ?: throw RuntimeException()
	}

	fun getCraftConomyBalance(accountId: Int): Double {
		return transaction(Databases.craftConomy) {
			CraftConomyBalance.select {
				CraftConomyBalance.id eq accountId
			}.firstOrNull()?.get(CraftConomyBalance.balance)
		} ?: throw RuntimeException()
	}
}