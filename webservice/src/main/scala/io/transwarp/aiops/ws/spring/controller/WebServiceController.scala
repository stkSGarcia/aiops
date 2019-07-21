package io.transwarp.aiops.ws.spring.controller

import java.io.{File, FileOutputStream}
import java.util.UUID
import java.util.concurrent.TimeUnit

import com.fasterxml.jackson.databind.ObjectMapper
import io.transwarp.aiops.log.message._
import io.transwarp.aiops.ws.feature.log.InceptorLogServices
import io.transwarp.aiops.ws.spring.controller.ProcessState.ProcessState
import io.transwarp.aiops.ws.spring.statebeans._
import io.transwarp.aiops.{Component, Logger, Utils}
import javax.annotation.Resource
import javax.servlet.http.HttpSession
import org.apache.commons.io.IOUtils
import org.springframework.context.annotation.Scope
import org.springframework.scheduling.annotation.EnableAsync
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.{RequestMapping, _}
import org.springframework.web.multipart.MultipartFile


@Controller
@Scope("session")
class WebServiceController extends Logger {
  @Resource
  private var logCache: HttpSession = _

  @Resource
  private var uploadService: UploadService = _

  @RequestMapping(path = Array("/api/log/upload"), method = Array(RequestMethod.POST))
  @ResponseBody
  @EnableAsync
  def logUpload(@RequestParam("files[]") files: Array[MultipartFile]): String = {
    val sessionID = logCache.getId
    CacheMap.clearLogCache
    CacheMap.logPutGlobal(sessionID, GlobalCacheType.ProcessState, ProcessState.WAIT.toString)

    val cpFileDir = new File(Utils.TempDir, UUID.randomUUID.toString)
    if (cpFileDir.exists) {
      cpFileDir.delete
      cpFileDir.mkdir
    } else {
      cpFileDir.mkdir
    }

    def copyFile: Array[File] = {
      files.map(mpFile => {
        val cpFile = new File(cpFileDir, mpFile.getOriginalFilename)
        val cpOS = new FileOutputStream(cpFile)
        IOUtils.copy(mpFile.getInputStream, cpOS)
        cpOS.close()
        cpFile
      })
    }
    val cpFiles = copyFile
    uploadService.unzipAndAnalyse(cpFiles, sessionID)
    "ok"
  }

  @RequestMapping(path = Array("/api/log/init"), method = Array(RequestMethod.POST))
  @ResponseBody
  def logInit: LogResponse = {
    val sessionID = logCache.getId
    val sessionGLock = CacheMap.logGetGlobal(sessionID)

    def getProgressState: ProcessState = {
      val stateString = sessionGLock.synchronized {
        CacheMap.logGetAndCheckGlobal(sessionID, GlobalCacheType.ProcessState)
      }
      ProcessState.withName(stateString)
    }

    val state = getProgressState
    val response = state match {
      case ProcessState.WAIT => {
        LogInitResponse(state.toString, 0, null)
      }
      case ProcessState.UNZIP => {
        val rate = sessionGLock.synchronized {
          CacheMap.logGetAndCheckGlobal(sessionID, GlobalCacheType.UnzipRateCache)
        }
        LogInitResponse(state.toString, rate, null)
      }
      case ProcessState.ANALYSE => {
        val rate = sessionGLock.synchronized {
          CacheMap.logGetAndCheckGlobal(sessionID, GlobalCacheType.AnalyseRateCache)
        }
        LogInitResponse(state.toString, rate, null)
      }
      case ProcessState.OK => {
        val comps = CacheMap.logGetAndCheckGlobal(sessionID, GlobalCacheType.CompsCache)
        LogInitResponse(state.toString, 0, comps)
      }
    }
    response
  }


  /**
    * @param component
    * @param service
    * @param reqBody
    * @return
    */
  @RequestMapping(path = Array("/api/log/{component}/{service}"), method = Array(RequestMethod.POST))
  @ResponseBody
  def logService(@PathVariable component: String, @PathVariable service: String,
                 @RequestBody(required = false) reqBody: String): LogResponse = {
    implicit val sessionID: String = logCache.getId
    val comp = Component.withCaseIgnoreName(component)
    val mapper = new ObjectMapper
    val startTime = System.currentTimeMillis
    val serviceRes = comp match {
      case Component.INCEPTOR => {
        service match {
          case "date" => InceptorLogServices.date
          case "session" => InceptorLogServices.session(mapper.readValue(reqBody, classOf[InceptorSessionBeans]))
          case "timeline" => InceptorLogServices.timeLine(mapper.readValue(reqBody, classOf[InceptorTimelineBeans]))
          case "flatgoals" => InceptorLogServices.flatGoals(sessionID, mapper.readValue(reqBody, classOf[InceptorFlatGoalsBeans]))
          case "goal" => InceptorLogServices.getGoalDetail(sessionID, mapper.readValue(reqBody, classOf[InceptorLogGoalStateBeans]))
          case "goaltimeline" => InceptorLogServices.getGoalTimeLine(sessionID, mapper.readValue(reqBody, classOf[InceptorLogGoalTimeLineStateBeans]))
        }
      }
      case _ => null
    }
    val duration = System.currentTimeMillis - startTime
    val timeString = "%d min, %d sec".format(TimeUnit.MILLISECONDS.toMinutes(duration),
      TimeUnit.MILLISECONDS.toSeconds(duration) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(duration)))
    println(s"component ${comp} service ${service}, take time ${timeString}")
    serviceRes
  }

}
