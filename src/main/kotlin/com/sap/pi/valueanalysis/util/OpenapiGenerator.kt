package com.sap.pi.valueanalysis.util

import guru.zoroark.tegral.openapi.dsl.toJson
import guru.zoroark.tegral.openapi.ktor.openApi
import io.ktor.server.application.*
import java.io.File

fun Application.openApiWriter() {
    environment.monitor.subscribe(ApplicationStarted) { application ->
        application.environment.log.info("Writing openapi.json")
        application.openApi.buildOpenApiDocument().toJson()
            .also { File("build/openapi.json").writeText(it) }
    }
}
