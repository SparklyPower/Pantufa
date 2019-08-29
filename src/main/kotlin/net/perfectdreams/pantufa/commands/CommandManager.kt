package net.perfectdreams.pantufa.commands

import net.perfectdreams.pantufa.commands.server.*
import net.perfectdreams.pantufa.commands.vanilla.utils.PingCommand

class CommandManager {
	val commands = mutableListOf<AbstractCommand>()

	init {
		commands.add(PingCommand())
		commands.add(MoneyCommand())
		commands.add(OnlineCommand())
		commands.add(PanelaCommand())
		commands.add(LSXCommand())
		commands.add(RegistrarCommand())
		commands.add(TpsCommand())
		commands.add(ExecuteCommand())

		commands.add(BanCommand())
		commands.add(UnbanCommand())
		commands.add(KickCommand())
		commands.add(WarnCommand())
		commands.add(UnwarnCommand())
		commands.add(CheckBanCommand())
		commands.add(DupeIpCommand())
		commands.add(FingerprintCommand())
		commands.add(GeoIpCommand())

		commands.add(IpBanCommand())
		commands.add(IpUnbanCommand())
	}
}