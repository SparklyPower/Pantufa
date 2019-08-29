package net.perfectdreams.pantufa.commands.server

import net.perfectdreams.pantufa.utils.Server

class IpUnbanCommand : RemoteCommandExecutorCommand(
		"ipunban",
		listOf("desbanirip"),
		"ipunban",
		Server.PERFECTDREAMS_BUNGEE
)