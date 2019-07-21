package io.transwarp.aiops.log.conf

import com.fasterxml.jackson.annotation.{JsonIgnoreProperties, JsonProperty}
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import io.transwarp.aiops.Component.Component
import io.transwarp.aiops.log.conf.LogFormatField.LogFormatField
import io.transwarp.aiops.{ComponentDeserializer, ConfBean}

@JsonIgnoreProperties(ignoreUnknown = true)
case class LogConfBean(
//                        @JsonProperty("model_template")
                      @JsonProperty("entry_format")
                      @JsonDeserialize(using = classOf[LogFormatMapDeserializer])
                        logFormats: Map[Component, LogFormat],
                      @JsonProperty("component_keywords")
                      @JsonDeserialize(using = classOf[ComponentMapDeserializer])
                        compMap: Map[String, Component]
                      ) extends ConfBean

private[log] case class LogFormat(component: Component,
                                  config: Map[LogFormatField, Any],
                                  patterns: Map[LogFormatField, Any])

private[log] object LogFormatField extends Enumeration {
  type LogFormatField = Value
  val CONFIG = Value("config")
  val TIME_FORMAT = Value("time_format")
  val PATTERNS = Value("patterns")
  val HEAD = Value("head")
  val TIMESTAMP = Value("timestamp")
  val LEVEL = Value("level")
  val UNIT = Value("unit")

  def withNameOpt(s: String): Option[Value] = values.find(_.toString == s)
}
