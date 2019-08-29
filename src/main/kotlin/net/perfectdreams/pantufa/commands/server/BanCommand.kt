package net.perfectdreams.pantufa.commands.server

import net.perfectdreams.pantufa.utils.Server

class BanCommand : RemoteCommandExecutorCommand(
		"ban",
		listOf("banir"),
		"ban",
		Server.PERFECTDREAMS_BUNGEE
)