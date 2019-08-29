package net.perfectdreams.pantufa.commands.server

import net.perfectdreams.pantufa.utils.Server

class WarnCommand : RemoteCommandExecutorCommand(
		"warn",
		listOf(),
		"warn",
		Server.PERFECTDREAMS_BUNGEE
)