package net.perfectdreams.pantufa.network

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import net.perfectdreams.pantufa.pantufa
import org.jetbrains.exposed.sql.Database

object Databases {
    val hikariConfigPantufa by lazy {
        val config = HikariConfig()
        config.jdbcUrl = "jdbc:postgresql://${pantufa.config.postgreSqlSparklyPower.ip}:${pantufa.config.postgreSqlSparklyPower.port}/${pantufa.config.postgreSqlSparklyPower.databaseName}"
        config.username = pantufa.config.postgreSqlSparklyPower.username
        config.password = pantufa.config.postgreSqlSparklyPower.password
        config.driverClassName = "org.postgresql.Driver"
        // Exposed uses autoCommit = false, so we need to set this to false to avoid HikariCP resetting the connection to
        // autoCommit = true when the transaction goes back to the pool, because resetting this has a "big performance impact"
        // https://stackoverflow.com/a/41206003/7271796
        config.isAutoCommit = false

        config.maximumPoolSize = 4
        config.addDataSourceProperty("cachePrepStmts", "true")
        config.addDataSourceProperty("prepStmtCacheSize", "250")
        config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048")
        return@lazy config
    }

    val dataSourcePantufa by lazy { HikariDataSource(hikariConfigPantufa); }
    val sparklyPower by lazy { Database.connect(dataSourcePantufa); }

    val hikariConfigLuckPerms by lazy {
        val config = HikariConfig()
        config.jdbcUrl = "jdbc:postgresql://${pantufa.config.postgreSqlSparklyPower.ip}:${pantufa.config.postgreSqlSparklyPower.port}/${pantufa.config.postgreSqlLuckPerms.databaseName}"
        config.username = pantufa.config.postgreSqlSparklyPower.username
        config.password = pantufa.config.postgreSqlSparklyPower.password
        config.driverClassName = "org.postgresql.Driver"
        // Exposed uses autoCommit = false, so we need to set this to false to avoid HikariCP resetting the connection to
        // autoCommit = true when the transaction goes back to the pool, because resetting this has a "big performance impact"
        // https://stackoverflow.com/a/41206003/7271796
        config.isAutoCommit = false

        config.maximumPoolSize = 4
        config.addDataSourceProperty("cachePrepStmts", "true")
        config.addDataSourceProperty("prepStmtCacheSize", "250")
        config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048")
        return@lazy config
    }

    val dataSourceLuckPerms by lazy { HikariDataSource(hikariConfigLuckPerms) }
    val sparklyPowerLuckPerms by lazy { Database.connect(dataSourceLuckPerms) }

    val hikariConfigLoritta by lazy {
        val config = HikariConfig()
        config.jdbcUrl = "jdbc:postgresql://${pantufa.config.postgreSqlLoritta.ip}:${pantufa.config.postgreSqlLoritta.port}/${pantufa.config.postgreSqlLoritta.databaseName}"
        config.username = pantufa.config.postgreSqlLoritta.username
        config.password = pantufa.config.postgreSqlLoritta.password
        config.driverClassName = "org.postgresql.Driver"
        // Exposed uses autoCommit = false, so we need to set this to false to avoid HikariCP resetting the connection to
        // autoCommit = true when the transaction goes back to the pool, because resetting this has a "big performance impact"
        // https://stackoverflow.com/a/41206003/7271796
        config.isAutoCommit = false

        config.maximumPoolSize = 4
        config.addDataSourceProperty("cachePrepStmts", "true")
        config.addDataSourceProperty("prepStmtCacheSize", "250")
        config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048")
        return@lazy config
    }

    val dataSourceLoritta by lazy { HikariDataSource(hikariConfigLoritta) }
    val loritta by lazy { Database.connect(dataSourceLoritta) }

    val hikariConfigCraftConomy by lazy {
        val config = HikariConfig()
        config.jdbcUrl = "jdbc:mysql://${pantufa.config.mariaDbCraftConomy.ip}:${pantufa.config.mariaDbCraftConomy.port}/${pantufa.config.mariaDbCraftConomy.databaseName}"
        config.username = pantufa.config.mariaDbCraftConomy.username
        config.password = pantufa.config.mariaDbCraftConomy.password
        // Exposed uses autoCommit = false, so we need to set this to false to avoid HikariCP resetting the connection to
        // autoCommit = true when the transaction goes back to the pool, because resetting this has a "big performance impact"
        // https://stackoverflow.com/a/41206003/7271796
        config.isAutoCommit = false

        config.maximumPoolSize = 4
        config.addDataSourceProperty("cachePrepStmts", "true")
        config.addDataSourceProperty("prepStmtCacheSize", "250")
        config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048")
        return@lazy config
    }

    val dataSourceCraftConomy by lazy { HikariDataSource(hikariConfigCraftConomy) }
    val craftConomy by lazy { Database.connect(dataSourceCraftConomy) }
}