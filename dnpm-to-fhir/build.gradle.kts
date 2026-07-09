import net.ltgt.gradle.errorprone.errorprone
import org.gradle.api.tasks.testing.logging.TestLogEvent

plugins {
    id("java")
    id("com.gradleup.shadow") version "8.3.8"
    id("com.diffplug.spotless") version "7.2.1"
    id("net.ltgt.errorprone") version "4.3.0"
}

group = "dev.pcvolkmer.onco"
version = "0.1.0-SNAPSHOT"

val commonsCli by extra("1.10.0")
val commonsIo by extra("2.22.0")
val mtbDto by extra ("0.3.0")
val slf4j by extra("2.0.17")
val hapiFhirVersion by extra("7.6.1")

// Test dependencies

val junit by extra("5.14.4")
val assertj by extra("3.27.7")
val approvaltests by extra("31.0.0")

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(11)
    }
}

repositories {
    maven {
        url = uri("https://git.dnpm.dev/api/packages/public-snapshots/maven")
    }
    maven {
        url = uri("https://git.dnpm.dev/api/packages/public/maven")
    }
    mavenCentral()
}

sourceSets {
    create("snapshotTest") {
        java.srcDir("src/snapshotTest/java")

        compileClasspath += sourceSets.main.get().output
        runtimeClasspath += sourceSets.main.get().output

        compileClasspath += sourceSets.test.get().output
        runtimeClasspath += sourceSets.test.get().output
    }
}

val snapshotTestImplementation: Configuration by configurations.getting {
    extendsFrom(configurations.implementation.get())
    extendsFrom(configurations.testImplementation.get())
    extendsFrom(configurations.runtimeOnly.get())
    extendsFrom(configurations.testRuntimeOnly.get())
}

configurations {
    snapshotTestImplementation.extendsFrom(configurations.testImplementation.get())
}

dependencies {
    implementation("dev.pcvolkmer.mv64e:mtb-dto:$mtbDto")
    implementation("commons-cli:commons-cli:$commonsCli")
    implementation("commons-io:commons-io:$commonsIo")
    implementation("ca.uhn.hapi.fhir:hapi-fhir-base:${hapiFhirVersion}")
    implementation("ca.uhn.hapi.fhir:hapi-fhir-structures-r4:${hapiFhirVersion}")
    implementation("org.slf4j:slf4j-api:$slf4j")
    implementation("org.jspecify:jspecify:1.0.0")

    errorprone("com.google.errorprone:error_prone_core:2.31.0")
    errorprone("com.uber.nullaway:nullaway:0.12.12")

    testImplementation(platform("org.junit:junit-bom:$junit"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testImplementation("org.assertj:assertj-core:$assertj")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")

    snapshotTestImplementation("com.approvaltests:approvaltests:$approvaltests")
}

// Include dependencies in resulting JAR file
tasks.jar {
    manifest {
        attributes["Main-Class"] = "dev.pcvolkmer.onco.datamapper.fhir.ExportApplication"
    }
}

// Build fat JAR using task build
tasks.build.get().dependsOn(tasks.shadowJar)

tasks.register<Test>("snapshotTest") {
    description = "Runs integration tests"
    group = "verification"

    testClassesDirs = sourceSets["snapshotTest"].output.classesDirs
    classpath = sourceSets["snapshotTest"].runtimeClasspath

    shouldRunAfter("test")
}

tasks.withType<Test> {
    testLogging {
        events = setOf(TestLogEvent.PASSED, TestLogEvent.SKIPPED, TestLogEvent.FAILED)
    }
    useJUnitPlatform()
    dependsOn(tasks.spotlessCheck)
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
    options.errorprone {
        disableAllChecks = true
        option("NullAway:OnlyNullMarked", "true")
        error("NullAway")
    }
}

spotless {
    java {
        importOrder()
        removeUnusedImports()
        googleJavaFormat()
    }
}