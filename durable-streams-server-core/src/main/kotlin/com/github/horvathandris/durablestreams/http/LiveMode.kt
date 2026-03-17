package com.github.horvathandris.durablestreams.http

enum class LiveMode(
  val value: String,
) {
  LongPoll("long-poll"),
  SSE("sse"),
  ;

  companion object {
    fun fromValue(value: String): LiveMode? =
      entries.find { it.value == value }
  }
}