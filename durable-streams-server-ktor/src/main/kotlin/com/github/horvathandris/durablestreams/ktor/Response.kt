package com.github.horvathandris.durablestreams.ktor

import com.github.horvathandris.durablestreams.http.Response
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCall
import io.ktor.server.response.header
import io.ktor.server.response.respond

internal suspend fun ApplicationCall.respond(response: Response) {
  for (header in response.headers) {
    for (value in header.value) {
      this.response.header(header.key, value)
    }
  }
  this.respond(
    status = HttpStatusCode.fromValue(response.status),
    message = response.data ?: ByteArray(0),
  )
}