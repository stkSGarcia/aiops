package io.transwarp.aiops.log.message

import com.fasterxml.jackson.databind.annotation.JsonSerialize
import io.transwarp.aiops.Component
import io.transwarp.aiops.Component.Component
import io.transwarp.aiops.log.parser.Level.Level
import io.transwarp.aiops.log.serde.{CustomComponentSerializer, CustomDateSerializer, CustomLevelSerializer}
import org.joda.time.DateTime

/**
  * This class define an entity of a log.
  */
case class LogEntity(@JsonSerialize(using = classOf[CustomComponentSerializer]) component: Component = Component.UNKNOWN,
                     @JsonSerialize(using = classOf[CustomDateSerializer]) timeStamp: DateTime = null,
                     @JsonSerialize(using = classOf[CustomLevelSerializer]) level: Level = null,
                     content: String = null)

/**
  * This class define an inceptor entity of logs.
  */
case class InceptorLogEntity(session: String, logEntity: LogEntity)
