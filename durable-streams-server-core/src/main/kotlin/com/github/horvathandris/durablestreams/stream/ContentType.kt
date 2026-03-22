package com.github.horvathandris.durablestreams.stream

data class ContentType(
  val type: String,
  val subType: String,
) {

  companion object {

    val ApplicationOctetStream = ContentType("application", "octet-stream")

    fun parseOrNull(value: String): ContentType? {
      val parts = value.trim()
        .substringBefore(";")
        .trim()
        .split('/')

      if (parts.size != 2) return null

      val type = parts[0].trim()
      val subtype = parts[1].trim()

      if (type.isEmpty() || subtype.isEmpty()) return null

      return ContentType(type, subtype)
    }

  }

  override fun toString(): String =
    "$type/$subType"

}