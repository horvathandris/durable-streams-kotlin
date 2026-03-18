package com.github.horvathandris.durablestreams.http

data class Headers(
  private val map: MutableMap<String, List<String>>,
) {

  object Http {
    const val ContentType = "Content-Type"
    const val CacheControl = "Cache-Control"
    const val XForwardedProto = "X-Forwarded-Proto"
    const val XForwardedHost = "X-Forwarded-Host"
    const val Location = "Location"
  }

  object Stream {
    const val Seq = "Stream-Seq"
    const val TTL = "Stream-TTL"
    const val ExpiresAt = "Stream-Expires-At"
    const val Closed = "Stream-Closed"
    const val NextOffset = "Stream-Next-Offset"
    const val Cursor = "Stream-Cursor"
    const val UpToDate = "Stream-Up-To-Date"
  }

  object Producer {
    const val Id = "Producer-Id"
    const val Epoch = "Producer-Epoch"
    const val Seq = "Producer-Seq"
    const val ExpectedSeq = "Producer-Expected-Seq"
    const val ReceivedSeq = "Producer-Received-Seq"
  }

  companion object {

    fun of(vararg pairs: Pair<String, String>): Headers =
      Headers(
        pairs.groupBy(
          { it.first.lowercase() },
          { it.second }
        )
          .toMutableMap()
      )
  }

  operator fun get(name: String): List<String> =
    map[name.lowercase()] ?: emptyList()

  operator fun set(index: String, value: String) {
    map[index] = listOf(value)
  }

  operator fun iterator(): MutableIterator<MutableMap.MutableEntry<String, List<String>>> =
    map.iterator()

}