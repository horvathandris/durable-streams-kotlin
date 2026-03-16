package com.github.horvathandris.durablestreams.store

import com.github.horvathandris.durablestreams.stream.StreamMetadata

typealias Path = String

/**
 * Options for creating a stream.
 */
data class CreateOptions(
    val contentType: String,
)

/**
 * @param newlyCreated Whether the stream was newly created,
 *                     or it existed with matching config.
 */
data class CreateStreamResult(
    val streamMetadata: StreamMetadata,
    val newlyCreated: Boolean,
)

/**
 * Store is the interface for durable stream storage.
 */
interface Store {

    /**
     * Creates a new stream, or returns the stream if it
     * exists with the same config (idempotent).
     * @throws com.github.horvathandris.durablestreams.StreamExistsException if stream exists with different config.
     */
    fun create(path: Path, options: CreateOptions): CreateStreamResult

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
    fun delete(path: Path)

}