package net.perfectdreams.pantufa.commands.server

import net.perfectdreams.pantufa.utils.Server

class IpBanCommand : RemoteCommandExecutorCommand(
		"ipban",
		listOf("banirip"),
		"ipban",
		Server.PERFECTDREAMS_BUNGEE
)