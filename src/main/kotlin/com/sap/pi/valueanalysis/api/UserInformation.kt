package com.sap.pi.valueanalysis.api

import com.sap.pi.valueanalysis.util.getUuidFromStringOrException
import io.ktor.server.auth.jwt.*
import java.util.*

data class UserInformation(
    val tenantId: UUID,
    val userId: UUID? = null,
)

fun getUserInformation(principal: JWTPrincipal): UserInformation {
    return UserInformation(
        getUuidFromStringOrException(principal.payload.getClaim("ten").asString()),
        getUuidFromStringOrException(principal.payload.getClaim("uid").asString()),
    )
}
