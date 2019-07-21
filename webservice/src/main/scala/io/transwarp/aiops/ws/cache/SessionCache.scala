//package io.transwarp.aiops.ws.spring.controller
//
//import io.transwarp.aiops.Component.Component
//import io.transwarp.aiops.log.loader.{LogCache, LogCacheType}
//
//import scala.collection.mutable
//import org.joda.time.LocalDate
//
//
////private[controller] class DayCache {
////  type CacheTypeName = String
////  private val map = new mutable.HashMap[CacheTypeName, Any]
////
////  def contains[A](cacheType: CacheType[A]): Boolean = {
////    map.contains(cacheType.name)
////  }
////
////  def put[A](cacheType: CacheType[A], cacheValue: A): Option[A] = {
////    map.put(cacheType.name, cacheValue).fold(Option.empty[A])(_ => Some(cacheValue))
////  }
////
////  def get[A](cacheType: CacheType[A]): Option[A] = {
////    map.get(cacheType.name).fold(Option.empty[A])(x => Some(x.asInstanceOf[A]))
////  }
////}
////
////private[controller] class SessionCache {
////  type Date = LocalDate
////  private val map = new mutable.HashMap[Date, DayCache]
////
////  def contains[A](date: Date, cacheType: CacheType[A]): Boolean = {
////    val res = if (map.contains(date)) {
////      val dayCache = map.get(date).get
////      dayCache.contains(cacheType)
////    } else false
////    res
////  }
////
////  def put[A](date: Date, cacheType: CacheType[A], cacheValue: A): Option[A] = {
////    map.get(date).fold({
////      val dayCache = new DayCache
////      dayCache.put(cacheType, cacheValue)
////      map.put(date, dayCache)
////      Option(cacheValue)
////    })(_.put(cacheType, cacheValue))
////  }
////
////  def get[A](date: Date, cacheType: CacheType[A]): Option[A] = {
////    map.get(date).fold(Option.empty[A])(_.get(cacheType))
////  }
////}
//
//private[controller] class SessionGlobalCache {
//  type CacheTypeName = String
//  private val map = new mutable.HashMap[CacheTypeName, Any]
//
//  def contains[A](cacheType: GCacheType[A]): Boolean = {
//    map.contains(cacheType.name)
//  }
//
//  def put[A](cacheType: GCacheType[A], cacheValue: A): Option[A] = {
//    map.put(cacheType.name, cacheValue).fold(Option.empty[A])(_ => Some(cacheValue))
//  }
//
//  def get[A](cacheType: GCacheType[A]): Option[A] = {
//    map.get(cacheType.name).fold(Option.empty[A])(x => Some(x.asInstanceOf[A]))
//  }
//}
//
//object CacheMap {
//  type SessionID = String
//  type Date = LocalDate
//  type LogCompCacheMap = mutable.HashMap[Component, LogCache]
//
//  private val logGlobalCache = new mutable.HashMap[SessionID, SessionGlobalCache]
//  private val logDateCache = new mutable.HashMap[SessionID, LogCompCacheMap]
//
//  def clearLogCache = {
//    logGlobalCache.clear
//    logDateCache.clear
//  }
//
//  def clearSessionCache(id: SessionID) = {
//     logGlobalCache.remove(id)
//     logDateCache.remove(id)
//  }
//
//  def logPutGlobal[A](id: SessionID, cacheType: GCacheType[A], cacheValue: A): Option[A] = {
//    logGlobalCache.get(id).fold({
//      val sgc = new SessionGlobalCache
//      sgc.put(cacheType, cacheValue)
//      logGlobalCache.put(id, sgc)
//      Option(cacheValue)
//    })(_.put(cacheType, cacheValue))
//  }
//
//  def logPut(id: SessionID, logCompCacheMap: LogCompCacheMap): Option[LogCompCacheMap] = {
//    logDateCache.put(id, logCompCacheMap)
//  }
//
//  def logPut[A](id: SessionID, comp: Component, date: Date, cacheType: LogCacheType[A], cacheValue: A): Option[A] = {
//    logDateCache.get(id).fold {
//      val logCompCacheMap = new mutable.HashMap[Component, LogCache]
//      val logCache = new LogCache
//      val res = logCache.put(date, cacheType, cacheValue)
//      logCompCacheMap += (comp -> logCache)
//      res
//    }{
//      logCompCacheMap => {
//         logCompCacheMap.get(comp).fold {
//           val logCache = new LogCache
//           val res = logCache.put(date, cacheType, cacheValue)
//           logCompCacheMap += (comp -> logCache)
//           res
//         }{
//           logCache => {
//             logCache.put(date, cacheType, cacheValue)
//           }
//         }
//      }
//    }
//  }
//
//
////  def put[A](id: SessionID, date: Date, cacheType: CacheType[A], cacheValue: A): Option[A] = {
////    logDateCache.get(id).fold({
////      val sessionCache = new LogCache
////      sessionCache.put(date, cacheType, cacheValue)
////      map.put(id, sessionCache)
////      Option(cacheValue)
////    })(_.put(date, cacheType, cacheValue))
////
////  }
//
//  def logGetGlobal[A](id: SessionID, cacheType: GCacheType[A]): Option[A] = {
//   logGlobalCache.get(id).fold(Option.empty[A])(_.get(cacheType))
//  }
//
//  def logGetAndCheckGlobal[A](id: SessionID, cacheType: GCacheType[A]): A = {
//    logGetGlobal(id, cacheType) match {
//      case Some(item) => item
//      case None => throw new RuntimeException(s"Can't find global cache for session ${id} with name ${cacheType.name}")
//    }
//  }
//
//
//  def logGetGlobal(id: SessionID): SessionGlobalCache = {
//    logGlobalCache.get(id).get
//  }
//
//  def getLogCache(id: SessionID, comp: Component): Option[LogCache] = logDateCache.get(id).flatMap(_.get(comp))
//
//  def logGet[A](id: SessionID, comp:Component, date: Date, cacheType: LogCacheType[A]): Option[A] = {
//    logDateCache.get(id) match {
//      case Some(compMap) => {
//        compMap.get(comp) match {
//          case Some(logCache) => {
//             logCache.get(date, cacheType)
//          }
//          case None => Option.empty[A]
//        }
//      }
//      case None => Option.empty[A]
//    }
//  }
//
//  def logGetAndCheck[A](id: SessionID, comp: Component, date: Date, cacheType: LogCacheType[A]): A = {
//    val item = logGet(id, comp, date, cacheType)
//    item match {
//      case Some(x) => {
//        x
//      }
//      case None => {
//        throw new RuntimeException(s"Can't find cache with session ID ${id} and date ${date.toString} and cacheType ${cacheType.name}")
//      }
//    }
//  }
//
//}
