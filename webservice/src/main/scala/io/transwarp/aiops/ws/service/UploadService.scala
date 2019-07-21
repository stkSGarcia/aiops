//package io.transwarp.aiops.ws.spring.controller
//
//import java.io.File
//import java.util.concurrent.TimeUnit
//
//import io.transwarp.aiops.log.conf.Component.Component
//import io.transwarp.aiops.log.conf.ComponentMap
//import io.transwarp.aiops.log.loader.InitLogLoader
//import io.transwarp.aiops.log.message.{EmptyLogEntity, LogEntity}
//import io.transwarp.aiops.log.parser.{LogParser, UniversalParser}
//import io.transwarp.aiops.ws.feature.log.Unzip
//import org.joda.time.DateTime
//import org.springframework.scheduling.annotation.Async
//import org.springframework.stereotype.Service
//
//import scala.collection.mutable.ArrayBuffer
//
//@Service
//class UploadService {
//  @Async
//  def unzipAndAnalyse(files: Array[File], sessionID: String): Unit = {
//    val sessionGLock = CacheMap.logGetGlobal(sessionID)
//    // unzip and analyse
//    println("get req for comps " + Thread.currentThread.getName + " time: " + new DateTime)
//
//    if (files == null) throw new RuntimeException("No file has been accepted for analysis.")
//
//    sessionGLock.synchronized {
//      CacheMap.logPutGlobal(sessionID, GlobalCacheType.ProcessState, ProcessState.UNZIP.toString)
//      CacheMap.logPutGlobal(sessionID, GlobalCacheType.UnzipRateCache, 0F)
//    }
//
//    val targetBuffer = new ArrayBuffer[File]
//
//    files.zipWithIndex.foreach {
//      case (file, index) =>
//        Unzip.normalizeFile(sessionID, index, files.length, file, targetBuffer)
//    }
//
//    sessionGLock.synchronized {
//      CacheMap.logPutGlobal(sessionID, GlobalCacheType.ProcessState, ProcessState.ANALYSE.toString)
//      CacheMap.logPutGlobal(sessionID, GlobalCacheType.AnalyseRateCache, 0F)
//      val compArray: Array[String] = targetBuffer.map { file => ComponentMap.identify(file.getName).toString.toLowerCase }.toSet.toArray
//      CacheMap.logPutGlobal(sessionID, GlobalCacheType.CompsCache, compArray)
//    }
//
//    val startTime = System.currentTimeMillis
//
//    val logParser = new LogParser(targetBuffer.toArray)
//    logParser.parsers.foreach { case (comp, parser) =>
//      comp match {
//        case _ => universalAnalyze(parser.asInstanceOf[UniversalParser], logParser.components, sessionID, sessionGLock)
//      }
//    }
//
//    val duration = System.currentTimeMillis - startTime
//    val timeString = "%d min, %d sec".format(TimeUnit.MILLISECONDS.toMinutes(duration),
//      TimeUnit.MILLISECONDS.toSeconds(duration) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(duration)))
//    println(s"init service time (analyse) $timeString")
//    println(s"----- session id ------ : $sessionID")
//
//    CacheMap.logPutGlobal(sessionID, GlobalCacheType.ProcessState, ProcessState.OK.toString)
//
//    targetBuffer.foreach(file => file.delete)
//    println(Thread.currentThread().getName + " request for init backend completed")
//  }
//
//  private def universalAnalyze(parser: UniversalParser, comps: Set[Component], sessionID: String, sessionGLock: SessionGlobalCache): Unit = {
//    parser.work
//    val loader = new InitLogLoader(components = comps)
//    var curEntity: LogEntity = null
//
//    while (parser.hasNext) {
//      curEntity = parser.next
//
//      sessionGLock.synchronized {
//        CacheMap.logPutGlobal(sessionID, GlobalCacheType.AnalyseRateCache, parser.progress.toFloat)
//      }
//
//      loader.postMsg(curEntity)
//    }
//
//    val endEntity = new EmptyLogEntity
//    loader.postMsg(endEntity)
//
//    CacheMap.logPut(sessionID, loader.genCache)
//  }
//}
