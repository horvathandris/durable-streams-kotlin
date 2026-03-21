package com.github.horvathandris.durablestreams.http

data class URI(
  val scheme: String,
  val host: String,
  val path: String,
  val query: Map<String, List<String>>,
)