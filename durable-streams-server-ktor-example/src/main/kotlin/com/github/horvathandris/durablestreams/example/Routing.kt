package com.github.horvathandris.durablestreams.example

import com.github.horvathandris.durablestreams.ktor.DurableStreamsPlugin
import io.ktor.server.application.Application
import io.ktor.server.response.respondText
import io.ktor.server.routing.get
import io.ktor.server.routing.route
import io.ktor.server.routing.routing

fun Application.configureRouting() {
  routing {
    route("/streams") {
      install(DurableStreamsPlugin)
    }
    get("/") {
      call.respondText("Hello World!")
    }
  }
}