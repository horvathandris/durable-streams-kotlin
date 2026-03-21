package com.github.horvathandris.durablestreams.stream

import com.github.horvathandris.durablestreams.stream.store.CreateOptions
import com.github.horvathandris.durablestreams.stream.store.Path
import kotlin.time.Clock
import kotlin.time.Duration.Companion.seconds
import kotlin.time.Instant

typealias ProducerId = String
typealias Epoch = Long
typealias Seq = Long

data class StreamMetadata(
  val path: Path,
  val contentType: String,
  val currentOffset: Offset,
  val ttlSeconds: Long?,
  val expiresAt: Instant?,
  val createdAt: Instant,
  val closed: Boolean,
  val closedBy: Producer? = null,
)

fun StreamMetadata.isExpired(): Boolean {
  val now = Clock.System.now()
  return when {
    expiresAt != null -> now > expiresAt
    ttlSeconds != null -> now > createdAt.plus(ttlSeconds.seconds)
    else -> false
  }
}

fun StreamMetadata.configMatches(options: CreateOptions): Boolean =
  // TODO: content type matching should be more involved
  contentType == options.contentType &&
  ttlSeconds == options.ttlSeconds &&
  expiresAt == options.expiresAt &&
  closed == options.closed