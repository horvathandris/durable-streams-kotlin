package com.github.horvathandris.durablestreams.stream.handler

import com.github.horvathandris.durablestreams.InvalidHeaderException
import com.github.horvathandris.durablestreams.InvalidParameterException
import com.github.horvathandris.durablestreams.StreamExistsException
import com.github.horvathandris.durablestreams.StreamNotFoundException
import com.github.horvathandris.durablestreams.http.Headers
import com.github.horvathandris.durablestreams.http.LiveMode
import com.github.horvathandris.durablestreams.http.Request
import com.github.horvathandris.durablestreams.http.Response
import com.github.horvathandris.durablestreams.stream.Producer
import com.github.horvathandris.durablestreams.stream.store.CreateOptions
import com.github.horvathandris.durablestreams.stream.store.Store
import com.github.horvathandris.durablestreams.stream.toOffset

class StreamHandler(
  private val store: Store,
) {

  suspend fun createStream(request: Request): Response {
    val options = CreateOptions.fromRequest(request)
    try {
      val result = store.create(request.uri.path, options)
      val headers = Headers.builder()
      headers[Headers.Http.ContentType] = result.metadata.contentType
      headers[Headers.Stream.NextOffset] = result.metadata.currentOffset.toString()
      if (result.metadata.closed) {
        headers[Headers.Stream.Closed] = "true"
      }
      if (result.newlyCreated) {
        val scheme = request.headers[Headers.Http.XForwardedProto].firstOrNull()
          ?: if (request.uri.scheme == "https") "https" else "http"
        val host = request.headers[Headers.Http.XForwardedHost].firstOrNull()
          ?: request.uri.host
        headers[Headers.Http.Location] = "$scheme://$host${request.uri.path}"
        return Response(
          status = 201,
          headers = headers.build(),
        )
      }
      return Response(
        status = 200,
        headers = headers.build(),
      )
    } catch (e: StreamExistsException) {
      return Response(
        status = 409,
        data = e.message,
      )
    }
  }

  fun getStream(request: Request): Response {
    val metadata = store.get(request.uri.path) ?: return Response(status = 404)

    val headers = Headers.builder()
    headers[Headers.Http.ContentType] = metadata.contentType
    headers[Headers.Stream.NextOffset] = metadata.currentOffset.toString()
    headers[Headers.Http.CacheControl] = "no-store"
    metadata.ttlSeconds?.let {
      headers[Headers.Stream.TTL] = it.toString()
    }
    metadata.expiresAt?.let {
      headers[Headers.Stream.ExpiresAt] = it.toString()
    }
    if (metadata.closed) {
      headers[Headers.Stream.Closed] = "true"
    }

    return Response(
      status = 200,
      headers = headers.build(),
    )
  }

  suspend fun deleteStream(request: Request): Response {
    try {
      store.delete(request.uri.path)
      return Response(status = 204)
    } catch (e: StreamNotFoundException) {
      return Response(
        status = 404,
        data = e.message,
      )
    }
  }

  suspend fun appendToStream(request: Request): Response {
    val metadata = store.get(request.uri.path) ?: return Response(status = 404)
    val producer = try {
      Producer.fromHeaders(request.headers)
    } catch (e: InvalidHeaderException) {
      return Response(
        status = 400,
        data = e.message,
      )
    }

    val closeStream = request.headers[Headers.Stream.Closed].firstOrNull() == "true"
    if (request.data.isEmpty() && closeStream) {
      // TODO: close stream
    }

    return Response(status = 501)
  }

  suspend fun readStream(request: Request): Response {
    val metadata = store.get(request.uri.path) ?: return Response(status = 404)

    val offset = try {
      request.query("offset")?.toOffset()
    } catch (e: InvalidParameterException) {
      return Response(
        status = 400,
        data = e.message,
      )
    }

    val liveModeQuery = request.query("live")?.firstOrNull()
    val liveMode = liveModeQuery?.let {  LiveMode.fromValue(it) }

    return Response(
      status = 501,
    )
  }

}