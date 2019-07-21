package io.transwarp.aiops.jstack.parser

import java.io.{File, InputStream}

import io.transwarp.aiops.Logger
import io.transwarp.aiops.jstack.structure.JstackEntry

/**
  * Trait for parsing Jstack entries
  */
private[jstack] trait EntryParser extends Iterable[JstackEntry] with Logger {
  var timestamp: String = _
}

private[jstack] object EntryParser {
  def fromFile(file: File): EntryParser = new FileEntryParser().setSource(file)

  def fromInputStream(inputStream: InputStream): EntryParser = new FileEntryParser().setSource(inputStream)

  // TODO: @see io.transwarp.aiops.jstack.parser.ArrayEntryParser
  def fromString(string: String): EntryParser = new ArrayEntryParser().setSource(string)
}
