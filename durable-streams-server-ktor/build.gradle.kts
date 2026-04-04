plugins {
    id("conventions.kotlin-jvm")
}

version = "0.0.1"

repositories {
    mavenCentral()
}

dependencies {
    api(project(":durable-streams-server-core"))
    implementation(project(":durable-streams-json-spi"))

    implementation(libs.ktor.server.core)

    testImplementation(kotlin("test"))
}