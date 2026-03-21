package com.github.horvathandris.durablestreams.ktor

import com.github.horvathandris.durablestreams.http.Headers
import com.github.horvathandris.durablestreams.http.Request
import com.github.horvathandris.durablestreams.http.URI
import io.ktor.server.application.ApplicationCall
import io.ktor.server.plugins.origin
import io.ktor.server.request.path
import io.ktor.server.request.receive
import io.ktor.util.toMap

internal suspend fun ApplicationCall.toRequest(): Request {
  val uri = URI(
    scheme = request.origin.scheme,
    host = request.origin.serverHost,
    path = request.path(),
    query = request.queryParameters.toMap(),
  )
  return Request(
    uri = uri,
    headers = Headers(request.headers.toMap()),
    data = receive<ByteArray>(),
  )
}