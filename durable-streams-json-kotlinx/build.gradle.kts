plugins {
  id("conventions.kotlin-jvm")
  alias(libs.plugins.kotlin.serialization)
}

version = "0.0.1"

repositories {
  mavenCentral()
}

dependencies {
  api(project(":durable-streams-json-core"))

  implementation(libs.kotlinx.serialization.json)

  testImplementation(kotlin("test"))
}
