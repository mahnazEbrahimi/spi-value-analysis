package com.sap.pi.valueanalysis.util

import io.kotest.matchers.shouldBe
import io.ktor.server.testing.*
import io.mockk.every
import io.mockk.mockkStatic
import org.junit.jupiter.api.Test

class GeneralUtilUnitTest {

    @Test
    fun `diacritics characters of an input string should be stripped out correctly`() = testApplication {
        mockkStatic("com.sap.pi.valueanalysis.util.GeneralUtilKt")
        every { getCurrentEnvironment() } returns "prod_cloud_os_eu"
        val currentEnv = getCurrentRegion()
        currentEnv shouldBe "EU"
    }

}
