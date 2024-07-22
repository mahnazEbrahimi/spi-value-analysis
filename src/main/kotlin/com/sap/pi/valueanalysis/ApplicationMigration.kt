package com.sap.pi.valueanalysis

import com.sap.pi.valueanalysis.rds.DataSourceConfig
import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.server.config.*
import org.flywaydb.core.Flyway

private val logger = KotlinLogging.logger {}
fun migrate(appConfig: ApplicationConfig) {
    try {
        val dataSource = DataSourceConfig.createConnectionPool(appConfig)

        val flyway = Flyway.configure()
            .dataSource(dataSource)
            .baselineVersion("-1")
            .baselineOnMigrate(true)
            .load()

        flyway.migrate()

    } catch (e: Exception) {
        logger.error(e) { "Error during Database Schema Migration" }
        throw RuntimeException("Error during Database Schema Migration", e)
    }
}

