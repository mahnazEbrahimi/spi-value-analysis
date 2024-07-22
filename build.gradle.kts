import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.jooq.meta.jaxb.ForcedType


group = "com.sap.pi"
version = "0.0.1"

val ktorVersion = "2.3.11"
val logbackVersion = "1.5.6"
val prometheusVersion = "1.12.5"
val jacksonVersion = "2.17.1"
val nettyVersion = "4.1.109.Final"
val postgresVersion = "42.7.3"
val jooqVersion = "3.19.9"
val flywayVersion: String by project

val dbUrl: String by project
val dbUser: String by project
val dbPassword: String by project
val dbSchema: String by project


plugins {
    application
    jacoco
    kotlin("jvm") version "2.0.0"
    id("com.github.johnrengelman.shadow") version "8.1.1"
    id("org.owasp.dependencycheck") version "9.0.9"
    id("org.sonarqube") version "5.0.0.4638"
    id("org.flywaydb.flyway")
    id("nu.studer.jooq") version "9.0"
}

application {
    applicationName = "spi-value-analysis"
    group = "com.sap.pi"
    mainClass.set("com.sap.pi.valueanalysis.ApplicationKt")
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}
kotlin {
    jvmToolchain {
        (this as JavaToolchainSpec).languageVersion.set(JavaLanguageVersion.of(17))
    }
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs = listOf("-Xcontext-receivers")
    }
}

tasks.withType<ShadowJar> {
    mergeServiceFiles()
    archiveBaseName = project.name
    archiveVersion = ""
}


dependencies {

    implementation("io.ktor:ktor-server-core:$ktorVersion")
    implementation("io.ktor:ktor-server-netty:$ktorVersion")

    implementation("io.ktor:ktor-server-resources:$ktorVersion")
    implementation("io.ktor:ktor-server-auth:$ktorVersion")
    implementation("io.ktor:ktor-server-auth-jwt:$ktorVersion")
    implementation("io.ktor:ktor-server-cors:$ktorVersion")
    implementation("io.ktor:ktor-server-status-pages:$ktorVersion")
    implementation("io.ktor:ktor-server-call-logging:$ktorVersion")

    implementation("ch.qos.logback:logback-classic:$logbackVersion")
    implementation("net.logstash.logback:logstash-logback-encoder:7.4")
    implementation("io.sentry:sentry-logback:7.10.0")
    implementation("io.github.oshai:kotlin-logging-jvm:6.0.9")
    implementation("org.codehaus.janino:janino:3.1.12")

    implementation("io.ktor:ktor-server-content-negotiation:$ktorVersion")
    implementation("io.ktor:ktor-serialization-jackson:$ktorVersion")
    implementation("io.ktor:ktor-serialization:$ktorVersion")
    implementation("com.fasterxml.jackson.core:jackson-databind:$jacksonVersion")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:$jacksonVersion")

    implementation("io.ktor:ktor-server-metrics-micrometer:$ktorVersion")
    implementation("io.micrometer:micrometer-registry-prometheus:$prometheusVersion")
    implementation("com.codahale.metrics:metrics-healthchecks:3.0.2")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-debug:1.8.1")

    implementation(platform("com.amazonaws:aws-java-sdk-bom:1.12.742"))
    implementation("com.amazonaws:aws-java-sdk-rds")
    implementation("com.amazonaws:aws-java-sdk-sts")

    implementation("org.flywaydb:flyway-core:$flywayVersion")
    implementation("org.flywaydb:flyway-database-postgresql:$flywayVersion")
    implementation("com.zaxxer:HikariCP:5.1.0")
    implementation("org.postgresql:postgresql:$postgresVersion")
    implementation("org.jooq:jooq-codegen:$jooqVersion")
    jooqGenerator("org.postgresql:postgresql:$postgresVersion")
    api("org.jooq:jooq:$jooqVersion")
    implementation("org.jooq:jooq-kotlin-coroutines:$jooqVersion")

    implementation("io.ktor:ktor-client-cio:$ktorVersion")
    implementation("io.ktor:ktor-client-logging:$ktorVersion")
    implementation("io.ktor:ktor-client-content-negotiation:$ktorVersion")
    implementation("io.ktor:ktor-client-auth:$ktorVersion")
    implementation("io.ktor:ktor-client-auth:$ktorVersion")

    implementation("guru.zoroark.tegral:tegral-openapi-ktor:0.0.4")
    implementation("guru.zoroark.tegral:tegral-core:0.0.4")
    implementation("javax.xml.bind:jaxb-api:2.3.1")

    testImplementation("org.junit.jupiter:junit-jupiter:5.10.2")
    testImplementation("io.ktor:ktor-server-test-host:$ktorVersion")
    testImplementation("io.ktor:ktor-client-mock:$ktorVersion")
    testImplementation("io.kotest:kotest-runner-junit5-jvm:5.9.1")
    testImplementation("io.mockk:mockk:1.13.11")
    testImplementation("com.github.tomakehurst:wiremock-jre8:3.0.1")
    testApi("com.google.code.findbugs:jsr305:3.0.2")

    constraints {
        // Fix https://sap.blackducksoftware.com/api/vulnerabilities/CVE-2022-41881/overview
        implementation("io.netty:netty-codec-http2") {
            version {
                require(nettyVersion)
            }
        }
        implementation("io.netty:netty-transport-native-kqueue") {
            version {
                require(nettyVersion)
            }
        }
        implementation("io.netty:netty-transport-native-epoll") {
            version {
                require(nettyVersion)
            }
        }
        implementation("io.netty:netty-transport") {
            version {
                require(nettyVersion)
            }
        }
        implementation("io.netty:netty-transport-native-unix-common") {
            version {
                require(nettyVersion)
            }
        }
        implementation("io.netty:netty-resolver") {
            version {
                require(nettyVersion)
            }
        }
        implementation("io.netty:netty-codec") {
            version {
                require(nettyVersion)
            }
        }
        implementation("io.netty:netty-codec-http") {
            version {
                require(nettyVersion)
            }
        }
        implementation("io.netty:netty-common") {
            version {
                require(nettyVersion)
            }
        }
        implementation("io.netty:netty-buffer") {
            version {
                require(nettyVersion)
            }
        }
        implementation("io.netty:netty-handler") {
            version {
                require(nettyVersion)
            }
        }
        // Fix: https://sap.blackducksoftware.com/api/vulnerabilities/CVE-2023-2976/overview
        implementation("com.google.guava:guava") {
            version {
                require("32.1.2-jre")
            }
        }
    }
}

tasks.test {
    useJUnitPlatform()
}

tasks.jacocoTestReport {
    reports {
        xml.required.set(true)
        html.required.set(true)
    }
    classDirectories.setFrom(
        files(
            classDirectories.files.map {
                fileTree(it) {
                    exclude(
                        "**/generated/**/*",
                    )
                }
            },
        ),
    )
}

tasks.test {
    useJUnitPlatform()
}

tasks.jacocoTestReport {
    reports {
        xml.required.set(true)
        html.required.set(true)
    }
    classDirectories.setFrom(
        files(
            classDirectories.files.map {
                fileTree(it) {
                    exclude(
                        "**/generated/**/*",
                    )
                }
            },
        ),
    )
}

tasks.test {
    finalizedBy("jacocoTestReport")
}

tasks.check {
    dependsOn("jacocoTestReport")
}

flyway {
    url = dbUrl
    user = dbUser
    password = dbPassword
    schemas = arrayOf(dbSchema)
    baselineVersion = "0"
    baselineOnMigrate = true
    cleanDisabled = false
}

jooq {
    version.set(jooqVersion)
    edition.set(nu.studer.gradle.jooq.JooqEdition.OSS)

    configurations {
        create("main") {
            generateSchemaSourceOnCompilation.set(true)

            jooqConfiguration.apply {
                logging = org.jooq.meta.jaxb.Logging.INFO
                jdbc.apply {
                    driver = "org.postgresql.Driver"
                    url = dbUrl
                    user = dbUser
                    password = dbPassword
                }
                generator.apply {
                    name = "org.jooq.codegen.KotlinGenerator"
                    strategy.name = "org.jooq.codegen.DefaultGeneratorStrategy"
                    database.apply {
                        name = "org.jooq.meta.postgres.PostgresDatabase"
                        inputSchema = dbSchema
                        excludes = "key_vault|schema_history"
                        forcedTypes = listOf(
                            ForcedType().apply {
                                userType = "com.fasterxml.jackson.databind.JsonNode"
                                binding = "com.sap.pi.spi-value-analysis.jooq.PostgresJSONJacksonJsonNodeBinding"
                                includeExpression = ".*"
                                includeTypes = "(?i:.*jsonb?.*)"
                            },
                        )
                    }
                    generate.apply {
                        isDeprecated = false
                        isRecords = true
                        isRelations = true
                        isPojos = true
                        isPojosEqualsAndHashCode = true
                    }
                    target.apply {
                        packageName = "com.sap.pi.valueanalysis.generated"
                    }
                }
            }
        }
    }
}

sonarqube {
    properties {
        property("sonar.coverage.jacoco.xmlReportPaths", "${project.layout.buildDirectory}/reports/jacoco/test/jacocoTestReport.xml")
    }
}
