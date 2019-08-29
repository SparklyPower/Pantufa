package net.perfectdreams.pantufa

import net.perfectdreams.pantufa.utils.PantufaConfig
import java.io.File

object PantufaLauncher {
	@JvmStatic
	fun main(args: Array<String>) {
		val config = File("./pantufa.txt").readLines()

		val token = config[0]
		val postgreSqlIp = config[1]
		val postgreSqlPort = config[2]
		val postgreSqlUsername = config[3]
		val postgreSqlPassword = config[4]

		val postgreSqlCraftConomyIp = config[5]
		val postgreSqlCraftConomyPort = config[6]
		val postgreSqlCraftConomyUsername = config[7]
		val postgreSqlCraftConomyPassword = config[8]

		val pantufa = Pantufa(
				PantufaConfig(
						token,
						PantufaConfig.PostgreSqlConfig(
								postgreSqlIp,
								postgreSqlPort.toInt(),
								postgreSqlUsername,
								postgreSqlPassword
						),
						PantufaConfig.PostgreSqlConfig(
								postgreSqlCraftConomyIp,
								postgreSqlCraftConomyPort.toInt(),
								postgreSqlCraftConomyUsername,
								postgreSqlCraftConomyPassword
						)
				)
		)

		pantufa.start()
	}
}