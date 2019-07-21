package io.transwarp.aiops.ws.spring.controller

import java.io.File
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.concurrent.TimeUnit

import io.transwarp.aiops.Component
import io.transwarp.aiops.log.conf.LogConf
import io.transwarp.aiops.log.loader.InitLogLoader
import io.transwarp.aiops.log.message.LogEntity
import io.transwarp.aiops.log.parser.{Level, LogParser}
import io.transwarp.aiops.ws.feature.log.Unzip
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Service

import scala.collection.mutable.ArrayBuffer

@Service
class UploadService {

  @Async
  def unzipAndAnalyse(files: Array[File], sessionID: String): Unit = {
    val sessionGLock = CacheMap.logGetGlobal(sessionID)
    // unzip and analyse
    val timeFormat = new SimpleDateFormat("HH:mm:ss")
    val now = Calendar.getInstance.getTime
    val timeString = timeFormat.format(now)
    println("get req for comps " + Thread.currentThread().getName + " time: " + timeString)

    if (files == null) {
      throw new RuntimeException("no file has been accepted for analyze")
    }

    sessionGLock.synchronized {
      CacheMap.logPutGlobal(sessionID, GlobalCacheType.ProcessState, ProcessState.UNZIP.toString)
      CacheMap.logPutGlobal(sessionID, GlobalCacheType.UnzipRateCache, 0f)
    }

    val targetBuffer = new ArrayBuffer[File]

    files.zipWithIndex.foreach {
      case (file, index) => {
        Unzip.normalizeFile(sessionID, index, files.size, file, targetBuffer)
      }
    }

    sessionGLock.synchronized {
      CacheMap.logPutGlobal(sessionID, GlobalCacheType.ProcessState, ProcessState.ANALYSE.toString)
      CacheMap.logPutGlobal(sessionID, GlobalCacheType.AnalyseRateCache, 0f)
      val compArray: Array[String] = targetBuffer.map { file => LogConf.identifyComponent(file.getName).toString.toLowerCase }.toSet.toArray
      CacheMap.logPutGlobal(sessionID, GlobalCacheType.CompsCache, compArray)
    }

    def analyse = {
      val startTime = System.currentTimeMillis()
      val files = targetBuffer.toArray

      val parser = new LogParser
      parser.setSource(files)
      val comps = parser.getComponents

      val loader = new InitLogLoader(components = comps)
      var curEntity: LogEntity = null

      while (parser.hasNext) {
        curEntity = parser.next

        sessionGLock.synchronized {
          CacheMap.logPutGlobal(sessionID, GlobalCacheType.AnalyseRateCache, parser.getProgress.toFloat)
        }

        loader.postMsg(curEntity)
      }

      val endEntity = LogEntity(component = Component.WILDCARD, level = Level.END)
      loader.postMsg(endEntity)

      CacheMap.logPut(sessionID, loader.genCache)

      val duration = System.currentTimeMillis - startTime
      val timeString = "%d min, %d sec".format(TimeUnit.MILLISECONDS.toMinutes(duration),
        TimeUnit.MILLISECONDS.toSeconds(duration) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(duration)))
      println(s"init service time (analyse) ${timeString}")
      println(s"----- session id ------ : ${sessionID}")
    }

    analyse

    CacheMap.logPutGlobal(sessionID, GlobalCacheType.ProcessState, ProcessState.OK.toString)

    targetBuffer.foreach(file => file.delete)
    println(Thread.currentThread().getName + " request for init backend completed")
  }
}
