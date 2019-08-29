package net.perfectdreams.pantufa.commands.server

import net.perfectdreams.pantufa.utils.Server

class GeoIpCommand : RemoteCommandExecutorCommand(
		"geoip",
		listOf(),
		"geoip",
		Server.PERFECTDREAMS_BUNGEE
)