package com.github.horvathandris.durablestreams.json

import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.SerializationStrategy
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.serializer

class KotlinxJsonSerializer(
  private val json: Json = Json,
) : JsonSerializer {

  override fun <T> serialize(value: T, type: Class<T>): ByteArray {
    @Suppress("UNCHECKED_CAST")
    val serializer = json.serializersModule.serializer(type) as SerializationStrategy<T>
    return json.encodeToString(serializer, value).toByteArray()
  }

  override fun <T> deserialize(jsonByteArray: ByteArray, type: Class<T>): T {
    @Suppress("UNCHECKED_CAST")
    val deserializer = json.serializersModule.serializer(type) as DeserializationStrategy<T>
    return json.decodeFromString(deserializer, jsonByteArray.decodeToString())
  }

  override fun isArray(jsonByteArray: ByteArray): Boolean {
    return json.parseToJsonElement(jsonByteArray.decodeToString()) is JsonArray
  }

}