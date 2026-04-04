dependencyResolutionManagement {
  @Suppress("UnstableApiUsage")
  repositories {
    mavenCentral()
  }
}

rootProject.name = "durable-streams-kotlin"

include("durable-streams-json-spi")
include("durable-streams-json-kotlinx")
include("durable-streams-server-core")
include("durable-streams-server-ktor")
include("durable-streams-server-ktor-example")