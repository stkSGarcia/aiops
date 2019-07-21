package io.transwarp.aiops

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.{DeserializationContext, JsonDeserializer, KeyDeserializer}

object Component extends Enumeration {
  type Component = Value
  val UNKNOWN, WILDCARD,
  HADOOP, HBASE, HDFS, HYPERBASE,
  INCEPTOR,
  MANAGER,
  OS,
  SEARCH, SHIVA, SOPHON,
  TOS
  = Value

  def withCaseIgnoreName(s: String): Component = values.find(_.toString == s.toUpperCase).getOrElse(UNKNOWN)

  def exist(s: String): Boolean = values.exists(f => {
    val comp = f.toString
    comp != "UNKNOWN" && comp != "WILDCARD" && comp == s.toUpperCase
  })
}

/**
  * Use @JsonDeserialize(using = classOf[ComponentDeserializer])
  */
class ComponentDeserializer extends JsonDeserializer[Component.Value] {
  override def deserialize(jsonParser: JsonParser, deserializationContext: DeserializationContext): Component.Value =
    Component withCaseIgnoreName jsonParser.getValueAsString
}

/**
  * Use @JsonDeserialize(keyUsing = classOf[ComponentKeyDeserializer])
  */
class ComponentKeyDeserializer extends KeyDeserializer {
  override def deserializeKey(s: String, deserializationContext: DeserializationContext): AnyRef =
    Component withCaseIgnoreName s
}
