package io.transwarp.aiops.log.parser

import java.io.File
import java.util

import io.transwarp.aiops.Component
import io.transwarp.aiops.Component.Component
import io.transwarp.aiops.log.message.LogEntity

import scala.collection.JavaConverters._
import scala.collection.mutable

class LogParser extends Parser {
  private val minHeap: mutable.PriorityQueue[(LogEntry, Int)] = mutable.PriorityQueue.empty(
    new Ordering[(LogEntry, Int)] {
      override def compare(x: (LogEntry, Int), y: (LogEntry, Int)): Int = {
        if (x._1._timestamp == null) 1
        else if (y._1._timestamp == null) -1
        else compareTimeString(x._1._timestamp, y._1._timestamp)
      }
    })
  private var sourceMap: Map[Int, FileParser] = _
  private var nextEntry: LogEntry = _
  private var totalSize: Long = _
  private var currentSize: Long = _

  override def setSource(files: Array[File]): Unit = {
    sourceMap = files.zipWithIndex.map { case (file, fileIndex) =>
      totalSize += file.length
      val fileParser = new FileParser(file, fileIndex, Component.INCEPTOR)
      if (fileParser.hasNext) minHeap.enqueue((fileParser.next, fileIndex))
      fileIndex -> fileParser
    }.toMap
  }

  override def next: LogEntity = {
    currentSize += nextEntry.size
    nextEntry.convertToLogEntity
  }

  override def hasNext: Boolean =
    if (minHeap.nonEmpty) {
      val (entity, fileIndex) = minHeap.dequeue
      nextEntry = entity
      // insert next entity
      val fileParser = sourceMap(fileIndex)
      if (fileParser.hasNext) minHeap.enqueue((fileParser.next, fileIndex))
      true
    } else false

  override def getComponents: util.Set[Component] = Set(Component.INCEPTOR).asJava

  override def getProgress: Double = currentSize.toDouble / totalSize.toDouble

  /**
    * Compare two timestamp strings.
    *
    * @param time1 left timestamp
    * @param time2 right timestamp
    * @return if time1 is before time2, then return 1, otherwise return -1.
    *         if time1 and time2 are equal, then return 0.
    */
  private def compareTimeString(time1: String, time2: String): Int = {
    if (time1.length != time2.length) throw new IllegalArgumentException(s"$time1 cannot be compared with $time2.")
    val length = time1.length
    var i = 0
    while (i < length) {
      val diff = time1(i) - time2(i)
      if (diff != 0) return if (diff > 0) -1 else 1
      i += 1
    }
    0
  }
}
