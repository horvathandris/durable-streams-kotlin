package com.github.horvathandris.durablestreams.ktor

import com.github.horvathandris.durablestreams.http.Headers
import com.github.horvathandris.durablestreams.http.Response
import com.github.horvathandris.durablestreams.stream.handler.StreamHandler
import com.github.horvathandris.durablestreams.stream.store.Store
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.server.application.createRouteScopedPlugin
import io.ktor.server.request.httpMethod
import io.ktor.server.response.header
import io.ktor.server.routing.route

class DurableStreamsPluginConfiguration(
  val store: Store,
)

fun DurableStreamsPlugin(
  store: Store,
) = createRouteScopedPlugin(
  name = "DurableStreamsPlugin",
  configurationPath = "durable-streams",
  createConfiguration = {
    DurableStreamsPluginConfiguration(store)
  },
) {
  val handler = StreamHandler(pluginConfig.store)

  route?.apply {
    route("{...}") {
      handle {
        val request = call.toRequest()
        val response = when (call.request.httpMethod) {
          HttpMethod.Options -> Response(status = 204)
          HttpMethod.Put -> handler.createStream(request)
          HttpMethod.Head -> handler.getStream(request)
          HttpMethod.Delete -> handler.deleteStream(request)
          HttpMethod.Get -> handler.readStream(request)
          HttpMethod.Post -> handler.appendToStream(request)
          else -> Response(status = 405)
        }
        call.respondWith(response)
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
          Headers.Stream.Seq,
          Headers.Stream.TTL,
          Headers.Stream.ExpiresAt,
          Headers.Stream.Closed,
          HttpHeaders.IfNoneMatch,
          Headers.Producer.Id,
          Headers.Producer.Epoch,
          Headers.Producer.Seq,
        )
          .joinToString(", ")
      )
      header(
        HttpHeaders.AccessControlExposeHeaders,
        listOf(
          Headers.Stream.NextOffset,
          Headers.Stream.Cursor,
          Headers.Stream.UpToDate,
          Headers.Stream.Closed,
          HttpHeaders.ETag,
          Headers.Producer.Epoch,
          Headers.Producer.Seq,
          Headers.Producer.ExpectedSeq,
          Headers.Producer.ReceivedSeq,
        )
          .joinToString(", ")
      )

      // Browser security headers (Protocol Section 10.7)
      header("X-Content-Type-Options", "nosniff")
      header("Cross-Origin-Resource-Policy", "cross-origin")
    }
  }
}