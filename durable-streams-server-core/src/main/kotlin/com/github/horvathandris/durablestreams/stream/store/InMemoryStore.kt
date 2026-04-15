package com.github.horvathandris.durablestreams.stream.store

import com.github.horvathandris.durablestreams.ContentTypeMismatchException
import com.github.horvathandris.durablestreams.InvalidDataException
import com.github.horvathandris.durablestreams.StreamExistsException
import com.github.horvathandris.durablestreams.StreamNotFoundException
import com.github.horvathandris.durablestreams.json.JsonSerializer
import com.github.horvathandris.durablestreams.json.deserialize
import com.github.horvathandris.durablestreams.stream.ContentType
import com.github.horvathandris.durablestreams.stream.Message
import com.github.horvathandris.durablestreams.stream.Offset
import com.github.horvathandris.durablestreams.stream.Producer
import com.github.horvathandris.durablestreams.stream.StreamMetadata
import com.github.horvathandris.durablestreams.stream.configMatches
import com.github.horvathandris.durablestreams.stream.isExpired
import io.github.oshai.kotlinlogging.KotlinLogging
import io.github.oshai.kotlinlogging.coroutines.withLoggingContextAsync
import kotlin.time.Clock
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class InMemoryStore(
  private val serializer: JsonSerializer,
) : Store {

  private val log = KotlinLogging.logger {}

  private val mutex = Mutex()
  private val streams = mutableMapOf<String, InMemoryStream>()

  override suspend fun create(
    path: Path,
    options: CreateOptions,
  ): CreateResult = withLoggingContextAsync(
    "stream.path" to path,
  ) {
    mutex.withLock {
      log.info { "Creating stream" }
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

      val stream = InMemoryStream(metadata)
      options.initialData?.let {
        stream.append(
          data = options.initialData,
          serializer = serializer,
          allowEmpty = true,
        )
        log.info { "Appended initial data to stream" }
      }
      streams[path] = stream

      log.info { "Created stream" }
      CreateResult(metadata, newlyCreated = true)
    }
  }

  override fun get(path: Path): StreamMetadata? =
    streams[path]
      ?.metadata
      ?.takeUnless { it.isExpired() }

  override fun has(path: Path): Boolean =
    get(path) != null

  override suspend fun delete(path: Path): Unit = withLoggingContextAsync(
    "stream.path" to path,
  ) {
    mutex.withLock {
      log.info { "Deleting stream" }
      streams.remove(path) ?: throw StreamNotFoundException()
    }
  }

  override suspend fun close(
    path: Path,
    producer: Producer?,
  ): CloseResult = withLoggingContextAsync(
    "stream.path" to path,
  ) {
    mutex.withLock {
      log.info { "Closing stream" }
      val metadata = get(path) ?: throw StreamExistsException()
      streams[path] = InMemoryStream(
        metadata = metadata.copy(closed = true, closedBy = producer),
        messages = streams[path]?.messages ?: mutableListOf(),
      )
      log.info { "Closed stream" }

      // TODO: Notify pending long-polls that stream is closed

      CloseResult(metadata.currentOffset)
    }
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

    val stream = streams[path] ?: throw StreamNotFoundException()
    val metadata = stream.metadata
    if (metadata.closed) {
      if (producer != null && metadata.closedBy == producer) {
        return AppendResult.StreamClosed(
          offset = metadata.currentOffset,
          duplicate = true,
        )
      }
      return AppendResult.StreamClosed(offset = metadata.currentOffset)
    }

    if (metadata.contentType != options.contentType) {
      throw ContentTypeMismatchException()
    }

    stream.append(data, serializer)

    return AppendResult.StreamAppended(
      offset = metadata.currentOffset,
    )
  }

  data class InMemoryStream(
    val metadata: StreamMetadata,
    val messages: MutableList<Message> = mutableListOf(),
  ) {

    fun append(
      data: ByteArray,
      serializer: JsonSerializer,
      allowEmpty: Boolean = false,
    ) {
      val dataArray = if (
        metadata.contentType == ContentType.ApplicationJson
        && serializer.isArray(data)
      ) {
        serializer.deserialize<List<ByteArray>>(data)
          .apply {
            if (!allowEmpty && isEmpty()) {
              throw InvalidDataException("empty JSON array not allowed")
            }
          }
      } else {
        listOf(data)
      }

      dataArray.forEach { this.append(it) }
    }

    private fun append(data: ByteArray) {
      val newOffset = metadata.currentOffset + data.size
      val message = Message(
        data = data,
        offset = newOffset,
      )
      messages.add(message)
      metadata.currentOffset = newOffset
    }

  }

}