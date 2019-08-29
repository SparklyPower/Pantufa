package net.perfectdreams.pantufa.commands.server

import net.perfectdreams.pantufa.utils.Server

class DupeIpCommand : RemoteCommandExecutorCommand(
		"dupeip",
		listOf(),
		"dupeip",
		Server.PERFECTDREAMS_BUNGEE
)