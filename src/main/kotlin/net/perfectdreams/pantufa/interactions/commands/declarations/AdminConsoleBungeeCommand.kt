package net.perfectdreams.pantufa.interactions.commands.declarations

import net.perfectdreams.discordinteraktions.common.commands.SlashCommandDeclarationWrapper
import net.perfectdreams.discordinteraktions.common.commands.slashCommand
import net.perfectdreams.pantufa.interactions.commands.administration.*

object AdminConsoleBungeeCommand : SlashCommandDeclarationWrapper {
    override fun declaration() = slashCommand(
        "adminconsole",
        "Executa algo no console"
    ) {
        subcommand("advdupeip", "Executa addupeip") {
            executor = AdvDupeIpExecutor
        }

        subcommand("ban", "Executa ban") {
            executor = BanExecutor
        }

        subcommand("checkban", "Executa checkban") {
            executor = CheckBanExecutor
        }

        subcommand("dupeip", "Executa dupeip") {
            executor = DupeIpExecutor
        }

        subcommand("fingerprint", "Executa fingerprint") {
            executor = FingerprintExecutor
        }

        subcommand("geoip", "Executa geoip") {
            executor = GeoIpExecutor
        }

        subcommand("ipban", "Executa ipban") {
            executor = IpBanExecutor
        }

        subcommand("ipunban", "Executa ipunban") {
            executor = IpUnbanExecutor
        }

        subcommand("kick", "Executa kick") {
            executor = KickExecutor
        }

        subcommand("unban", "Executa unban") {
            executor = UnbanExecutor
        }

        subcommand("warn", "Executa warn") {
            executor = UnbanExecutor
        }

        subcommand("unwarn", "Executa unwarn") {
            executor = UnbanExecutor
        }
    }
}