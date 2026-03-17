package com.github.horvathandris.durablestreams

class StreamExistsException : Exception()

class StreamNotFoundException : Exception()

class InvalidOffsetException : Exception()

class MissingProducerHeadersException : Exception() {
  override val message =
    "All producer headers (Producer-Id, Producer-Epoch, Producer-Seq) must be provided together"
}

class InvalidProducerEpochException : Exception()

class InvalidProducerSeqException : Exception()