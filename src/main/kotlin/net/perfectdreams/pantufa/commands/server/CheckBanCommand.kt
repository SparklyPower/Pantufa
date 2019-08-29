package net.perfectdreams.pantufa.commands.server

import net.perfectdreams.pantufa.utils.Server

class CheckBanCommand : RemoteCommandExecutorCommand(
		"checkban",
		listOf(),
		"checkban",
		Server.PERFECTDREAMS_BUNGEE
)