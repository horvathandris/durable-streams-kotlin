package com.github.horvathandris.durablestreams.http

@Suppress("ConstPropertyName")
data class Headers(
  private val map: Map<String, List<String>>,
) {

  constructor(): this(mapOf())

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

    fun builder(): HeadersBuilder =
      HeadersBuilder()

  }

  operator fun get(name: String): List<String> =
    map[name.lowercase()] ?: emptyList()

  operator fun iterator(): Iterator<Map.Entry<String, List<String>>> =
    map.iterator()

  class HeadersBuilder {

    private val map: MutableMap<String, MutableList<String>> = mutableMapOf()

    operator fun set(key: String, value: String): HeadersBuilder {
      map[key.lowercase()] = mutableListOf(value)
      return this
    }

    fun build(): Headers =
      Headers(map)

  }

}