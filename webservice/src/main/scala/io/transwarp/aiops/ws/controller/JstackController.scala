package io.transwarp.aiops.ws.controller

import java.io.{File, FileOutputStream}
import java.util.UUID
import javax.annotation.Resource

import io.swagger.annotations.{Api, ApiOperation, ApiParam}
import io.transwarp.aiops.Utils
import io.transwarp.aiops.ws.controller.response.{AnalyzeRes, BlackWhiteRes, JstackRes, RestResponse}
import io.transwarp.aiops.ws.service.JstackService
import org.apache.commons.io.IOUtils
import org.springframework.web.bind.annotation._
import org.springframework.web.multipart.MultipartFile

/**
  * Created by hippo on 9/3/18.
  */
@Api(value = "jstack", description = "Jstack相关操作")
@RestController
@RequestMapping(path = Array(PathV1.JSTACK))
class JstackController {
  @Resource
  private var jstackService: JstackService = _

  @ApiOperation(value = "统计", notes = "生成统计信息")
  @RequestMapping(method = Array(RequestMethod.POST))
  def summary(@ApiParam(value = "jstack文件", required = true)
              @RequestParam("files") files: Array[MultipartFile]): RestResponse[JstackRes] = {

    val cpFileDir = new File(Utils.TempDir, UUID.randomUUID.toString)
    if (cpFileDir.exists) cpFileDir.delete
    cpFileDir.mkdir

    val cpFiles = files.map(mpFile => {
      val cpFile = new File(cpFileDir, mpFile.getOriginalFilename)
      val cpOS = new FileOutputStream(cpFile)
      IOUtils.copy(mpFile.getInputStream, cpOS)
      cpOS.close()
      cpFile
    })
    jstackService.serve(cpFiles)
  }

  @ApiOperation(value = "分析", notes = "分析单个文件")
  @RequestMapping(value = Array("/analysis"), method = Array(RequestMethod.GET))
  def analyze(@ApiParam(value = "jstack文件名", required = true)
              @RequestParam filename: String): RestResponse[AnalyzeRes] = {
    jstackService.analyze(filename)
  }

  @ApiOperation(value = "显示", notes = "显示 callStack 黑白名单")
  @RequestMapping(value = Array("/callstack_black_white"), method = Array(RequestMethod.GET))
  def showCallstackBlackWhiteList(@ApiParam(value = "组件", required = true)
                                  @RequestParam component: String): RestResponse[BlackWhiteRes] = {
    jstackService.showCallstackList(component)
  }

  @ApiOperation(value = "显示", notes = "显示 threadGroup黑白名单")
  @RequestMapping(value = Array("/threadGroup_black_white"), method = Array(RequestMethod.GET))
  def showThreadGroupBlackWhiteList: RestResponse[BlackWhiteRes] = jstackService.showThreadGroupList()

  //  @ApiOperation(value = "获取文件", notes = "接收 executor 的 IP 地址获取并保存 jstack 文件")
  //  @RequestMapping(value = Array("/file_storage"), method = Array(RequestMethod.POST))
  //  def fileSave(@ApiParam(value = "触发消息", required = true)
  //               @RequestBody string: String): RestResponse.NullType = {
  //    // 调用之神的接口，获取 jstack文件
  //
  //    val jstack: String = ""
  //
  //    jstackService.saveFile(jstack: String) {
  //      // 把jstack 写到文件中去，动态存到具体路径下
  //    }
  //  }

}
