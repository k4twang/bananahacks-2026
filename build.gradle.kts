plugins {
    kotlin("jvm") version "1.9.23"
    application
    id("org.jetbrains.kotlin.plugin.serialization") version "1.9.23"
}

group = "com.bananahacks.agent"
version = "1.0-0"

repositories {
    mavenCentral()
}

val ktorVersion = "2.3.10"
val logbackVersion = "1.4.14"

dependencies {
    implementation("io.ktor:ktor-server-core-jvm:$ktorVersion")
    implementation("io.ktor:ktor-server-netty-jvm:$ktorVersion")
    implementation("io.ktor:ktor-server-content-negotiation-jvm:$ktorVersion")
    implementation("io.ktor:ktor-serialization-kotlinx-json-jvm:$ktorVersion")
    implementation("io.ktor:ktor-server-cors-jvm:$ktorVersion")
    implementation("ch.qos.logback:logback-classic:$logbackVersion")
    
    testImplementation(kotlin("test"))
    testImplementation("io.ktor:ktor-server-tests-jvm:$ktorVersion")
}

application {
    mainClass.set("com.bananahacks.agent.ApplicationKt")
}

kotlin {
    jvmToolchain(17)
}
