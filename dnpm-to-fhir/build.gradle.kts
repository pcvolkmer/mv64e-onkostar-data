import org.gradle.api.tasks.testing.logging.TestLogEvent

/*
 * This file is part of mv64e-onkostar-data
 *
 * Copyright (C) 2026 the original author or authors.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

plugins {
    id("java")
    id("com.gradleup.shadow") version "8.3.8"
    id("com.diffplug.spotless") version "7.2.1"
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


    snapshotTestImplementation(platform("org.junit:junit-bom:$junit"))
    snapshotTestImplementation("org.junit.jupiter:junit-jupiter")
    snapshotTestImplementation("org.assertj:assertj-core:$assertj")
    snapshotTestImplementation("org.junit.platform:junit-platform-launcher")
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
    dependsOn(tasks.spotlessApply)
}

spotless {
    java {
        importOrder()
        removeUnusedImports()
        googleJavaFormat()
    }
}