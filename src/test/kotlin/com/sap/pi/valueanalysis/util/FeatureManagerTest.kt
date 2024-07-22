package com.sap.pi.valueanalysis.util

import com.sap.pi.valueanalysis.TestApiExtension
import io.kotest.matchers.shouldBe
import io.ktor.server.testing.*
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.extension.ExtendWith
import java.util.*

@ExtendWith(TestApiExtension::class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class FeatureManagerTest {

    private val validTenant = UUID.fromString("12341234-1234-1234-1234-1234567890ab")
    private val invalidTenant = UUID.fromString("12345678-1234-1234-1234-1234567890ab")
    private lateinit var mockedLabel: FeatureLabels

    @BeforeAll
    fun init() {
        mockedLabel = mockk()
    }

    @Test
    fun `feature manager should return true for an active label for a tenant`() = testApplication {
        every { mockedLabel.name } returns "SWITCH_TO_NEW_SERVICE"
        val enabled = isFeatureActive(mockedLabel, validTenant)
        enabled shouldBe true
    }

    @Test
    fun `feature manager should return true for an active label for a tenant when current environment is null`() = testApplication {
        every { mockedLabel.name } returns "SWITCH_TO_NEW_SERVICE"
        mockkStatic("com.sap.pi.valueanalysis.util.GeneralUtilKt")
        every { getCurrentEnvironment() } returns ""
        val enabled = isFeatureActive(mockedLabel, validTenant)
        enabled shouldBe true
    }


    @Test
    fun `feature manager should return true when feature is active for all`() = testApplication {
        every { mockedLabel.name } returns "ACTIVE_FOR_ALL"
        val enabled = isFeatureActive(mockedLabel, validTenant)
        enabled shouldBe true
    }

    @Test
    fun `feature manager should return true when feature is active for release`() = testApplication {
        every { mockedLabel.name } returns "ACTIVE_FOR_RELEASE"
        val enabled = isFeatureActive(mockedLabel, validTenant)
        enabled shouldBe true
    }

    @Test
    fun `feature manager should return true when feature is active for all in release`() = testApplication {
        every { mockedLabel.name } returns "ACTIVE_FOR_ALL_RELEASE"
        val enabled = isFeatureActive(mockedLabel, validTenant)
        enabled shouldBe true
    }

    @Test
    fun `feature manager should return false when feature is not active for tenant in release`() = testApplication {
        every { mockedLabel.name } returns "NOT_ACTIVE_FOR_RELEASE"
        val enabled = isFeatureActive(mockedLabel, validTenant)
        enabled shouldBe false
    }

    @Test
    fun `feature manager should return true for all tenants when feature is active for environment`() = testApplication {
        mockkStatic("com.sap.pi.valueanalysis.util.GeneralUtilKt")
        every { getCurrentEnvironment() } returns "staging_cloud_os_eu"
        every { mockedLabel.name } returns "ACTIVE_FOR_ENVIRONMENT"
        val enabledForValidTenant = isFeatureActive(mockedLabel, validTenant)
        enabledForValidTenant shouldBe true

        val enabledForInValidTenant = isFeatureActive(mockedLabel, invalidTenant)
        enabledForInValidTenant shouldBe true
    }

    @Test
    fun `feature manager should return false when feature is not active for environment`() = testApplication {
        mockkStatic("com.sap.pi.valueanalysis.util.GeneralUtilKt")
        every { getCurrentEnvironment() } returns "staging_cloud_os_au"
        every { mockedLabel.name } returns "ACTIVE_FOR_ENVIRONMENT"
        val enabled = isFeatureActive(mockedLabel, validTenant)
        enabled shouldBe false
    }

    @Test
    fun `feature manager should return false when feature is not active for any tenant`() = testApplication {
        every { mockedLabel.name } returns "NO_TENANT_FEATURE"
        val enabled = isFeatureActive(mockedLabel, validTenant)
        enabled shouldBe false
    }

    @Test
    fun `feature manager should return false when feature is not active for any release`() = testApplication {
        every { mockedLabel.name } returns "NOT_RELEASE_FEATURE"
        val enabled = isFeatureActive(mockedLabel, validTenant)
        enabled shouldBe false
    }
}
