plugins {
    id("conventions.kotlin-jvm")
}

version = "0.0.1"

repositories {
    mavenCentral()
}

dependencies {
    implementation(project(":durable-streams-json-core"))

    implementation(libs.kotlinxCoroutines)
    implementation(libs.kotlin.logging)

    testImplementation(kotlin("test"))
}