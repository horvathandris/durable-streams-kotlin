plugins {
    id("conventions.kotlin-jvm")
}

version = "0.0.1"

repositories {
    mavenCentral()
}

dependencies {
    implementation(libs.ktor.server.core)

    testImplementation(kotlin("test"))
}