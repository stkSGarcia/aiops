package io.transwarp.aiops.log.parser

import java.io.File
import java.util.regex.Pattern

import io.transwarp.aiops.Component.Component
import io.transwarp.aiops.log.conf.{LogConf, LogFormatField}

import scala.io.Source

private[parser] class FileParser(file: File, fileIndex: Int, component: Component) {
  private val lines = Source.fromFile(file).getLines
  private val logFormat = LogConf.logFormats(component)
  private val headRegex = logFormat.patterns(LogFormatField.HEAD).asInstanceOf[Pattern]
  private var head: String = _
  private var nextEntry: LogEntry = _

  def next: LogEntry = nextEntry

  def hasNext: Boolean = {
    var line: String = null
    val content = new StringBuilder
    var nextHead: String = null
    var break = false

    while (!break && lines.hasNext) {
      line = lines.next
      if (!headRegex.matcher(line).find) {
        content.append(line).append('\n')
      } else if (head != null) {
        break = true
        nextHead = line
      } else {
        head = line
        content.clear
      }
    }

    if (head != null) {
      nextEntry = new LogEntry(head, content.toString, fileIndex, logFormat)
      head = nextHead
      true
    } else false
  }
}
