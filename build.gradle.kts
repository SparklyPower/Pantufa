import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.7.10"
    kotlin("plugin.serialization") version "1.7.10"
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
    maven("https://jitpack.io/")
}

dependencies {
    implementation(kotlin("stdlib"))
    runtimeOnly("ch.qos.logback:logback-classic:1.3.0-alpha14")
    implementation("io.github.microutils:kotlin-logging:2.1.23")

    implementation("net.dv8tion:JDA:5.0.0-beta.3")
    implementation("com.github.MinnDevelopment:jda-ktx:0.10.0-beta.1")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.4")
    api("org.jetbrains.kotlinx:kotlinx-coroutines-jdk8:1.6.4")
    implementation("com.github.ben-manes.caffeine:caffeine:3.1.1")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.4.0")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-hocon:1.4.0")

    // Remove this after everything has been migrated to InteraKTions Unleashed
    implementation("dev.kord:kord-rest:0.8.x-lori-fork-20221109.172532-14")
    implementation("dev.kord:kord-gateway:0.8.x-lori-fork-20221109.172532-15")
    implementation("dev.kord:kord-core:0.8.x-lori-fork-20221109.172532-14")

    implementation("com.github.kevinsawicki:http-request:6.0")

    // Web API
    api("io.ktor:ktor-server-netty:2.2.3")

    // Sequins
    implementation("net.perfectdreams.sequins.text:text-utils:1.0.0")

    // Database
    implementation("org.postgresql:postgresql:42.5.0")
    implementation("mysql:mysql-connector-java:8.0.30")
    implementation("com.zaxxer:HikariCP:5.0.1")
    implementation("org.jetbrains.exposed:exposed-core:0.39.2")
    implementation("org.jetbrains.exposed:exposed-dao:0.39.2")
    implementation("org.jetbrains.exposed:exposed-jdbc:0.39.2")
    implementation("net.perfectdreams.exposedpowerutils:postgres-java-time:1.0.0")

    implementation("io.ktor:ktor-client-cio:2.1.0")

    // Pudding
    implementation("net.perfectdreams.loritta.cinnamon.pudding:client:0.0.2-20220829.182502-714")

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