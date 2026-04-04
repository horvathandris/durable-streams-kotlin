package com.github.horvathandris.durablestreams.json

interface JsonSerializer {
  fun <T> serialize(value: T, type: Class<T>): ByteArray
  fun <T> deserialize(jsonByteArray: ByteArray, type: Class<T>): T
  fun isArray(jsonByteArray: ByteArray): Boolean
}

inline fun <reified T> JsonSerializer.serialize(value: T): ByteArray =
  serialize(value, T::class.java)

inline fun <reified T> JsonSerializer.deserialize(jsonByteArray: ByteArray): T =
  deserialize(jsonByteArray, T::class.java)