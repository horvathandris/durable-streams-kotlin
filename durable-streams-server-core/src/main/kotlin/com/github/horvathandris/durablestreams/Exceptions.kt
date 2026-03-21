package com.github.horvathandris.durablestreams

import com.github.horvathandris.durablestreams.http.Response

sealed class DurableStreamsException(
  val status: Int,
  override val message: String,
) : Exception(message)

internal fun DurableStreamsException.toResponse(): Response {
  return Response(
    status = this.status,
    data = this.message,
  )
}

class StreamExistsException(
  override val message: String = "stream exists with different configuration",
) : DurableStreamsException(409, message)

class StreamNotFoundException(
  override val message: String = "stream not found",
) : DurableStreamsException(404, message)

class InvalidHeaderException(
  override val message: String,
) : DurableStreamsException(400, message)

class InvalidParameterException(
  override val message: String,
) : DurableStreamsException(400, message)

class StaleEpochException(
  override val message: String = "producer epoch is stale",
) : DurableStreamsException(400, message)

class InvalidEpochSeqException(
  override val message: String = "new epoch must start at sequence 0",
) : DurableStreamsException(400, message)

class ProducerSeqGapException(
  override val message: String = "new epoch must start at sequence 0",
) : DurableStreamsException(400, message)

class InvalidDataException(
  override val message: String,
) : DurableStreamsException(400, message)