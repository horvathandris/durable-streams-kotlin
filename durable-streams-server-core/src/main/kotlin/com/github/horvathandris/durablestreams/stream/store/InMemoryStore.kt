package com.github.horvathandris.durablestreams.stream.store

import com.github.horvathandris.durablestreams.InvalidDataException
import com.github.horvathandris.durablestreams.StreamExistsException
import com.github.horvathandris.durablestreams.StreamNotFoundException
import com.github.horvathandris.durablestreams.stream.Message
import com.github.horvathandris.durablestreams.stream.Offset
import com.github.horvathandris.durablestreams.stream.Producer
import com.github.horvathandris.durablestreams.stream.StreamMetadata
import com.github.horvathandris.durablestreams.stream.configMatches
import com.github.horvathandris.durablestreams.stream.isExpired
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlin.time.Clock
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class InMemoryStore : Store {

  private val log = KotlinLogging.logger {}

  data class InMemoryStream(
    val metadata: StreamMetadata,
    val messages: List<Message> = mutableListOf(),
  )

  val mutex = Mutex()
  val streams = mutableMapOf<String, InMemoryStream>()

  override suspend fun create(
    path: Path,
    options: CreateOptions,
  ): CreateResult = mutex.withLock {
    log.info { "Creating stream for path: $path" }
    streams[path]?.let { existing ->
      when {
        existing.metadata.isExpired() -> streams.remove(path)
        existing.metadata.configMatches(options) ->
          return@withLock CreateResult(existing.metadata, newlyCreated = false)
        else -> throw StreamExistsException()
      }
    }

    val metadata = StreamMetadata(
      path = path,
      contentType = options.contentType,
      currentOffset = Offset.Zero,
      ttlSeconds = options.ttlSeconds,
      expiresAt = options.expiresAt,
      createdAt = Clock.System.now(),
      closed = options.closed,
    )

    streams[path] = InMemoryStream(metadata)

    // TODO: handle initial data

    log.info { "Created stream for path: $path" }
    CreateResult(metadata, newlyCreated = true)
  }

  override fun get(path: Path): StreamMetadata? =
    streams[path]
      ?.metadata
      ?.takeUnless { it.isExpired() }

  override fun has(path: Path): Boolean =
    get(path) != null

  override suspend fun delete(path: Path): Unit = mutex.withLock {
    log.info { "Deleting stream for path: $path" }
    streams.remove(path) ?: throw StreamNotFoundException()
  }

  override suspend fun close(
    path: Path,
    producer: Producer?,
  ): CloseResult = mutex.withLock {
    log.info { "Closing stream for path: $path" }
    val metadata = get(path) ?: throw StreamExistsException()
    streams[path] = InMemoryStream(
      metadata = metadata.copy(closed = true, closedBy = producer),
      messages = streams[path]?.messages ?: mutableListOf(),
    )
    log.info { "Closed stream for path: $path" }

    // TODO: Notify pending long-polls that stream is closed

    CloseResult(metadata.currentOffset)
  }

  override suspend fun append(
    path: Path,
    data: ByteArray,
    producer: Producer?,
    options: AppendOptions,
  ): AppendResult {
    if (data.isEmpty()) {
      throw InvalidDataException("empty body not allowed")
    }

    val metadata = get(path) ?: throw StreamNotFoundException()
    if (metadata.closed) {
      if (producer != null && metadata.closedBy == producer) {
        return AppendResult.StreamClosed(
          offset = metadata.currentOffset,
          duplicate = true,
        )
      }
      return AppendResult.StreamClosed(offset = metadata.currentOffset)
    }


    TODO("Not yet implemented")
  }

}