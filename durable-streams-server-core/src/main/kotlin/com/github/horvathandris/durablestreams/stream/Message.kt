package com.github.horvathandris.durablestreams.stream

class Message(
  val data: ByteArray,
  val offset: Offset,
)
