package com.github.horvathandris.durablestreams.ktor

import com.github.horvathandris.durablestreams.store.CreateOptions
import com.github.horvathandris.durablestreams.store.Store
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCall
import io.ktor.server.request.contentType
import io.ktor.server.response.respond

internal suspend fun ApplicationCall.handleOptions() {
  respond(HttpStatusCode.NoContent)
}

internal suspend fun ApplicationCall.handleCreate(
  streamPath: String,
  store: Store,
) {
  val contentType = request.contentType().toString()
  val createOptions = CreateOptions(contentType)
  store.create(streamPath, createOptions)
  respond(HttpStatusCode.Created)
}