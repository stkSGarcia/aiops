package io.transwarp.aiops.log.parser

import java.util.regex.Pattern

import io.transwarp.aiops.Component.Component
import io.transwarp.aiops.log.conf.{LogFormatField, LogFormat}
import io.transwarp.aiops.log.message.LogEntity
import io.transwarp.aiops.log.parser.Level.Level
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormatter

private[parser] class LogEntry(val head: String,
                               val content: String,
                               val fileIndex: Int,
                               logFormat: LogFormat) {
  if (logFormat == null) throw new IllegalArgumentException("Log format cannot be empty.")

  val component: Component = logFormat.component
  // FIXME 0~23: yyyy-MM-dd HH:mm:ss,SSS
  val _timestamp: String = head.substring(0, 23)
  val timestamp: DateTime = {
    // val matcher = logFormat.patterns(LogField.TIMESTAMP).asInstanceOf[Pattern].matcher(head)
    // if (matcher.find) DateTime.parse(matcher.group(0), logFormat.config(LogField.TIME_FORMAT).asInstanceOf[DateTimeFormatter]) else null
    DateTime.parse(_timestamp, logFormat.config(LogFormatField.TIME_FORMAT).asInstanceOf[DateTimeFormatter])
  }
  val level: Level = {
    val matcher = logFormat.patterns(LogFormatField.LEVEL).asInstanceOf[Pattern].matcher(head)
    if (matcher.find) Level.withCaseIgnoreName(matcher.group(0)) else Level.UNKNOWN
  }
  val size: Int = head.length + content.length

  def convertToLogEntity: LogEntity = LogEntity(component, timestamp, level, head + "\n" + content)
}

object Level extends Enumeration {
  type Level = Value
  val UNKNOWN = Value
  val DEBUG = Value
  val TRACE = Value
  val INFO = Value
  val WARN = Value
  val ERROR = Value
  val FATAL = Value
  // a fake entity with END level to mark the end of log
  val END = Value

  implicit class SuitValue(suit: Value) {
    def isError: Boolean = suit match {
      case ERROR | FATAL => true
      case _ => false
    }
  }

  def withCaseIgnoreName(level: String): Level = values.find(_.toString == level.toUpperCase).getOrElse(UNKNOWN)
}
