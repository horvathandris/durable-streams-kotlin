package com.github.horvathandris.durablestreams.ktor

import com.github.horvathandris.durablestreams.http.Headers
import com.github.horvathandris.durablestreams.http.Request
import com.github.horvathandris.durablestreams.http.URI
import io.ktor.server.application.ApplicationCall
import io.ktor.server.request.receive
import io.ktor.server.request.uri
import io.ktor.util.toMap

internal suspend fun ApplicationCall.toRequest(): Request {
  return Request(
    uri = URI.parse(request.uri),
    headers = Headers(request.headers.toMap()),
    data = receive<ByteArray>(),
  )
}