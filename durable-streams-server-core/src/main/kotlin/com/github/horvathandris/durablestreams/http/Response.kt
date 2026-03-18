package com.github.horvathandris.durablestreams.http

class Response(
  val status: Int,
  val headers: Headers = Headers(),
  val data: ByteArray?,
) {

  constructor(
    status: Int,
    headers: Headers = Headers(),
    data: String,
  ) : this(
    status = status,
    headers = headers,
    data = data.toByteArray(),
  )

  constructor(
    status: Int,
    headers: Headers = Headers(),
  ) : this(
    status = status,
    headers = headers,
    data = null,
  )

}
