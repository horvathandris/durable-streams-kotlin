package com.github.horvathandris.durablestreams.http

data class URI(
  val scheme: String,
  val host: String,
  val path: String,
  val query: Map<String, List<String>>,
) {

  companion object {

    fun parse(input: String): URI =
      java.net.URI.create(input).let {
        URI(
          scheme = it.scheme,
          host = it.host,
          path = it.path,
          query = mapOf(), // TODO: parse query into map
        )
      }

  }

}