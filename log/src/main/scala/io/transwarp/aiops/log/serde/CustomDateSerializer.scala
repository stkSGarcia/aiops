package io.transwarp.aiops.log.serde

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.{JsonSerializer, SerializerProvider}
import io.transwarp.aiops.Component.Component
import io.transwarp.aiops.log.parser.Level.Level
import org.joda.time.DateTime

class CustomDateSerializer extends JsonSerializer[DateTime] {

  override def serialize(t: DateTime, jsonGenerator: JsonGenerator, serializerProvider: SerializerProvider): Unit = {
    jsonGenerator.writeNumber(t.getMillis)
  }
}


class CustomLevelSerializer extends JsonSerializer[Level] {
  override def serialize(t: Level, jsonGenerator: JsonGenerator, serializerProvider: SerializerProvider): Unit = {
    jsonGenerator.writeString(t.toString)
  }
}

class CustomComponentSerializer extends JsonSerializer[Component] {
  override def serialize(t: Component, jsonGenerator: JsonGenerator, serializerProvider: SerializerProvider): Unit = {
    jsonGenerator.writeString(t.toString)
  }
}
