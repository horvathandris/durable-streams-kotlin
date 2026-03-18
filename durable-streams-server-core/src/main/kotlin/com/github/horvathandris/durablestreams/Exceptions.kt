package com.github.horvathandris.durablestreams

class StreamExistsException(
  override val message: String = "stream exists with different configuration"
) : Exception()

class StreamNotFoundException(
  override val message: String = "stream not found"
) : Exception()

class InvalidHeaderException(
  override val message: String,
) : Exception()

class InvalidParameterException(
  override val message: String,
) : Exception()