rootProject.name = "spi-value-analysis"
pluginManagement {
    val flywayVersion: String by settings

    plugins {
        id("org.flywaydb.flyway") version flywayVersion
    }

    @Suppress("UnstableApiUsage")
    repositories {
        val artifactory_user: String = System.getenv("SAP_ARTIFACTORY_USER") ?: settings.extra.properties["artifactory_user"] as String? ?: error("Artifactory User required.")
        val artifactory_password: String = System.getenv("SAP_ARTIFACTORY_TOKEN") ?: settings.extra.properties["artifactory_password"] as String? ?: error("Artifactory Identity Token key required.")
        val artifactory_contextUrl = System.getenv("SAP_ARTIFACTORY_URL") ?: settings.extra.properties["artifactory_contextUrl"] as String?
        val artifactory_prefix = System.getenv("SAP_ARTIFACTORY_PREFIX") ?: settings.extra.properties["artifactory_prefix"] as String?

        val startsWithComSignavio = "com\\.signavio(\\..*)?"

        maven {
            url = uri("${artifactory_contextUrl}/${artifactory_prefix}gradlePluginPortal")
            credentials {
                username = artifactory_user
                password = artifactory_password
            }
            content {
                excludeGroup("com.signavio")
            }
        }

        maven {
            url = uri("${artifactory_contextUrl}/${artifactory_prefix}plugins-release")
            credentials {
                username = artifactory_user
                password = artifactory_password
            }
            content {
                includeGroupByRegex(startsWithComSignavio)
            }
        }
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)

    @Suppress("UnstableApiUsage")
    repositories {
        val artifactory_user: String = System.getenv("SAP_ARTIFACTORY_USER") ?: settings.extra.properties["artifactory_user"] as String? ?: error("Artifactory User required.")
        val artifactory_password: String = System.getenv("SAP_ARTIFACTORY_TOKEN") ?: settings.extra.properties["artifactory_password"] as String? ?: error("Artifactory Identity Token key required.")
        val artifactory_contextUrl = System.getenv("SAP_ARTIFACTORY_URL") ?: settings.extra.properties["artifactory_contextUrl"] as String?
        val artifactory_prefix = System.getenv("SAP_ARTIFACTORY_PREFIX") ?: settings.extra.properties["artifactory_prefix"] as String?

        maven {
            url = uri("${artifactory_contextUrl}/${artifactory_prefix}maven-central")
            credentials {
                username = artifactory_user
                password = artifactory_password
            }
        }
        maven {
            url = uri("${artifactory_contextUrl}/${artifactory_prefix}libs-release")
            credentials {
                username = artifactory_user
                password = artifactory_password
            }
        }
        maven {
            url = uri("${artifactory_contextUrl}/${artifactory_prefix}libs-snapshot")
            credentials {
                username = artifactory_user
                password = artifactory_password
            }
        }
        maven {
            url = uri("${artifactory_contextUrl}/${artifactory_prefix}gradlePluginPortal")
            credentials {
                username = artifactory_user
                password = artifactory_password
            }
        }
    }
}

buildscript {
    val flywayVersion: String by settings
    repositories {
        val artifactory_user: String =
            System.getenv("SAP_ARTIFACTORY_USER") ?: settings.extra.properties["artifactory_user"] as String?
            ?: error("Artifactory User required.")
        val artifactory_password: String =
            System.getenv("SAP_ARTIFACTORY_TOKEN") ?: settings.extra.properties["artifactory_password"] as String?
            ?: error("Artifactory Identity Token key required.")
        val artifactory_contextUrl =
            System.getenv("SAP_ARTIFACTORY_URL") ?: settings.extra.properties["artifactory_contextUrl"] as String?
        val artifactory_prefix =
            System.getenv("SAP_ARTIFACTORY_PREFIX") ?: settings.extra.properties["artifactory_prefix"] as String?

        maven {
            url = uri("${artifactory_contextUrl}/${artifactory_prefix}maven-central")
            credentials {
                username = artifactory_user
                password = artifactory_password
            }
        }
    }
    dependencies {
        classpath("org.flywaydb:flyway-database-postgresql:$flywayVersion")
        classpath("com.fasterxml.jackson.core:jackson-databind:2.17.1")
    }
}