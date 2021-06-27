package net.perfectdreams.pantufa.interactions.commands

import com.github.salomonbrys.kotson.array
import com.github.salomonbrys.kotson.get
import com.github.salomonbrys.kotson.obj
import com.github.salomonbrys.kotson.set
import com.github.salomonbrys.kotson.string
import com.google.gson.JsonObject
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import net.perfectdreams.discordinteraktions.common.context.SlashCommandArguments
import net.perfectdreams.discordinteraktions.declarations.slash.SlashCommandDeclaration
import net.perfectdreams.discordinteraktions.declarations.slash.SlashCommandExecutorDeclaration
import net.perfectdreams.pantufa.PantufaBot
import net.perfectdreams.pantufa.utils.Constants
import net.perfectdreams.pantufa.utils.Constants.SPARKLYPOWER_OFFLINE
import net.perfectdreams.pantufa.utils.PantufaReply
import net.perfectdreams.pantufa.utils.socket.SocketUtils

class OnlineExecutor(pantufa: PantufaBot) : PantufaInteractionCommand(
    pantufa
) {
    companion object : SlashCommandExecutorDeclaration(OnlineExecutor::class) {
        val serverToFancyName = mapOf(
            "sparklypower_lobby" to "SparklyPower Lobby",
            "sparklypower_survival" to "SparklyPower Survival"
        )
    }

    override suspend fun executePantufa(context: PantufaCommandContext, args: SlashCommandArguments) {
        val jsonObject = JsonObject()
        jsonObject["type"] = "getOnlinePlayersInfo"

        SocketUtils.sendAsync(jsonObject, host = Constants.PERFECTDREAMS_BUNGEE_IP, port = Constants.PERFECTDREAMS_BUNGEE_PORT, success = { response ->
            val servers = response["servers"].array

            val replies = mutableListOf<PantufaReply>()

            val totalPlayersOnline = servers.sumBy {
                it["players"].array.size()
            }
            replies.add(
                PantufaReply(
                    message = "**Players Online no SparklyPower Network ($totalPlayersOnline players online)**",
                    prefix = "<:pocketdreams:333655151871000576>"
                )
            )

            servers.forEach {
                val obj = it.obj
                val name = obj["name"].string
                val players = obj["players"].array.map {
                    it["name"].string
                }.sorted()

                val fancyName = serverToFancyName[name]

                if (fancyName != null) {
                    if (players.isNotEmpty()) {
                        replies.add(
                            PantufaReply(
                                "**$fancyName (${players.size})**: ${players.joinToString(", ", transform = { "**`$it`**" })}",
                                mentionUser = false
                            )
                        )
                    } else {
                        replies.add(
                            PantufaReply(
                                "**$fancyName (${players.size})**: Ninguém online... \uD83D\uDE2D",
                                mentionUser = false
                            )
                        )
                    }
                }
            }

            GlobalScope.launch {
                context.reply(*replies.toTypedArray())
            }
        }, error = { GlobalScope.launch { SPARKLYPOWER_OFFLINE.invoke(context) } })
    }
}