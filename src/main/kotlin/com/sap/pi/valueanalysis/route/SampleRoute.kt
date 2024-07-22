package com.sap.pi.valueanalysis.route

import com.sap.pi.valueanalysis.api.getUserInformation
import com.sap.pi.valueanalysis.util.RequestContext
import com.sap.pi.valueanalysis.dataSource
import com.sap.pi.valueanalysis.rds.DataSourceConfig
import com.sap.pi.valueanalysis.rds.startSession
import com.sap.pi.valueanalysis.service.getSessionIdByTenant
import org.jooq.kotlin.coroutines.transactionCoroutine
import org.jooq.DSLContext
import kotlin.reflect.KSuspendFunction2

import guru.zoroark.tegral.openapi.dsl.schema
import guru.zoroark.tegral.openapi.ktor.describe
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.util.pipeline.*



fun Application.configureRoutes() {
    routing {
        route("/spi-value-analysis") {
            authenticate {
                get {
                    runInRequestScope(this::getInfo)
                }
                describe {
                    description = "Get user information"
                    security("userJwt")
                    200 response {
                        json { schema<String>() }
                    }
                }
            }
        }
    }
}

internal suspend fun PipelineContext<Unit, ApplicationCall>.runInRequestScope(handler: KSuspendFunction2<DSLContext, RequestContext, Unit>) {
    val dslContext = DataSourceConfig.createDSLContext(dataSource)
    val userInfo = getUserInformation(call.principal()!!)
    val authToken = call.request.header("authorization")!!.removePrefix("Bearer ")
    val requestContext = RequestContext(userInfo, authToken)
    dslContext.transactionCoroutine { trx ->
        trx.dsl().startSession(userInfo)
        handler(trx.dsl(), requestContext)
    }
}
context(DSLContext, RequestContext)
private suspend fun PipelineContext<Unit, ApplicationCall>.getInfo() {
    val sessionId = getSessionIdByTenant(user.tenantId)
    call.respond(sessionId)
}
