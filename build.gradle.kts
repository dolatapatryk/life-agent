plugins {
    kotlin("jvm") version "2.3.0"
    kotlin("plugin.serialization") version "2.2.20"
    application
}

group = "com.patrykdolata"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("io.ktor:ktor-client-core:3.3.0")
    implementation("io.ktor:ktor-client-cio:3.3.0")
    implementation("io.ktor:ktor-client-content-negotiation:3.3.0")
    implementation("io.ktor:ktor-serialization-kotlinx-json:3.3.0")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.9.0")
    implementation("io.ktor:ktor-client-core:3.3.0")

    testImplementation(kotlin("test"))
}

kotlin {
    jvmToolchain(23)
}

application {
    mainClass.set("com.patrykdolata.lifeagent.MainKt")
}

tasks.test {
    useJUnitPlatform()
}

tasks.named<JavaExec>("run") {
    standardInput = System.`in`
}
