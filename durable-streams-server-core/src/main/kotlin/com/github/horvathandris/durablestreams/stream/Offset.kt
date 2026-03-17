package com.github.horvathandris.durablestreams.stream

import com.github.horvathandris.durablestreams.InvalidOffsetException

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

}

/**
 * Special cases:
 *  - "-1" returns ZeroOffset (meaning "start from beginning")
 *  - "now" returns NowOffset (meaning "current tail position, skip historical data")
 *
 * Returns error for invalid formats.
 */
fun String?.toOffset(): Offset {
  if (this.isNullOrBlank() || this == "-1") return Offset.Zero
  if (this == "now") return Offset.Now

  val parts = this.split("_")
  if (parts.size != 2) throw InvalidOffsetException()
  val readSeq = parts[0].toLongOrNull() ?: throw InvalidOffsetException()
  val bytesOffset = parts[1].toLongOrNull() ?: throw InvalidOffsetException()
  return Offset(readSeq, bytesOffset)
}

