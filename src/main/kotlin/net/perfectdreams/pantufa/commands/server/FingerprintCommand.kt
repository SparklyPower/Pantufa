package net.perfectdreams.pantufa.commands.server

import net.perfectdreams.pantufa.utils.Server

class FingerprintCommand : RemoteCommandExecutorCommand(
		"fingerprint",
		listOf(),
		"fingerprint",
		Server.PERFECTDREAMS_BUNGEE
)