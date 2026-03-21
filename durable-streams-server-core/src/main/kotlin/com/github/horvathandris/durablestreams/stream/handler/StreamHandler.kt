package com.github.horvathandris.durablestreams.stream.handler

import com.github.horvathandris.durablestreams.DurableStreamsException
import com.github.horvathandris.durablestreams.StreamNotFoundException
import com.github.horvathandris.durablestreams.http.Headers
import com.github.horvathandris.durablestreams.http.LiveMode
import com.github.horvathandris.durablestreams.http.Request
import com.github.horvathandris.durablestreams.http.Response
import com.github.horvathandris.durablestreams.stream.Producer
import com.github.horvathandris.durablestreams.stream.store.AppendOptions
import com.github.horvathandris.durablestreams.stream.store.AppendResult
import com.github.horvathandris.durablestreams.stream.store.CreateOptions
import com.github.horvathandris.durablestreams.stream.store.Store
import com.github.horvathandris.durablestreams.stream.toOffset
import com.github.horvathandris.durablestreams.toResponse

class StreamHandler(
  private val store: Store,
) {

  suspend fun createStream(request: Request): Response = withExceptionHandling {
    val options = CreateOptions.fromRequest(request)
    val result = store.create(request.uri.path, options)
    val headers = Headers.builder()
    headers[Headers.Http.ContentType] = result.metadata.contentType
    headers[Headers.Stream.NextOffset] = result.metadata.currentOffset.toString()
    if (result.metadata.closed) {
      headers[Headers.Stream.Closed] = "true"
    }
    if (result.newlyCreated) {
      val scheme = request.headers[Headers.Http.XForwardedProto].firstOrNull()
        ?: request.uri.scheme
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
  }

  fun getStream(request: Request): Response = withExceptionHandling {
    val metadata = store.get(request.uri.path) ?: throw StreamNotFoundException()

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

  suspend fun deleteStream(request: Request): Response = withExceptionHandling {
    store.delete(request.uri.path)
    return Response(status = 204)
  }

  suspend fun appendToStream(request: Request): Response = withExceptionHandling {
    val producer = Producer.fromHeaders(request.headers)

    val shouldCloseStream = request.headers[Headers.Stream.Closed].firstOrNull() == "true"
    if (request.data.isEmpty() && shouldCloseStream) {
      return closeStream(request, producer)
    }

    val options = AppendOptions.fromRequest(request)
    val result = store.append(request.path, request.data, producer, options)

    val headers = Headers.builder()
    when (result) {
      is AppendResult.StreamClosed -> {
        headers[Headers.Stream.Closed] = "true"
      }
    }

    if (result.duplicate || producer == null) {
      return Response(
        status = 204,
        headers = headers.build(),
      )
    }

    return Response(
      status = 200,
      headers = headers.build(),
    )
  }

  private suspend fun closeStream(request: Request, producer: Producer?): Response {
    val result = store.close(request.path, producer)
    val headers = Headers.builder()
    headers[Headers.Stream.NextOffset] = result.finalOffset.toString()
    headers[Headers.Stream.Closed] = "true"
    producer?.let {
      headers[Headers.Producer.Epoch] = it.epoch.toString()
      headers[Headers.Producer.Seq] = it.seq.toString()
    }
    return Response(
      status = 204,
      headers = headers.build(),
    )
  }

  suspend fun readStream(request: Request): Response = withExceptionHandling {
    val metadata = store.get(request.uri.path) ?: return Response(status = 404)

    val offset = request.query("offset")?.toOffset()

    val liveModeQuery = request.query("live")?.firstOrNull()
    val liveMode = liveModeQuery?.let {  LiveMode.fromValue(it) }

    return Response(
      status = 501,
    )
  }

}

private inline fun withExceptionHandling(body: () -> Response): Response {
  return try {
    body()
  } catch (e: DurableStreamsException) {
    e.toResponse()
  }
}