package com.github.horvathandris.durablestreams.stream

import com.github.horvathandris.durablestreams.InvalidProducerEpochException
import com.github.horvathandris.durablestreams.InvalidProducerSeqException
import com.github.horvathandris.durablestreams.MissingProducerHeadersException

data class Producer(
  val id: ProducerId,
  val epoch: Epoch,
  val seq: Seq,
) {

  companion object {

    fun fromHeaders(
      id: String?,
      epoch: String?,
      seq: String?,
    ): Producer? {
      val providedCount = listOfNotNull(id, epoch, seq).size
      if (providedCount in 1..2) throw MissingProducerHeadersException()
      if (providedCount == 0) return null
      val epochLong = epoch?.toLongOrNull() ?: throw InvalidProducerEpochException()
      val seqLong = seq?.toLongOrNull() ?: throw InvalidProducerSeqException()
      return Producer(id!!, epochLong, seqLong)
    }

  }

}