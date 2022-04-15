package net.perfectdreams.pantufa.interactions.commands.administration

import net.perfectdreams.discordinteraktions.common.commands.SlashCommandExecutorDeclaration
import net.perfectdreams.pantufa.PantufaBot
import net.perfectdreams.pantufa.utils.Server

class KickExecutor(
    pantufa: PantufaBot
) : AdminConsoleBungeeExecutor(
    pantufa,
    "dreamnetworkbans.kick",
    "kick",
    Server.PERFECTDREAMS_BUNGEE
) {
    companion object : SlashCommandExecutorDeclaration(KickExecutor::class) {
        override val options = AdminConsoleBungeeExecutor.Companion.Options
    }
}