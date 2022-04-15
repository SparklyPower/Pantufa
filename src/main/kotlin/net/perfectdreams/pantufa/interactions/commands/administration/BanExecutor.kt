package net.perfectdreams.pantufa.interactions.commands.administration

import net.perfectdreams.discordinteraktions.common.commands.SlashCommandExecutorDeclaration
import net.perfectdreams.pantufa.PantufaBot
import net.perfectdreams.pantufa.utils.Server

class BanExecutor(
    pantufa: PantufaBot
) : AdminConsoleBungeeExecutor(
    pantufa,
    "dreamnetworkbans.ban",
    "ban",
    Server.PERFECTDREAMS_BUNGEE
) {
    companion object : SlashCommandExecutorDeclaration(BanExecutor::class) {
        override val options = AdminConsoleBungeeExecutor.Companion.Options
    }
}