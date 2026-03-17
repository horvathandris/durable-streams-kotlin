package com.github.horvathandris.durablestreams.stream.store

import com.github.horvathandris.durablestreams.StreamExistsException
import com.github.horvathandris.durablestreams.StreamNotFoundException
import com.github.horvathandris.durablestreams.stream.Message
import com.github.horvathandris.durablestreams.stream.StreamMetadata
import com.github.horvathandris.durablestreams.stream.configMatches
import com.github.horvathandris.durablestreams.stream.isExpired
import com.github.horvathandris.durablestreams.stream.zeroOffset
import kotlin.time.Clock
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class InMemoryStore : Store {

  data class InMemoryStream(
    val metadata: StreamMetadata,
    val messages: List<Message> = mutableListOf(),
  )

  val mutex = Mutex()
  val streams = mutableMapOf<String, InMemoryStream>()

  override suspend fun create(
    path: Path,
    options: CreateOptions,
  ): CreateStreamResult = mutex.withLock {
    streams[path]?.let { existing ->
      when {
        existing.metadata.isExpired() -> streams.remove(path)
        existing.metadata.configMatches(options) ->
          return@withLock CreateStreamResult(existing.metadata, newlyCreated = false)
        else -> throw StreamExistsException()
      }
    }

    val metadata = StreamMetadata(
      path = path,
      contentType = options.contentType,
      currentOffset = zeroOffset(),
      ttlSeconds = options.ttlSeconds,
      expiresAt = options.expiresAt,
      createdAt = Clock.System.now(),
      closed = options.closed,
    )

    streams[path] = InMemoryStream(metadata)

    // TODO: handle initial data

    CreateStreamResult(metadata, newlyCreated = true)
  }

  override fun get(path: Path): StreamMetadata? =
    streams[path]
      ?.metadata
      ?.takeUnless { it.isExpired() }

  override fun has(path: Path): Boolean =
    get(path) != null

  override suspend fun delete(path: Path): Unit = mutex.withLock {
    streams.remove(path) ?: throw StreamNotFoundException()
  }

}