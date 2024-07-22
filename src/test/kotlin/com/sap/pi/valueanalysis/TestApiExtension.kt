package com.sap.pi.valueanalysis

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.github.tomakehurst.wiremock.WireMockServer
import com.sap.pi.valueanalysis.api.UserInformation
import com.sap.pi.valueanalysis.rds.DataSourceConfig
import com.typesafe.config.ConfigFactory
import com.sap.pi.valueanalysis.util.loadFeatureToggles
import com.sap.pi.valueanalysis.util.loadReleaseToggles
import io.ktor.http.*
import io.ktor.server.config.*
import org.junit.jupiter.api.extension.BeforeAllCallback
import org.junit.jupiter.api.extension.ExtensionContext
import java.io.File
import java.io.FileWriter
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Paths
import java.util.*
import java.util.stream.Collectors

class TestApiExtension : BeforeAllCallback {

    override fun beforeAll(context: ExtensionContext?) {
        applicationConfig = HoconApplicationConfig(ConfigFactory.load("application-test.conf"))
        featureConfiguration = loadFeatureToggles()
        releaseConfiguration = loadReleaseToggles()
        dataSource = DataSourceConfig.createConnectionPool(applicationConfig)
    }
}

fun updateConfigFile(externalServicesMockServer: WireMockServer): ApplicationConfig {
    externalServicesMockServer.start()
    val tempFile = File("preprocessed_test.conf")

    Files.lines(Paths.get("src/test/resources/application-test.conf"), StandardCharsets.UTF_8).use { lines ->
        FileWriter(tempFile).use { writer ->
            val content = lines.map { line: String ->
                line.replace(
                    "\${?WIREMOCK_URL}",
                    externalServicesMockServer.baseUrl()
                        .quote(),
                )
            }
                .collect(Collectors.joining("\n"))
            writer.write(content)
        }
    }
    val config = ConfigFactory.parseFile(tempFile).resolve()
    tempFile.deleteOnExit()
    return HoconApplicationConfig(config)
}

fun createJWTToken(user: UserInformation): String = JWT.create()
    .withClaim("ano", false)
    .withClaim("gue", false)
    .withClaim("sub", "subject-1")
    .withIssuer("signavio")
    .withClaim("adm", true)
    .withClaim("ten", user.tenantId.toString().replace("-", ""))
    .withClaim("uid", user.userId.toString().replace("-", ""))
    .withIssuedAt(Date(System.currentTimeMillis()))
    .withExpiresAt(Date(System.currentTimeMillis() + 60 * 100000))
    .sign(Algorithm.HMAC256("test123asdf1!"))
