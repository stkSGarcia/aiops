//package io.transwarp.aiops.ws.spring.controller
//
//import io.transwarp.aiops.ws.cache.GCacheType
//
//import scala.collection.mutable
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
//
//  private val jstackGlobalCache = new mutable.HashMap[SessionID, SessionGlobalCache]
//
//  def clearJstackCache = jstackGlobalCache.clear
//
//
//  def clearSessionCache(id: SessionID) = {
//     jstackGlobalCache.remove(id)
//  }
//
//  // 存什么，类型和值
//  def jstackPutGlobal[A](id: SessionID, cacheType: GCacheType[A], cacheValue: A): Option[A] = {
//    jstackGlobalCache.get(id).fold({
//      val sgc = new SessionGlobalCache
//      sgc.put(cacheType, cacheValue)
//      jstackGlobalCache.put(id, sgc)
//      Option(cacheValue)
//    })(_.put(cacheType, cacheValue))
//  }
//
//  def jstackGetGlobal[A](id: SessionID, cacheType: GCacheType[A]): Option[A] = {
//   jstackGlobalCache.get(id).fold(Option.empty[A])(_.get(cacheType))
//  }
//
//  def jstackGetAndCheckGlobal[A](id: SessionID, cacheType: GCacheType[A]): A = {
//    jstackGetGlobal(id, cacheType) match {
//      case Some(item) => item
//      case None => throw new RuntimeException(s"Can't find global cache for session ${id} with name ${cacheType.name}")
//    }
//  }
//
//  def jstackGetGlobal(id: SessionID): SessionGlobalCache = {
//    jstackGlobalCache.get(id).get
//  }
//
//}
