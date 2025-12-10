import net.ltgt.gradle.errorprone.errorprone

plugins {
    id("java")
    id("java-library")
    id("com.diffplug.spotless") version "7.2.1"
    id("net.ltgt.errorprone") version "4.3.0"
    id("maven-publish")
}

group = "dev.pcvolkmer.mv64e"
version = "0.2.4" // x-release-please-version

var versions = mapOf(
    "mtb-dto" to "0.1.2",
    "commons-csv" to "1.10.0",
    "slf4j" to "2.0.17",
    "junit" to "5.14.1",
    "assertj" to "3.27.6",
    "mockito" to "5.21.0"
)

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(11)
    }
    withJavadocJar()
    withSourcesJar()
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

dependencies {
    api("dev.pcvolkmer.mv64e:mtb-dto:${versions["mtb-dto"]}")
    api("com.fasterxml.jackson.core:jackson-databind:[2.12.2, )")
    implementation("org.springframework:spring-jdbc") {
        version {
            strictly("[4.3.8.RELEASE, )")
            // Current Onkostar 2.14.0 dependendcy
            prefer("4.3.8.RELEASE")
        }
    }
    implementation("org.apache.commons:commons-csv:${versions["commons-csv"]}")
    implementation("org.slf4j:slf4j-api:${versions["slf4j"]}")
    implementation("org.jspecify:jspecify:1.0.0")

    testImplementation(platform("org.junit:junit-bom:${versions["junit"]}"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testImplementation("org.assertj:assertj-core:${versions["assertj"]}")
    testImplementation("org.mockito:mockito-core:${versions["mockito"]}")
    testImplementation("org.mockito:mockito-junit-jupiter:${versions["mockito"]}")
    testImplementation("ch.qos.logback:logback-classic:1.5.21")

    testRuntimeOnly("org.junit.platform:junit-platform-launcher")

    errorprone("com.google.errorprone:error_prone_core:2.31.0")
    errorprone("com.uber.nullaway:nullaway:0.12.12")
}

tasks.test {
    useJUnitPlatform()
    dependsOn(tasks.spotlessCheck)
}

tasks.withType<JavaCompile> {
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

publishing {
    repositories {
        mavenLocal()
        maven {
            name = "GitDnpmDev"

            val releasesRepoUrl = uri("https://git.dnpm.dev/api/packages/public/maven")
            val snapshotsRepoUrl = uri("https://git.dnpm.dev/api/packages/public-snapshots/maven")
            url = if (version.toString().endsWith("SNAPSHOT"))
                snapshotsRepoUrl
            else
                releasesRepoUrl

            credentials(HttpHeaderCredentials::class) {
                name = "Authorization"
                value = "token ${properties["dnpm_dev_token"] ?: ""}"
            }

            authentication {
                create<HttpHeaderAuthentication>("header")
            }
        }
    }

    publications {
        create<MavenPublication>("maven") {
            from(components["java"])
        }
    }
}
