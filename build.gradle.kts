import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.6.10"
    kotlin("plugin.serialization") version "1.6.10"
    id("com.google.cloud.tools.jib") version "3.1.4"
}

group = "net.perfectdreams.pantufa"
version = "1.0.0"

repositories {
    mavenCentral()
    maven("https://oss.sonatype.org/content/repositories/snapshots")
    maven("https://repo.perfectdreams.net/")
    maven("https://jcenter.bintray.com")
    maven("https://m2.dv8tion.net/releases")
}

dependencies {
    implementation(kotlin("stdlib"))
    runtimeOnly("ch.qos.logback:logback-classic:1.3.0-alpha12")
    implementation("io.github.microutils:kotlin-logging:2.1.21")

    implementation("net.dv8tion:JDA:4.3.0_283")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.0")
    api("org.jetbrains.kotlinx:kotlinx-coroutines-jdk8:1.4.1")
    implementation("com.github.ben-manes.caffeine:caffeine:2.8.5")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.3.2")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-hocon:1.3.2")

    implementation("net.perfectdreams.discordinteraktions:gateway-kord:0.0.12-SNAPSHOT")
    implementation("com.github.kevinsawicki:http-request:6.0")

    // Sequins
    implementation("net.perfectdreams.sequins.text:text-utils:1.0.0")

    // Database
    implementation("org.postgresql:postgresql:42.3.1")
    implementation("mysql:mysql-connector-java:8.0.25")
    implementation("com.zaxxer:HikariCP:5.0.1")
    implementation("org.jetbrains.exposed:exposed-core:0.37.3")
    implementation("org.jetbrains.exposed:exposed-dao:0.37.3")
    implementation("org.jetbrains.exposed:exposed-jdbc:0.37.3")

    implementation("io.ktor:ktor-client-cio:1.6.7")

    // Pudding
    implementation("net.perfectdreams.loritta.cinnamon.pudding:client:0.0.2-20220116.001122-65")

    // Used for unregister
    implementation("org.mindrot:jbcrypt:0.4")

    implementation("org.apache.commons:commons-text:1.9")

    api("com.github.salomonbrys.kotson:kotson:2.5.0")

    testImplementation("org.junit.jupiter:junit-jupiter:5.4.2")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.4.2")
}

jib {
    container {
        ports = listOf("8080")
    }

    to {
        image = "ghcr.io/sparklypower/pantufa"

        auth {
            username = System.getProperty("DOCKER_USERNAME") ?: System.getenv("DOCKER_USERNAME")
            password = System.getProperty("DOCKER_PASSWORD") ?: System.getenv("DOCKER_PASSWORD")
        }
    }

    from {
        image = "openjdk:17-slim-bullseye"
    }
}

tasks.test {
    useJUnitPlatform()

    testLogging {
        showStandardStreams = true
    }
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "16"
    kotlinOptions.javaParameters = true
}