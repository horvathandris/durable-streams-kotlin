plugins {
  id("conventions.kotlin-jvm")
  alias(libs.plugins.ktor)
}

application {
  mainClass = "io.ktor.server.netty.EngineMain"
}

repositories {
  mavenCentral()
}

dependencies {
  implementation(project(":durable-streams-server-ktor"))

  implementation(libs.ktor.server.core)
  implementation(libs.ktor.server.netty)
  implementation(libs.logback.classic)
  implementation(libs.ktor.server.config.yaml)

  testImplementation(kotlin("test"))
}