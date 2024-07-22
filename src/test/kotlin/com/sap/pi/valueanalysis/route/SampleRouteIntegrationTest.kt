package com.sap.pi.valueanalysis.route


import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.kotlinModule
import com.fasterxml.jackson.module.kotlin.readValue
import io.kotest.matchers.shouldNotBe
import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.common.Slf4jNotifier
import com.github.tomakehurst.wiremock.core.WireMockConfiguration
import com.github.tomakehurst.wiremock.junit.WireMockRule
import com.sap.pi.valueanalysis.TestApiExtension
import com.sap.pi.valueanalysis.api.UserInformation
import com.sap.pi.valueanalysis.createJWTToken
import com.sap.pi.valueanalysis.updateConfigFile
import com.sap.pi.valueanalysis.util.*
import io.kotest.matchers.shouldBe
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.config.*
import io.ktor.server.testing.*
import io.mockk.*
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.extension.ExtendWith
import java.util.*

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ExtendWith(TestApiExtension::class)
class SampleRoutesIntegrationTest {

    companion object {
        @JvmStatic
        private lateinit var pexObjectMapper: ObjectMapper
        private val user = UserInformation(UUID.randomUUID(), UUID.randomUUID())
        private val token = "Bearer ${createJWTToken(user)}"

        val externalServicesMockServer: WireMockServer = WireMockRule(
            WireMockConfiguration
                .wireMockConfig()
                .dynamicPort().notifier(Slf4jNotifier(true)),
        )

        @BeforeAll
        @JvmStatic
        fun init() {
            externalServicesMockServer.start()
            pexObjectMapper = ObjectMapper()
                .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
                .registerModules(
                    kotlinModule(),
                    JavaTimeModule(),
                    SimpleModule()
                        .addSerializer(UUID::class.java, UUIDWithoutDashesSerializer())
                        .addDeserializer(UUID::class.java, DashTolerantUUIDDeserializer()),
                )
        }
    }

    @AfterAll
    fun stopAll() {
        externalServicesMockServer.stop()
    }


    @Test
    fun `find session by id`() = testApplication {
        environment {
            config = updateConfigFile(externalServicesMockServer)
        }

        // Act
        val response = makeFindRequest(client)
        val sessionId: UUID = pexObjectMapper.readValue(response.bodyAsText())

        // Assert
        sessionId shouldNotBe null
    }

    


    private suspend fun makeFindRequest(client: HttpClient): HttpResponse {
        return client.get {
            url {
                path("/spi-value-analysis")
            }
            headers {
                append(HttpHeaders.Authorization, token)
                append(HttpHeaders.ContentType, ContentType.Application.Json)
            }
        }
    }

}
