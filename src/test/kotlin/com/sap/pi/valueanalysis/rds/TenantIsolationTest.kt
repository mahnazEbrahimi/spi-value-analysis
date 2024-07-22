package com.sap.pi.valueanalysis.rds

import com.sap.pi.valueanalysis.TestApiExtension
import com.sap.pi.valueanalysis.applicationConfig
import com.sap.pi.valueanalysis.dataSource
import com.sap.pi.valueanalysis.api.UserInformation
import com.zaxxer.hikari.HikariDataSource
import io.kotest.matchers.shouldBe
import org.jooq.DSLContext
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.extension.ExtendWith
import java.util.*

@ExtendWith(TestApiExtension::class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class TenantIsolationTest {

    lateinit var appDataSourceConfig: HikariDataSource
    lateinit var adminDataSource: HikariDataSource
    lateinit var adminContext: DSLContext
    lateinit var appContext: DSLContext

    @BeforeAll
    fun setup() {
        adminDataSource = DataSourceConfig.createConnectionPool(
            applicationConfig.property("datasource.jdbcUrl").getString(),
            "signavio_admin",
            "signavio",
        )

        appDataSourceConfig = DataSourceConfig.createConnectionPool(
            applicationConfig.property("datasource.jdbcUrl").getString(),
            "signavio_app",
            "signavio",
        )
        appContext = DataSourceConfig.createDSLContext(dataSource)

        // connect with signavio_admin user
        adminContext = DataSourceConfig.createDSLContext(adminDataSource)
        adminContext.transaction { trx ->
            with(trx.dsl()) {
                execute(
                    """
            DROP TABLE IF EXISTS t_test;
                    """.trimIndent(),
                )

                // create a table with tenant isolation
                execute(
                    """
            CREATE TABLE IF NOT EXISTS t_test
            (
                c_id          UUID DEFAULT gen_random_uuid() PRIMARY KEY,
                c_subject_id  UUID NOT NULL,
                c_tenant_id   UUID NOT NULL,
                c_description TEXT NOT NULL DEFAULT ''
            )
                    """.trimIndent(),
                )

                execute(
                    """
            ALTER TABLE t_test
            OWNER TO signavio_admin;
                    """.trimIndent(),
                )

                execute(
                    """
            ALTER TABLE t_test
            ENABLE ROW LEVEL SECURITY;
                    """.trimIndent(),
                )

                execute(
                    """
            CREATE POLICY p_default_t_test ON t_test
            FOR ALL TO PUBLIC
                    USING (TRUE) WITH CHECK (TRUE);
                    """.trimIndent(),
                )

                execute(
                    """
            CREATE POLICY p_multitenancy_t_test
            ON t_test AS RESTRICTIVE
                    FOR ALL
                    TO PUBLIC
                    USING (c_tenant_id = (get_session()).c_tenant_id)
            WITH CHECK (c_tenant_id = (get_session()).c_tenant_id);
                    """.trimIndent(),
                )

                // insert data for multiple tenants

                execute(
                    """
            |insert into t_test (c_subject_id, c_tenant_id, c_description)  
            |values('D16EBF21-C652-4B48-B47C-C51366471BB6', '2DF9B2A0-46C3-457C-9437-1ADF9D932ECA', 'test_a')
            |
                    """.trimMargin(),
                )

                execute(
                    """
            |insert into t_test (c_subject_id, c_tenant_id, c_description)  
            |values('F694EDEE-C3C1-4413-9250-F49E82BE4CF9', '7008D2BA-3192-4FD2-81F5-75E19D6F6B31', 'test_b')
            |
                    """.trimMargin(),
                )
            }
        }
    }

    @Test
    fun `signavio_app user automatically gets restricted to only tenant in session`() {
        // connect with siganvio_app user
        appContext.transaction { trx ->
            with(trx.dsl()) {
                // start session for siganvio_app user
                startSession(
                    UserInformation(
                        UUID.fromString("2DF9B2A0-46C3-457C-9437-1ADF9D932ECA"),
                        UUID.randomUUID(),
                    ),
                )
                // query data - make sure only one tenant is returned
                fetch("select * from t_test").size shouldBe 1
            }
        }
    }

    @Test
    fun `signavio_app user can write rows of tenant in session`() {
        // connect with siganvio_app user
        appContext.transaction { trx ->
            with(trx.dsl()) {
                startSession(
                    UserInformation(
                        UUID.fromString("2DF9B2A0-46C3-457C-9437-1ADF9D932ECA"),
                        UUID.randomUUID(),
                    ),
                )

                // query data - make sure no row is returned
                execute(
                    """
            |update t_test  
            |set c_description = 'test_bbb'
            |where c_subject_id = 'D16EBF21-C652-4B48-B47C-C51366471BB6'
            |
                    """.trimMargin(),
                ) shouldBe 1
            }
        }
    }

    @Test
    fun `signavio_app user can not read rows of tenant not in session`() {
        // connect with siganvio_app user
        appContext.transaction { trx ->
            with(trx.dsl()) {
                // start session for siganvio_app user
                startSession(
                    UserInformation(
                        UUID.fromString("2DF9B2A0-46C3-457C-9437-1ADF9D932ECA"),
                        UUID.randomUUID(),
                    ),
                )

                // query data - make sure no row is returned
                fetch(
                    """
            |select * from t_test  
            |where c_subject_id = 'F694EDEE-C3C1-4413-9250-F49E82BE4CF9'
            |
                    """.trimMargin(),
                ).size shouldBe 0
            }
        }
    }

    @Test
    fun `signavio_app user can not write rows of tenant not in session`() {
        // connect with siganvio_app user
        appContext.transaction { trx ->
            with(trx.dsl()) {
                // start session for siganvio_app user
                startSession(
                    UserInformation(
                        UUID.fromString("2DF9B2A0-46C3-457C-9437-1ADF9D932ECA"),
                        UUID.randomUUID(),
                    ),
                )

                // query data - make sure no row is returned
                execute(
                    """
            |update t_test  
            |set c_description = 'test_bbb'
            |where c_subject_id = 'F694EDEE-C3C1-4413-9250-F49E82BE4CF9'
            |
                    """.trimMargin(),
                ) shouldBe 0
            }
        }
    }

    @AfterAll
    fun cleanup() {
        adminContext.execute(
            """
            DROP TABLE IF EXISTS t_test;
            """.trimIndent(),
        )
        adminDataSource.close()
    }
}
