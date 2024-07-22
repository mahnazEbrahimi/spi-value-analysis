package com.sap.pi.valueanalysis.rds

import com.amazonaws.auth.DefaultAWSCredentialsProviderChain
import com.amazonaws.services.rds.auth.GetIamAuthTokenRequest
import com.amazonaws.services.rds.auth.RdsIamAuthTokenGenerator
import com.zaxxer.hikari.HikariDataSource
import io.ktor.server.config.*
import org.jooq.DSLContext
import org.jooq.SQLDialect
import org.jooq.impl.DSL
import java.io.Closeable
import java.net.URI
import java.sql.Connection
import java.util.concurrent.TimeUnit
import javax.sql.DataSource

data class DataSourceConfig(val jdbcUrl: String, val username: String) {

    companion object {
        fun createConnectionPool(
            url: String,
            user: String,
            dbPassword: String,
        ): HikariDataSource {
            return HikariDataSource().apply {
                jdbcUrl = url
                username = user
                password = dbPassword
                poolName = "ApplicationConnectionsPool"
                minimumIdle = 5
                maximumPoolSize = 10
                connectionTestQuery = "select 1;"
            }
        }

        fun createConnectionPool(
            applicationConfig: ApplicationConfig,
        ): DataSource {
            val jdbcUrl = applicationConfig.property("datasource.jdbcUrl").getString()
            val username = applicationConfig.property("datasource.username").getString()
            val awsRdsAuthTokenEnabled =
                applicationConfig.property("datasource.awsRdsAuthTokenEnabled").getString().toBoolean()

            return if (awsRdsAuthTokenEnabled) {
                val region = applicationConfig.property("datasource.awsRegion").getString()
                RdsIamRefreshableDataSource(
                    createConnectionPool(
                        jdbcUrl,
                        username,
                        getRdsToken(jdbcUrl, username, region),
                    ),
                    region,
                )
            } else {
                createConnectionPool(
                    jdbcUrl,
                    username,
                    applicationConfig.property("datasource.password").getString(),
                )
            }
        }

        fun createDSLContext(dataSource: DataSource): DSLContext {
            return DSL.using(dataSource, SQLDialect.POSTGRES)
        }

        fun closeDataSource(dataSource: HikariDataSource) {
            dataSource.close()
        }
    }
}

class RdsIamRefreshableDataSource(private val dataSource: HikariDataSource, private val region: String) : Closeable, DataSource by dataSource {

    private var lastRefreshTimestamp = 0L
    private val refreshInterval: Long = TimeUnit.MINUTES.toMillis(10)

    override fun getConnection(): Connection {
        refreshIfNeeded()
        return dataSource.connection
    }

    override fun getConnection(username: String?, password: String?): Connection {
        refreshIfNeeded()
        return dataSource.getConnection(username, password)
    }

    private fun isDataSourceExpired(): Boolean {
        return (lastRefreshTimestamp + refreshInterval) < System.currentTimeMillis()
    }

    @Synchronized
    private fun refreshIfNeeded() {
        if (isDataSourceExpired()) {
            lastRefreshTimestamp = System.currentTimeMillis()
            dataSource.password = getRdsToken(dataSource.jdbcUrl, dataSource.username, region)
        }
    }

    override fun close() {
        dataSource.close()
    }
}

fun getRdsToken(jdbcUrl: String, username: String, region: String): String {
    val uri = URI.create(jdbcUrl.replaceFirst(".*://".toRegex(), "jdbc://"))
    val host = uri.host
    val port = if (uri.port > 0) uri.port else 5432

    val iamTokenGenerator = RdsIamAuthTokenGenerator.builder()
        .credentials(DefaultAWSCredentialsProviderChain())
        .region(region)
        .build()

    val request = GetIamAuthTokenRequest.builder()
        .hostname(host)
        .port(port)
        .userName(username)
        .build()

    return iamTokenGenerator.getAuthToken(request)
}
