package com.sap.pi.valueanalysis.rds

import com.sap.pi.valueanalysis.TestApiExtension
import com.sap.pi.valueanalysis.dataSource
import io.kotest.matchers.ints.shouldBeGreaterThan
import io.kotest.matchers.shouldBe
import org.jooq.impl.DSL
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(TestApiExtension::class)
class DataSourceConfigTest {

    private val adminTables = setOf("flyway_schema_history", "key_vault", "t_session")

    @Test
    fun `When the schema is initially base tables should have correct grant`() {
        val dslContext = DataSourceConfig.createDSLContext(dataSource)
        dslContext.transaction { trx ->
            val records =
                trx.dsl().select(DSL.field("grantee"), DSL.field("privilege_type"), DSL.field(("table_name")))
                    .from("information_schema.role_table_grants")
                    .where(
                        DSL.field("table_name").`in`(adminTables)
                            .and(DSL.field("grantee").notEqual("signavio_admin")),
                    ).fetch()

            val granted = records.filter { record ->
                record.getValue(1).equals("SELECT") &&
                    record.getValue(2).equals("flyway_schema_history") &&
                    record.getValue(0).equals("signavio_app")
            }

            val notGranted = records.filter { record ->
                record.getValue(1).equals("SELECT") &&
                    record.getValue(2).equals("key_vault") &&
                    record.getValue(0).equals("signavio_app")
            }

            granted.size shouldBeGreaterThan 0
            notGranted.size shouldBe 0
        }
    }
}
