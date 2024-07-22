package com.sap.pi.valueanalysis.dao

import com.sap.pi.valueanalysis.generated.tables.references.T_SESSION
import org.jooq.DSLContext
import java.util.*

context(DSLContext)
fun findSessionIdByTenant(tenant: UUID): UUID? =
    select(T_SESSION.C_ID)
        .from(T_SESSION)
        .where(T_SESSION.C_TENANT_ID.eq(tenant))
        .limit(1)
        .fetchOne()
        ?.let { it[T_SESSION.C_ID] }
