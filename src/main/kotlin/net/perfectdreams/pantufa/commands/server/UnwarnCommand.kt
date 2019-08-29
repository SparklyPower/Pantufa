package net.perfectdreams.pantufa.commands.server

import net.perfectdreams.pantufa.utils.Server

class UnwarnCommand : RemoteCommandExecutorCommand(
		"unwarn",
		listOf(),
		"unwarn",
		Server.PERFECTDREAMS_BUNGEE
)