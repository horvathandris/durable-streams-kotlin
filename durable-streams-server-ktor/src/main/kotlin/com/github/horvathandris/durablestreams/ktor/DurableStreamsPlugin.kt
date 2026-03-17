package com.github.horvathandris.durablestreams.ktor

import com.github.horvathandris.durablestreams.http.ProducerHttpHeaders
import com.github.horvathandris.durablestreams.http.StreamHttpHeaders
import com.github.horvathandris.durablestreams.stream.store.InMemoryStore
import com.github.horvathandris.durablestreams.stream.store.Store
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.server.application.createRouteScopedPlugin
import io.ktor.server.request.httpMethod
import io.ktor.server.request.path
import io.ktor.server.response.header
import io.ktor.server.routing.route

class DurableStreamsPluginConfiguration {
  val store: Store = InMemoryStore()
}

val DurableStreamsPlugin = createRouteScopedPlugin(
  name = "DurableStreamsPlugin",
  createConfiguration = ::DurableStreamsPluginConfiguration,
) {
  val store = pluginConfig.store

  route?.apply {
    route("{...}") {
      handle {
        val path = call.request.path()
        when (call.request.httpMethod) {
          HttpMethod.Options -> call.handleOptions()
          HttpMethod.Put -> call.handleCreate(path, store)
          HttpMethod.Head -> call.handleHead(path, store)
          HttpMethod.Delete -> call.handleDelete(path, store)
        }
      }
    }
  }

  onCall { call ->
    call.response.apply {
      // Set CORS headers
      header(HttpHeaders.AccessControlAllowOrigin, "*")
      header(
        HttpHeaders.AccessControlAllowMethods,
        listOf(
          HttpMethod.Get,
          HttpMethod.Post,
          HttpMethod.Put,
          HttpMethod.Delete,
          HttpMethod.Head,
          HttpMethod.Options,
        )
          .joinToString(", ") { it.value }
      )
      header(
        HttpHeaders.AccessControlAllowHeaders,
        listOf(
          HttpHeaders.ContentType,
          StreamHttpHeaders.StreamSeq,
          StreamHttpHeaders.StreamTTL,
          StreamHttpHeaders.StreamExpiresAt,
          StreamHttpHeaders.StreamClosed,
          HttpHeaders.IfNoneMatch,
          ProducerHttpHeaders.ProducerId,
          ProducerHttpHeaders.ProducerEpoch,
          ProducerHttpHeaders.ProducerSeq,
        )
          .joinToString(", ")
      )
      header(
        HttpHeaders.AccessControlExposeHeaders,
        listOf(
          StreamHttpHeaders.StreamNextOffset,
          StreamHttpHeaders.StreamCursor,
          StreamHttpHeaders.StreamUpToDate,
          StreamHttpHeaders.StreamClosed,
          HttpHeaders.ETag,
          ProducerHttpHeaders.ProducerEpoch,
          ProducerHttpHeaders.ProducerSeq,
          ProducerHttpHeaders.ProducerExpectedSeq,
          ProducerHttpHeaders.ProducerReceivedSeq,
        )
          .joinToString(", ")
      )

      // Browser security headers (Protocol Section 10.7)
      header("X-Content-Type-Options", "nosniff")
      header("Cross-Origin-Resource-Policy", "cross-origin")
    }
  }
}