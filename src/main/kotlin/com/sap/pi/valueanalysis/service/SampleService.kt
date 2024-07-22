package com.sap.pi.valueanalysis.service

import com.sap.pi.valueanalysis.util.RequestContext
import io.ktor.server.plugins.*
import java.util.*
import com.sap.pi.valueanalysis.dao.findSessionIdByTenant
import org.jooq.DSLContext


context(DSLContext, RequestContext)
fun getSessionIdByTenant(tenant: UUID): UUID {
    return findSessionIdByTenant(tenant)
        ?: throw NotFoundException()
}

