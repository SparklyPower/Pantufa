package net.perfectdreams.pantufa.utils

class PantufaConfig(
        val token: String,
        val postgreSql: PostgreSqlConfig,
        val mariaDbCraftConomy: PostgreSqlConfig
) {
    class PostgreSqlConfig(
            val ip: String,
            val port: Int,
            val username: String,
            val password: String
    )
}