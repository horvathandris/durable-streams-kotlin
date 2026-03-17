package com.github.horvathandris.durablestreams.stream

/**
 * Represents a position within a stream.
 * Format: "0000000000000000_0000000000000000" (16 digits each, zero-padded)
 * The format is lexicographically sortable.
 */
data class Offset(
  val readSeq: Seq,
  val bytesOffset: Long,
) {

  override fun toString() =
    "%016d_%016d".format(readSeq, bytesOffset)

}

fun zeroOffset() =
  Offset(0, 0)

