package com.github.horvathandris.durablestreams.ktor

import com.github.horvathandris.durablestreams.StreamExistsException
import com.github.horvathandris.durablestreams.StreamNotFoundException
import com.github.horvathandris.durablestreams.http.StreamHttpHeaders
import com.github.horvathandris.durablestreams.stream.store.CreateOptions
import com.github.horvathandris.durablestreams.stream.store.Store
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCall
import io.ktor.server.plugins.origin
import io.ktor.server.request.header
import io.ktor.server.request.host
import io.ktor.server.request.receive
import io.ktor.server.response.header
import io.ktor.server.response.respond
import kotlin.time.Instant

internal suspend fun ApplicationCall.handleOptions() {
  respond(HttpStatusCode.NoContent)
}

internal suspend fun ApplicationCall.handleCreate(
  path: String,
  store: Store,
) {
  val ttlSeconds = request.header(StreamHttpHeaders.StreamTTL)?.toLongOrNull()
  val expiresAt = request.header(StreamHttpHeaders.StreamExpiresAt)?.let { Instant.parse(it) }
  if (ttlSeconds != null && expiresAt != null) {
    respond(HttpStatusCode.BadRequest)
    return
  }

  val createOptions = CreateOptions(
    contentType = request.header(HttpHeaders.ContentType)
      ?.let { ContentType.parse(it).toString() }
      ?: "application/octet-stream",
    ttlSeconds = ttlSeconds,
    expiresAt = expiresAt,
    initialData = receive<ByteArray>(),
    closed = request.header(StreamHttpHeaders.StreamClosed)?.lowercase() == "true",
  )

  try {
    val result = store.create(path, createOptions)
    response.header(HttpHeaders.ContentType, result.metadata.contentType)
    response.header(StreamHttpHeaders.StreamNextOffset, result.metadata.currentOffset.toString())
    if (result.metadata.closed) {
      response.header(StreamHttpHeaders.StreamClosed, "true")
    }
    if (result.newlyCreated) {
      val scheme = request.header("X-Forwarded-Proto")
        ?: if (request.origin.scheme == "https") "https" else "http"
      val host = request.header("X-Forwarded-Host")
        ?: request.host()
      response.header(HttpHeaders.Location, "$scheme://$host$path")
      respond(HttpStatusCode.Created)
    } else {
      respond(HttpStatusCode.OK)
    }
  } catch (_: StreamExistsException) {
    respond(HttpStatusCode.Conflict, "Stream exists with different configuration")
  }
}

internal suspend fun ApplicationCall.handleHead(path: String, store: Store) {
  val metadata = store.get(path)

  if (metadata == null) {
    respond(HttpStatusCode.NotFound)
    return
  }

  response.header(HttpHeaders.ContentType, metadata.contentType)
  response.header(StreamHttpHeaders.StreamNextOffset, metadata.currentOffset.toString())
  response.header(HttpHeaders.CacheControl, "no-store")

  metadata.ttlSeconds?.let {
    response.header(StreamHttpHeaders.StreamTTL, it.toString())
  }

  metadata.expiresAt?.let {
    response.header(StreamHttpHeaders.StreamExpiresAt, it.toString())
  }

  if (metadata.closed) {
    response.header(StreamHttpHeaders.StreamClosed, "true")
  }

  respond(HttpStatusCode.OK)
}

internal suspend fun ApplicationCall.handleDelete(path: String, store: Store) {
  try {
    store.delete(path)
    respond(HttpStatusCode.NoContent)
  } catch (_: StreamNotFoundException) {
    respond(HttpStatusCode.NotFound, "Stream not found")
  }
}