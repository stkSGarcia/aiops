package io.transwarp.aiops.ws.cache

import io.transwarp.aiops.jstack.message.JstackFileInfo

import scala.reflect.runtime.universe._

//case class CacheManager(cache: HttpSession) {
//  def setCache(cacheMap: HashMap[String, AnyRef]) = {
//    cacheMap.foreach(item => {
//      cache.setAttribute(item._1, item._2)
//    })
//  }
//
//  def setCache[A](cacheType: CacheType[A], value: A) = {
//    cache.setAttribute(cacheType.name, value)
//  }
//
//  def clearCache = {
//    val attrs = cache.getAttributeNames
//    while (attrs.hasMoreElements) {
//      val attrName = attrs.nextElement
//      cache.removeAttribute(attrName)
//    }
//  }
//
//  def getCache[A](cacheType: CacheType[A]): Option[A] = {
//    val attr = cache.getAttribute(cacheType.name)
//    if (attr == null) {
//      None
//    } else {
//      Some(attr.asInstanceOf[A])
//    }
//  }
//
//  def getAndCheckCache[A](cacheType: CacheType[A]): A = {
//    val res = cache.getAttribute(cacheType.name)
//    if (res == null) {
//      throw new RuntimeException(s"current session doesn't hold ${cacheType.name} cache")
//    }
//    res.asInstanceOf[A]
//  }
//}

object ProcessState extends Enumeration {
  type ProcessState = Value
  val OK = Value("ok")
  val WAIT = Value("wait")
  val UNZIP = Value("unzip")
  val ANALYSE = Value("analyse")
}

case class GCacheType[A](val name: String, val typeTag: TypeTag[A])

object GlobalCacheType {

  // global cache type for log service
  val ProcessState = GCacheType("ProcessState", typeTag[String])

  val UnzipRateCache = GCacheType("UnzipRate", typeTag[Float])

  val AnalyseRateCache = GCacheType("AnalyseRate", typeTag[Float])

  val CompsCache = GCacheType("Comps", typeTag[Array[String]])

  // global cache type for other service
  val FileInfoCache = GCacheType("FileInfo", typeTag[Array[JstackFileInfo]])
  val TotalInfoCache = GCacheType("TotalFileInfo", typeTag[JstackFileInfo])
//  val JstackResCache = GCacheType("JstackResCache", typeTag[JstackResponse])

}


//object CacheType {
//
//  val InceptorGoalMapCache = CacheType("InceptorGoalMap", typeTag[HashMap[String, LogGoalWithTask[InceptorGoalID]]])
//
//  val InceptorGoalBySessionCache = CacheType("InceptorGoalBySession", typeTag[HashMap[String, ArrayBuffer[LogGoalWithTask[InceptorGoalID]]]])
//
//  val InceptorGoalThinBeanMapCache = CacheType("InceptorGoalThinBeanMap", typeTag[HashMap[String, GoalTaskThinBean]])
//
//  val InceptorGoalBeanMapCache = CacheType("InceptorGoalBeanMap", typeTag[HashMap[String, GoalTaskBean]])
//
//  val InceptorGoalSortByStartCache = CacheType("InceptorGoalSortByStart", typeTag[Array[LogGoalWithTask[InceptorGoalID]]])
//
//  val InceptorGoalSortByEndCache = CacheType("InceptorGoalSortByEnd", typeTag[Array[LogGoalWithTask[InceptorGoalID]]])
//
//  val InceptorGoalSortByDurationCache = CacheType("InceptorGoalSortByDuration", typeTag[Array[LogGoalWithTask[InceptorGoalID]]])
//
//  val InceptorGoalByDateCache = CacheType("InceptorGoalByDate", typeTag[HashMap[LocalDate, (DateBean, HashMap[String, ArrayBuffer[LogGoalWithTask[InceptorGoalID]]])]])
//
//}

