package com.github.horvathandris.durablestreams.example

import com.github.horvathandris.durablestreams.json.KotlinxJsonSerializer
import com.github.horvathandris.durablestreams.ktor.DurableStreamsPlugin
import com.github.horvathandris.durablestreams.stream.store.InMemoryStore
import io.ktor.server.application.Application
import io.ktor.server.routing.route
import io.ktor.server.routing.routing

fun Application.configureRouting() {
  routing {
    route("/") {
      install(
        DurableStreamsPlugin(
          store = InMemoryStore(
            serializer = KotlinxJsonSerializer(),
          ),
        ),
      )
    }
  }
}