package io.transwarp.aiops.log.loader

import org.joda.time.LocalDate
import scala.collection.mutable



case class InceptorFilterState(startTime: Long = -1, endTime: Long = -1, smartType: Int = 0, goalType: Int = -1,
                                   minDuration: Long = -1, maxDuration: Long = -1, sortBy: Int = 0, order: Boolean = true)

class LogCache {
  type Date = LocalDate
  private val map = new mutable.HashMap[Date, DayCache]

  def keySet = map.keySet

  def contains[A](date: Date, cacheType: LogCacheType[A]): Boolean = {
    val res = if (map.contains(date)) {
      val dayCache = map.get(date).get
      dayCache.contains(cacheType)
    } else false
    res
  }

  def put[A](date: Date, cacheType: LogCacheType[A], cacheValue: A): Option[A] = {
    map.get(date).fold({
      val dayCache = new DayCache
      dayCache.put(cacheType, cacheValue)
      map.put(date, dayCache)
      Option(cacheValue)
    })(_.put(cacheType, cacheValue))
  }

  def get[A](date: Date, cacheType: LogCacheType[A]): Option[A] = {
    map.get(date).fold(Option.empty[A])(_.get(cacheType))
  }

}


private[log] class DayCache {
  type CacheTypeName = String
  private val map = new mutable.HashMap[CacheTypeName, Any]

  def contains[A](cacheType: LogCacheType[A]): Boolean = {
    map.contains(cacheType.name)
  }

  def put[A](cacheType: LogCacheType[A], cacheValue: A): Option[A] = {
    map.put(cacheType.name, cacheValue).fold(Option.empty[A])(_ => Some(cacheValue))
  }

  def get[A](cacheType: LogCacheType[A]): Option[A] = {
    map.get(cacheType.name).fold(Option.empty[A])(x => Some(x.asInstanceOf[A]))
  }
}


