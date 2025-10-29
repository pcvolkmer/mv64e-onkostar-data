plugins {
    id("java")
    id("java-library")
    id("com.diffplug.spotless") version "7.2.1"
}

group = "dev.pcvolkmer.onco"
version = "0.1.0-SNAPSHOT"

var versions = mapOf(
    "mtb-dto" to "0.1.0-SNAPSHOT",
    "commons-csv" to "1.10.0",
    "slf4j" to "2.0.17",
    "junit" to "5.13.1",
    "assertj" to "3.27.3",
    "mockito" to "5.18.0"
)

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(11)
    }
}

configurations {
    all {
        resolutionStrategy {
            cacheChangingModulesFor(5, "minutes")
        }
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

dependencies {
    api("dev.pcvolkmer.mv64e:mtb-dto:${versions["mtb-dto"]}") { isChanging = true }
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

    testImplementation(platform("org.junit:junit-bom:${versions["junit"]}"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testImplementation("org.assertj:assertj-core:${versions["assertj"]}")
    testImplementation("org.mockito:mockito-core:${versions["mockito"]}")
    testImplementation("org.mockito:mockito-junit-jupiter:${versions["mockito"]}")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.test {
    useJUnitPlatform()
    dependsOn(tasks.spotlessCheck)
}

spotless {
    java {
        importOrder()
        removeUnusedImports()
        googleJavaFormat()
    }
}
