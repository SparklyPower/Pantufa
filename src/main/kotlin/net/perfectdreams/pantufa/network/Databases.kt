package net.perfectdreams.pantufa.network

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import net.perfectdreams.pantufa.pantufa
import org.jetbrains.exposed.sql.Database

object Databases {
    val hikariConfigLoritta by lazy {
        val config = HikariConfig()
        config.jdbcUrl = "jdbc:postgresql://${pantufa.config.postgreSql.ip}:${pantufa.config.postgreSql.port}/sparklypower"
        config.username = pantufa.config.postgreSql.username
        config.password = pantufa.config.postgreSql.password
        config.driverClassName = "org.postgresql.Driver"

        config.maximumPoolSize = 16
        config.addDataSourceProperty("cachePrepStmts", "true")
        config.addDataSourceProperty("prepStmtCacheSize", "250")
        config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048")
        return@lazy config
    }

    val dataSourceLoritta by lazy { HikariDataSource(hikariConfigLoritta) }
    val sparklyPower by lazy { Database.connect(dataSourceLoritta) }
}