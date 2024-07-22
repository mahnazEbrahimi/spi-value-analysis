package com.sap.pi.valueanalysis.util

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.sap.pi.valueanalysis.applicationConfig
import java.util.*

fun getCurrentRegion(): String {
    return when (val environment = getCurrentEnvironment()) {
        "prod_cloud_os_eu" -> "EU"
        "prod_cloud_os_au" -> "AU"
        "prod_cloud_os_us" -> "US"
        "prod_cloud_os_ca" -> "CA"
        "prod_cloud_os_jp" -> "JP"
        "prod_cloud_os_kr" -> "KR"
        "prod_cloud_os_sgp" -> "SGP"
        "staging_cloud_os_eu" -> "STAGING"
        "jpmc_prod" -> "JPMC"
        "bugbounty_prod" -> "BUGBOUNTY"
        "dev_cloud_os_eu" -> "DEV"
        else -> throw IllegalStateException("export not implemented for ENVIRONMENT $environment")
    }
}

fun getAdminToken(authToken: String): String {
    return JWT.create()
        .withClaim("uid", getClaimFromToken(authToken, "uid"))
        .withClaim("ano", false)
        .withClaim("gue", false)
        .withClaim("sub", getClaimFromToken(authToken, "sub"))
        .withIssuer("signavio")
        .withClaim("adm", true)
        .withClaim("ten", getClaimFromToken(authToken, "ten"))
        .withIssuedAt(Date(System.currentTimeMillis()))
        .withExpiresAt(Date(System.currentTimeMillis() + 60 * 1000))
        .sign(Algorithm.HMAC256(applicationConfig.property("jwt.signingKey").getString()))
}

fun getClaimFromToken(token: String?, claim: String): String? {
    return if (token != null) {
        JWT.decode(token.removePrefix("Bearer ")).getClaim(claim).asString()
    } else {
        null
    }
}

fun getCurrentEnvironment(): String {
    return applicationConfig.property("pi.environment").getString()
}
