package io.transwarp.aiops.log.parser

import java.io.File

import io.transwarp.aiops.Component.Component
import io.transwarp.aiops.log.message.LogEntity

trait Parser {
  def setSource(files: Array[File])

  def getComponents: java.util.Set[Component]

  def hasNext: Boolean

  def next: LogEntity

  def getProgress: Double
}
