package com.github.horvathandris.durablestreams.stream

import com.github.horvathandris.durablestreams.InvalidHeaderException
import com.github.horvathandris.durablestreams.http.Headers

data class Producer(
  val id: ProducerId,
  val epoch: Epoch,
  val seq: Seq,
) {

  companion object {

    fun fromHeaders(
      headers: Headers,
    ): Producer? {
      val idHeader = headers[Headers.Producer.Id].firstOrNull()?.takeIf { it.isNotBlank() }
      val epochHeader = headers[Headers.Producer.Epoch].firstOrNull()?.takeIf { it.isNotBlank() }
      val seqHeader = headers[Headers.Producer.Seq].firstOrNull()?.takeIf { it.isNotBlank() }
      val providedCount = listOfNotNull(idHeader, epochHeader, seqHeader).size
      if (providedCount in 1..2) {
        throw InvalidHeaderException("all producer headers (Producer-Id, Producer-Epoch, Producer-Seq) must be provided together")
      }
      if (providedCount == 0) return null
      val epoch = epochHeader?.toLongOrNull()
        ?: throw InvalidHeaderException("invalid Producer-Epoch: must be an integer")
      val seq = seqHeader?.toLongOrNull()
        ?: throw InvalidHeaderException("invalid Producer-Seq: must be an integer")
      return Producer(idHeader!!, epoch, seq)
    }

  }

}