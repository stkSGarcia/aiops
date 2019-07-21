package io.transwarp.aiops.jstack.structure

import io.transwarp.aiops.Component
import io.transwarp.aiops.jstack.analyzer.RadixTree
import io.transwarp.aiops.jstack.conf.JstackConf
import io.transwarp.aiops.jstack.structure.BlackWhiteListLevel.BlackWhiteListLevel

import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer

class JstackFile {
  private val radixTree = new RadixTree[JstackEntry]
  // FIXME: Always be inceptor and NULL value has not been checked.
  private val stackTraceBWList = Option(JstackConf.getConf.stackTraceCompMap).map(_.getOrElse(Component.INCEPTOR, null)).orNull
  private val threadGroupBWList = JstackConf.getConf.threadGroup
  private val callStackMap = new mutable.HashMap[String, EntryListInfo]
  private val noCallStackArray = new ArrayBuffer[JstackEntry]
  // LockAddress -> (LockEntryList, WaitLockEntryList)
  private val lockMap = new mutable.HashMap[String, (ArrayBuffer[JstackEntry], ArrayBuffer[JstackEntry])]
  private val methodMap = new mutable.HashMap[String, ArrayBuffer[JstackEntry]]
  var fileName: String = _
  var timestamp: String = _
  private var _threadGroupMap: Map[String, EntryListInfo] = _

  /**
    * Add one JstackEntry and accumulate its info.
    *
    * @param entry JstackEntry
    */
  private[jstack] def +=(entry: JstackEntry): Unit = {
    // call stack
    if (entry.callStack == null) noCallStackArray += entry
    else callStackMap.get(entry.callStack) match {
      case Some(info) => info.entries += entry
      case None =>
        val entries = new ArrayBuffer[JstackEntry]
        entries += entry

        val keywords = stackTraceBWList.whiteList.filter(_.matcher(entry.callStack).find).map(_.toString)
        val callStackInfo =
          if (keywords.nonEmpty) // white list
            EntryListInfo(entries, BlackWhiteListLevel.USEFUL, keywords)
          else if (stackTraceBWList.blackList.exists(_.matcher(entry.callStack).find)) // black list
            EntryListInfo(entries, BlackWhiteListLevel.USELESS)
          else // neither
            EntryListInfo(entries, BlackWhiteListLevel.UNDEFINED)

        callStackMap += entry.callStack -> callStackInfo
    }
    // locks
    if (entry.locks != null) {
      entry.locks.foreach(lock => lockMap.get(lock.addr) match {
        case Some((lockEntries, _)) => lockEntries += entry
        case None =>
          val entryTuple = (new ArrayBuffer[JstackEntry], new ArrayBuffer[JstackEntry])
          entryTuple._1 += entry
          lockMap += lock.addr -> entryTuple
      })
    }
    if (entry.waitLocks != null) {
      entry.waitLocks.foreach(lock => lockMap.get(lock.addr) match {
        case Some((_, waitLockEntries)) => waitLockEntries += entry
        case None =>
          val entryTuple = (new ArrayBuffer[JstackEntry], new ArrayBuffer[JstackEntry])
          entryTuple._2 += entry
          lockMap += lock.addr -> entryTuple
      })
    }
    // method
    if (entry.method != null) {
      methodMap.get(entry.method) match {
        case Some(entries) => entries += entry
        case None =>
          val newEntries = new ArrayBuffer[JstackEntry]
          newEntries += entry
          methodMap += entry.method -> newEntries
      }
    }
    // thread group
    radixTree += entry.threadName -> entry
  }

  /**
    * Merge another JstackFile into this one.
    *
    * @param fileInfo JstackFile to be merged
    * @return this
    */
  private[jstack] def merge(fileInfo: JstackFile): this.type = {
    callStackMap ++= fileInfo.callStackMap.transform { case (k, v) =>
      callStackMap.get(k).foreach(v.entries ++= _.entries)
      v
    }
    noCallStackArray ++= fileInfo.noCallStackArray
    lockMap ++= fileInfo.lockMap.transform { case (k, v1) =>
      lockMap.get(k).foreach(v2 => {
        v1._1 ++= v2._1
        v1._2 ++= v2._2
      })
      v1
    }
    methodMap ++= fileInfo.methodMap.transform { case (k, v) =>
      methodMap.get(k).foreach(v ++= _)
      v
    }
    _threadGroupMap = getGroupMap ++ fileInfo.getGroupMap.transform { case (k, v) =>
      getGroupMap.get(k).foreach(v.entries ++= _.entries)
      v
    }
    this
  }

  private def getGroupMap: Map[String, EntryListInfo] = {
    if (_threadGroupMap == null) {
      _threadGroupMap = radixTree.getGroupMap
        .transform { case (group, entryList) =>
          if (threadGroupBWList.whiteList.exists(_.matcher(group).find))
            EntryListInfo(entryList, BlackWhiteListLevel.USEFUL)
          else if (threadGroupBWList.blackList.exists(_.matcher(group).find))
            EntryListInfo(entryList, BlackWhiteListLevel.USELESS)
          else
            EntryListInfo(entryList, BlackWhiteListLevel.UNDEFINED)
        }
    }
    _threadGroupMap
  }

  private[jstack] def toJstackFileInfo: JstackFileInfo = JstackFileInfo(fileName,
    timestamp,
    callStackMap.toMap,
    noCallStackArray.toArray,
    lockMap.toMap,
    methodMap.toMap,
    getGroupMap)
}

case class JstackFileInfo(fileName: String,
                          timestamp: String,
                          callStackMap: Map[String, EntryListInfo],
                          noCallStackArray: Array[JstackEntry],
                          lockMap: Map[String, (ArrayBuffer[JstackEntry], ArrayBuffer[JstackEntry])],
                          methodMap: Map[String, ArrayBuffer[JstackEntry]],
                          groupMap: Map[String, EntryListInfo])

case class EntryListInfo(entries: ArrayBuffer[JstackEntry], level: BlackWhiteListLevel, keywords: Array[String] = Array())

object BlackWhiteListLevel extends Enumeration {
  type BlackWhiteListLevel = Value
  val USELESS = Value(0)
  val UNDEFINED = Value(1)
  val USEFUL = Value(2)
}
