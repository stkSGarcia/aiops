package io.transwarp.aiops.jstack.parser

import java.io.{File, InputStream}

import io.transwarp.aiops.jstack.structure.{JstackEntry, Lock, StartLineState, ThreadState}

import scala.collection.mutable.ArrayBuffer
import scala.io.Source

/**
  * This class should be used for parsing a single jstack file.
  */
private[parser] class FileEntryParser extends EntryParser {
  private var source: Iterator[String] = _

  def setSource(file: File): this.type = {
    source = Source.fromFile(file).getLines
    this
  }

  def setSource(inputStream: InputStream): this.type = {
    source = Source.fromInputStream(inputStream).getLines
    this
  }

  /**
    * Note: please setSource before invoking iterator.
    *
    * @return An iterator which produces JstackEntries
    */
  override def iterator: Iterator[JstackEntry] = new Iterator[JstackEntry] {
    private var curLine: String = _

    {
      // Find the timestamp and the first line of the first jstack entry.
      if (source.hasNext) {
        curLine = source.next.trim
        val matcher = JstackPattern.timePattern.matcher(curLine)
        if (matcher.find) timestamp = matcher.group(1)
      }
      consumeUntilFirstLine()
    }

    @inline
    private def nextLine(): Unit = curLine = if (source.hasNext) source.next.trim else null

    @inline
    private def consumeUntilFirstLine(): Unit = {
      var break = false
      while (!break && curLine != null) {
        if (curLine.startsWith("\"")) break = true
        else nextLine()
      }
    }

    override def hasNext: Boolean = curLine != null

    override def next: JstackEntry = {
      var entry = new JstackEntry
      /*
      parse head
       */
      val matcher = JstackPattern.headPattern.matcher(curLine)
      var trailing = curLine
      if (matcher.find) {
        entry.threadName = matcher.group(1)
        entry.id = matcher.group(2)
        entry.isDaemon = matcher.group(3) != null
        entry.prio = matcher.group(4)
        entry.os_prio = matcher.group(5)
        entry.tid = matcher.group(6)
        entry.nid = matcher.group(7)
        trailing = matcher.group(8)
        entry.startLine = curLine
      }
      /*
      parse body
       */
      nextLine()
      val methodLines = new StringBuilder
      val lockList = new ArrayBuffer[Lock]()
      val waitLockList = new ArrayBuffer[Lock]()

      var break = false
      while (!break && curLine != null)
        curLine match {
          case l if l.startsWith("\"") => break = true
          case l if l == "" => consumeUntilFirstLine() // do not allow blank lines inside one jstack entry.
          case l if l.startsWith("=====") =>
            break = true
            curLine = null
          case l if l.startsWith("-") => // parse locks
            // lock pattern
            val lockMatcher = JstackPattern.lockPattern.matcher(l)
            if (lockMatcher.find) {
              lockList += Lock(lockMatcher.group(1), lockMatcher.group(2), l)
            }
            // wait lock pattern
            val waitLockMatcher = JstackPattern.waitLockPattern.matcher(l)
            if (waitLockMatcher.find) {
              waitLockList += Lock(waitLockMatcher.group(1), waitLockMatcher.group(2), l)
            }
            nextLine()
          case l if l.startsWith("java.lang.Thread.State") =>
            if (entry.threadState == null) {
              val matcher = JstackPattern.threadStatePattern.matcher(l)
              if (matcher.find) {
                entry.threadState = ThreadState.withName(matcher.group(0))
              } else {
                LOG.warn("[Jstack] Error state string: {}", l)
              }
            }
            methodLines.append(l).append("\n\t")
            nextLine()
          case l =>
            if (entry.method == null && l.startsWith("at")) entry.method = l
            methodLines.append(l).append("\n\t")
            nextLine()
        }

      entry.locks = if (lockList.isEmpty) null else lockList.toArray
      entry.waitLocks = if (waitLockList.isEmpty) null else waitLockList.toArray
      entry.callStack = if (methodLines.isEmpty) null else methodLines.toString.trim

      if (entry.threadState == null) {
        StartLineState.findState(trailing) match {
          case Some(x) =>
            entry.threadState = StartLineState.toThreadState(x)
          case None =>
            LOG.warn("[Jstack] Callstack is empty and no state found in the start line: {}", entry.startLine)
            entry = null
        }
      }
      entry
    }
  }.filter(_ != null)
}
