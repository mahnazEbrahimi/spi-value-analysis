package com.sap.pi.valueanalysis.health

import com.codahale.metrics.health.HealthCheckRegistry
import io.ktor.client.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.metrics.micrometer.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.micrometer.core.instrument.*
import io.micrometer.core.instrument.binder.MeterBinder
import io.micrometer.core.instrument.binder.jvm.ClassLoaderMetrics
import io.micrometer.core.instrument.binder.jvm.JvmGcMetrics
import io.micrometer.core.instrument.binder.jvm.JvmMemoryMetrics
import io.micrometer.core.instrument.binder.jvm.JvmThreadMetrics
import io.micrometer.core.instrument.binder.logging.LogbackMetrics
import io.micrometer.core.instrument.binder.system.ProcessorMetrics
import io.micrometer.prometheus.PrometheusConfig
import io.micrometer.prometheus.PrometheusMeterRegistry
import kotlinx.coroutines.*
import kotlinx.coroutines.debug.DebugProbes
import com.codahale.metrics.health.HealthCheck
import java.util.stream.Collectors
import com.sap.pi.valueanalysis.dataSource
import org.flywaydb.core.Flyway
import org.flywaydb.core.api.MigrationInfo
import java.util.*


lateinit var flyway: Flyway

object HealthCheckService {
    val registry: HealthCheckRegistry = HealthCheckRegistry()
}

@OptIn(ExperimentalCoroutinesApi::class)
fun Application.healthAndMetrics() {
    HealthCheckService.registry.register("flywayHealthCheck", flywayHealthCheck)

    // JVM agent to track existing coroutines
    DebugProbes.enableCreationStackTraces = false
    DebugProbes.install()

    val appMicrometerRegistry = PrometheusMeterRegistry(PrometheusConfig.DEFAULT)
    install(MicrometerMetrics) {
        registry = appMicrometerRegistry
        meterBinders = listOf(
            JvmMemoryMetrics(),
            JvmGcMetrics(),
            ProcessorMetrics(),
            LogbackMetrics(),
            ClassLoaderMetrics(),
            JvmThreadMetrics(),
            KtorCoroutineMetrics(),
        )
    }

    routing {
        get("/health") {
            val results = HealthCheckService.registry.runHealthChecks()
            val checkResult = results.flatMap { (key, values) -> listOf(key).plus(values) }.toString()
            call.respondText(checkResult, ContentType.Application.Json)
        }
        get("/metrics") {
            if (isAdminPort(call)) {
                call.respond(appMicrometerRegistry.scrape())
            }
        }
    }
}
private fun isAdminPort(call: ApplicationCall): Boolean {
    val adminPort = 8090
    return call.request.local.localPort == adminPort
}

val flywayHealthCheck = object : HealthCheck() {
    override fun check(): Result {
        if (!::flyway.isInitialized) {
            flyway = Flyway.configure()
                .dataSource(dataSource)
                .load()
        }

        val pendingMigrations = flyway.info().pending()

        return if (pendingMigrations.isEmpty()) {
            Result.healthy()
        } else {
            val pendingVersions = Arrays.stream(pendingMigrations)
                .map { x: MigrationInfo ->
                    x.version.version
                }
                .collect(Collectors.joining(","))
            Result.unhealthy("Pending migrations: $pendingVersions")
        }
    }
}
class KtorCoroutineMetrics : MeterBinder {

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun bindTo(registry: MeterRegistry) {
        Gauge.builder("ktor_active_coroutines") {
            DebugProbes.dumpCoroutinesInfo().size.toDouble()
        }
            .description("Number of active ktor coroutine's threads")
            .register(registry)

    }
}
