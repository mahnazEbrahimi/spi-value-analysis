package com.sap.pi.valueanalysis.exception

import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.response.*
import io.ktor.util.*

private val logger = KotlinLogging.logger {}

@OptIn(InternalAPI::class)
fun Application.configureExceptionMapping() {
    install(StatusPages) {
        exception<Throwable> { call, e ->
            var cause = e.rootCause ?: e

            when {
                cause is BusinessException -> call.respondText(text = "${cause.message}", status = cause.code)
                cause is NotFoundException -> call.respondText(text = "${cause.message}", status = HttpStatusCode.NotFound)
                cause is BadRequestException -> call.respondText(text = "${cause.message}", status = HttpStatusCode.BadRequest)
                else -> {
                    logger.error(cause) { "Exception during request processing: ${cause.message}" }
                    call.respondText(text = "An internal error occured", status = HttpStatusCode.InternalServerError)
                }
            }
        }
    }
}
