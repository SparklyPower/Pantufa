package net.perfectdreams.pantufa.commands.server

import net.perfectdreams.pantufa.commands.AbstractCommand
import net.perfectdreams.pantufa.commands.CommandContext

class PanelaCommand : AbstractCommand("panela", listOf("panelinha", "clan", "clans", "cla", "clã", "clube")) {
	override fun run(context: CommandContext) {
		/* val arg0 = context.args.getOrNull(0)

		if (arg0 == "list" || arg0 == "lista") {
			var page = context.args.getOrNull(1)?.toIntOrNull() ?: 1

			val panelas = pantufa.databaseSurvival.getCollection("panelinhas")
					.find()

			val panelaKdrs = mutableMapOf<String, Double>()

			for (panela in panelas) {
				var kills = 0
				var deaths = 0

				val members = panela.get("members") as List<Document>
				val usernames = members.map { it.getString("player") }

				val kdrs = pantufa.databaseSurvival.getCollection("panelinhas").find(
						Filters.`in`("username", usernames)
				)

				for (kdr in kdrs) {
					kills += kdr.getInteger("kills")
					deaths += kdr.getInteger("deaths")
				}

				try {
					if (deaths == 0) {
						deaths = 1
					}
					val kdr = kills.toDouble() / deaths.toDouble()
					val x = Math.round(kdr * 100.0) / 100.0
					panelaKdrs[panela.getString("_id")] = x
				} catch (e: Exception) {
					panelaKdrs[panela.getString("_id")] = 0.0
				}
			}

			val sorted = panelas.sortedByDescending {
				panelaKdrs[it.getString("_id")]
			}

			val x = (page - 1) * 5
			val y = (page * 5)

			val display = sorted.subList(x, Math.min(sorted.size, y))

			val embed = EmbedBuilder()
			embed.setTitle("\uD83C\uDF72 Melhores Panelinhas")
			embed.setColor(Constants.LORITTA_AQUA)
			for ((index, document) in display.withIndex()) {
				val icon = when (index) {
					0 -> "\uD83E\uDD47"
					1 -> "\uD83E\uDD48"
					2 -> "\uD83E\uDD49"
					else -> "\uD83C\uDF73"
				}
				embed.addField("$icon «${document.getString("_id")}» ${document.getString("name")}",
						"""👑 **Dono:** ${document.getString("owner")}
							|👥 **Membros:** ${(document.get("members") as List<Document>).size}
							|🤺 **KDR:** ${panelaKdrs[document.getString("_id")]}
						""".trimMargin(), true)
			}
			context.sendMessage(embed.build())
			return
		}
		return */
	}
}