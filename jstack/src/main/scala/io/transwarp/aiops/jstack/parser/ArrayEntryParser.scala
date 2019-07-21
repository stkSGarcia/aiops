package io.transwarp.aiops.jstack.parser

import io.transwarp.aiops.jstack.structure.JstackEntry

// TODO: For parsing string
private[parser] class ArrayEntryParser extends EntryParser {
  def setSource(string: String): this.type = ???

  override def iterator: Iterator[JstackEntry] = ???
}
