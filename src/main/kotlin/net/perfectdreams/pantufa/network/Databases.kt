package net.perfectdreams.pantufa.network

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import net.perfectdreams.pantufa.pantufa
import org.jetbrains.exposed.sql.Database

object Databases {
    val hikariConfigPantufa by lazy {
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

    val dataSourcePantufa by lazy { HikariDataSource(hikariConfigPantufa) }
    val sparklyPower by lazy { Database.connect(dataSourcePantufa) }

    val hikariConfigLuckPerms by lazy {
        val config = HikariConfig()
        config.jdbcUrl = "jdbc:postgresql://${pantufa.config.postgreSql.ip}:${pantufa.config.postgreSql.port}/sparklypower_luckperms"
        config.username = pantufa.config.postgreSql.username
        config.password = pantufa.config.postgreSql.password
        config.driverClassName = "org.postgresql.Driver"

        config.maximumPoolSize = 16
        config.addDataSourceProperty("cachePrepStmts", "true")
        config.addDataSourceProperty("prepStmtCacheSize", "250")
        config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048")
        return@lazy config
    }

    val dataSourceLuckPerms by lazy { HikariDataSource(hikariConfigLuckPerms) }
    val sparklyPowerLuckPerms by lazy { Database.connect(dataSourceLuckPerms) }

    val hikariConfigLoritta by lazy {
        val config = HikariConfig()
        config.jdbcUrl = "jdbc:postgresql://158.69.121.127:${pantufa.config.postgreSql.port}/loritta"
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
    val loritta by lazy { Database.connect(dataSourceLoritta) }

    val hikariConfigCraftConomy by lazy {
        val config = HikariConfig()
        config.jdbcUrl = "jdbc:mysql://10.0.0.7:${pantufa.config.mariaDbCraftConomy.port}/sparklypower_survival"
        config.username = pantufa.config.mariaDbCraftConomy.username
        config.password = pantufa.config.mariaDbCraftConomy.password

        config.maximumPoolSize = 16
        config.addDataSourceProperty("cachePrepStmts", "true")
        config.addDataSourceProperty("prepStmtCacheSize", "250")
        config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048")
        return@lazy config
    }

    val dataSourceCraftConomy by lazy { HikariDataSource(hikariConfigCraftConomy) }
    val craftConomy by lazy { Database.connect(dataSourceCraftConomy) }
}