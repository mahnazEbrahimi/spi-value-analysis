package com.sap.pi.valueanalysis.util

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.sap.pi.valueanalysis.applicationConfig
import com.sap.pi.valueanalysis.featureConfiguration
import com.sap.pi.valueanalysis.releaseConfiguration
import org.yaml.snakeyaml.Yaml
import java.io.File
import java.util.*

data class FeatureToggle(
    val feature: String,
    val tenants: List<String>? = emptyList(),
    val environments: List<String>? = emptyList(),
    val releases: List<String>? = emptyList(),
    @JsonProperty("active_for_all")
    val activeForAll: Boolean = false,
)

data class ReleaseToggle(
    val name: String,
    val tenants: List<String>? = emptyList(),
    @JsonProperty("active_for_all")
    val activeForAll: Boolean = false,
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class FeatureConfiguration(
    val toggles: List<FeatureToggle>,
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class ReleaseConfiguration(
    val toggles: List<ReleaseToggle>,
)

enum class FeatureLabels {
    SWITCH_TO_NEW_SERVICE,
}

fun loadFeatureToggles(): FeatureConfiguration {
    val filePath = applicationConfig.property("featureManager.featureTogglePath").getString()
    val featureToggleFile = File(filePath)
    val yaml = Yaml()
    val loadedData = yaml.load<Map<String, Any>>(featureToggleFile.inputStream())
    val objectMapper = jacksonObjectMapper()
    objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
    return objectMapper.convertValue(loadedData, FeatureConfiguration::class.java)
}

fun loadReleaseToggles(): ReleaseConfiguration {
    val filePath = applicationConfig.property("featureManager.releaseTogglePath").getString()
    val releaseToggleFile = File(filePath)
    val yaml = Yaml()
    val loadedData = yaml.load<Map<String, Any>>(releaseToggleFile.inputStream())
    val objectMapper = jacksonObjectMapper()
    objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
    return objectMapper.convertValue(loadedData, ReleaseConfiguration::class.java)
}

fun isFeatureActive(label: FeatureLabels, tenant: UUID): Boolean =
    featureConfiguration.toggles
        .firstOrNull { it.feature == label.name }
        ?.let { isFeatureActiveForTenant(it, removeDashesFromUuid(tenant)) }
        ?: false

private fun isFeatureActiveForTenant(feature: FeatureToggle, tenant: String): Boolean {
    val isEnvironmentEnabled = feature.environments.orEmpty().all { getCurrentEnvironment().isBlank() || it == getCurrentEnvironment() }
    return isEnvironmentEnabled &&
        (
            feature.activeForAll ||
                feature.tenants.orEmpty().contains(tenant) ||
                isEnabledForReleaseTenant(feature, tenant)
            )
}

private fun isEnabledForReleaseTenant(feature: FeatureToggle, tenant: String): Boolean =
    releaseConfiguration.toggles
        .any {
            feature.releases.orEmpty().contains(it.name) &&
                (it.activeForAll || it.tenants.orEmpty().contains(tenant))
        }
