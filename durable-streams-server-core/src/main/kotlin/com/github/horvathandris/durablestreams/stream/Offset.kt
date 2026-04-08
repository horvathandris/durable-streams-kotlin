package com.github.horvathandris.durablestreams.stream

import com.github.horvathandris.durablestreams.InvalidParameterException

/**
 * Represents a position within a stream.
 * Format: "0000000000000000_0000000000000000" (16 digits each, zero-padded)
 * The format is lexicographically sortable.
 */
data class Offset(
  val readSeq: Seq,
  val bytesOffset: Long,
) {

  companion object {
    val Zero = Offset(0, 0)
    val Now = Offset(Long.MAX_VALUE, Long.MAX_VALUE)
  }

  override fun toString() =
    "%016d_%016d".format(readSeq, bytesOffset)

  operator fun plus(bytesSize: Number) =
    Offset(
      readSeq = readSeq,
      bytesOffset = bytesOffset + bytesSize.toLong(),
    )

}

/**
 * Special cases:
 *  - "-1" returns ZeroOffset (meaning "start from beginning")
 *  - "now" returns NowOffset (meaning "current tail position, skip historical data")
 *
 * Returns error for invalid formats.
 */
fun List<String>.toOffset(): Offset {
  if (this.size > 1) {
    throw InvalidParameterException("multiple offset parameters not allowed")
  }

  if (this.isEmpty() || this.first().isBlank()) {
    throw InvalidParameterException("offset cannot be empty")
  }

  val value = this.firstOrNull()
  if (value.isNullOrBlank() || value == "-1") return Offset.Zero
  if (value == "now") return Offset.Now

  val parts = value.split("_")
  if (parts.size != 2) throw InvalidParameterException("invalid offset")
  val readSeq = parts[0].toLongOrNull() ?: throw InvalidParameterException("invalid offset")
  val bytesOffset = parts[1].toLongOrNull() ?: throw InvalidParameterException("invalid offset")
  return Offset(readSeq, bytesOffset)
}

