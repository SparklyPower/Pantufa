package net.perfectdreams.pantufa.commands.server

import net.perfectdreams.pantufa.utils.Server

class KickCommand : RemoteCommandExecutorCommand(
		"kick",
		listOf(),
		"kick",
		Server.PERFECTDREAMS_BUNGEE
)