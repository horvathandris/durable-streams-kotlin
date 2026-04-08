package com.github.horvathandris.durablestreams.stream.store

import com.github.horvathandris.durablestreams.InvalidHeaderException
import com.github.horvathandris.durablestreams.http.Headers
import com.github.horvathandris.durablestreams.http.Request
import com.github.horvathandris.durablestreams.stream.ContentType
import com.github.horvathandris.durablestreams.stream.Offset
import com.github.horvathandris.durablestreams.stream.Producer
import com.github.horvathandris.durablestreams.stream.Seq
import com.github.horvathandris.durablestreams.stream.StreamMetadata
import kotlin.time.Instant

typealias Path = String

/**
 * Options for creating a stream.
 */
class CreateOptions(
    val contentType: ContentType = ContentType.ApplicationOctetStream,
    val ttlSeconds: Long?,
    val expiresAt: Instant?,
    val initialData: ByteArray?,
    val closed: Boolean,
) {

    companion object {

        fun fromRequest(request: Request): CreateOptions {
            val ttlSecondsHeader = request.headers[Headers.Stream.TTL]
                .firstOrNull()
                ?.takeIf { it.isNotBlank() }
            val expiresAtHeader = request.headers[Headers.Stream.ExpiresAt]
                .firstOrNull()
                ?.takeIf { it.isNotBlank() }
            if (ttlSecondsHeader != null && expiresAtHeader != null) {
                throw InvalidHeaderException("cannot specify both Stream-TTL and Stream-Expires-At")
            }
            val contentType = request.headers[Headers.Http.ContentType]
                .firstOrNull()
                ?.let { ContentType.parseOrNull(it) ?: ContentType.ApplicationOctetStream }
                ?: throw InvalidHeaderException("invalid Content-Type format")
            val ttlSeconds = ttlSecondsHeader?.let {
                it.trim().toLongOrNull() ?: throw InvalidHeaderException("invalid Stream-TTL format")
            }
            val expiresAt = expiresAtHeader?.let {
                Instant.parseOrNull(it) ?: throw InvalidHeaderException("invalid Stream-Expires-At format")
            }
            val closed = request.headers[Headers.Stream.Closed].firstOrNull() == "true"
            return CreateOptions(
                contentType = contentType,
                ttlSeconds = ttlSeconds,
                expiresAt = expiresAt,
                initialData = request.data,
                closed = closed,
            )
        }

    }

}

/**
 * @param newlyCreated Whether the stream was newly created,
 *                     or it existed with matching config.
 */
data class CreateResult(
    val metadata: StreamMetadata,
    val newlyCreated: Boolean,
)

data class CloseResult(
    val finalOffset: Offset,
)

data class AppendOptions(
    val seq: Seq,
    val contentType: ContentType,
    val close: Boolean,
) {
    companion object {

        fun fromRequest(request: Request): AppendOptions {
            val seq = request.headers[Headers.Stream.Seq]
                .firstOrNull()
                ?.trim()
                ?.toLongOrNull()
                ?: throw InvalidHeaderException("invalid Stream-Seq format")
            val contentType = request.headers[Headers.Http.ContentType]
                .firstOrNull()
                ?.let { ContentType.parseOrNull(it) ?: ContentType.ApplicationOctetStream }
                ?: throw InvalidHeaderException("invalid Content-Type format")
            val closed = request.headers[Headers.Stream.Closed].firstOrNull() == "true"
            return AppendOptions(
                seq = seq,
                contentType = contentType,
                close = closed,
            )
        }

    }
}

sealed class AppendResult(
    val offset: Offset,
    val duplicate: Boolean,
) {
    class StreamClosed(
        offset: Offset,
        duplicate: Boolean = false,
    ) : AppendResult(
        offset = offset,
        duplicate = duplicate,
    )

    class StreamAppended(
        offset: Offset,
    ) : AppendResult(
        offset = offset,
        duplicate = false,
    )
}

/**
 * Store is the interface for durable stream storage.
 */
interface Store {

    /**
     * Creates a new stream, or returns the stream if it
     * exists with the same config (idempotent).
     * @throws com.github.horvathandris.durablestreams.StreamExistsException if stream exists with different config.
     */
    suspend fun create(path: Path, options: CreateOptions): CreateResult

    /**
     * Returns the metadata for the stream if a stream exists for the path.
     */
    fun get(path: Path): StreamMetadata?

    /**
     * @return `true` if a stream exists for the path.
     */
    fun has(path: Path): Boolean

    /**
     * @throws com.github.horvathandris.durablestreams.StreamNotFoundException if no stream exists for the path.
     */
    suspend fun delete(path: Path)

    suspend fun close(path: Path, producer: Producer?): CloseResult

    suspend fun append(
        path: Path,
        data: ByteArray,
        producer: Producer?,
        options: AppendOptions,
    ): AppendResult

}