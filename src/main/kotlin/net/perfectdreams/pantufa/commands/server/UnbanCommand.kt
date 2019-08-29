package net.perfectdreams.pantufa.commands.server

import net.perfectdreams.pantufa.utils.Server

class UnbanCommand : RemoteCommandExecutorCommand(
		"unban",
		listOf(),
		"unban",
		Server.PERFECTDREAMS_BUNGEE
)