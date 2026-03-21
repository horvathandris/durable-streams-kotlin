package com.github.horvathandris.durablestreams.http

class Request(
  val uri: URI,
  val headers: Headers,
  val data: ByteArray = ByteArray(0),
) {

  fun query(key: String): List<String>? =
    uri.query[key]

  val path get() = uri.path

}
