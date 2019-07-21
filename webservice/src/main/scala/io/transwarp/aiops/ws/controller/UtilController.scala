package io.transwarp.aiops.ws.controller

import io.swagger.annotations.{Api, ApiOperation}
import io.transwarp.aiops.plain.conf.EsConf
import io.transwarp.aiops.sar.conf.SarConf
import io.transwarp.aiops.ws.controller.response._
import io.transwarp.aiops.{AIOpsConf, Logger}
import org.springframework.web.bind.annotation._

import scala.util.{Failure, Success, Try}

@Api(value = "util", description = "通用操作")
@RestController
@RequestMapping(path = Array(PathV1.UTIL))
class UtilController extends Logger {
  @ApiOperation(value = "时间查询", notes = "查询服务器当前时间")
  @RequestMapping(value = Array("/time"), method = Array(RequestMethod.GET))
  def timestamp: RestResponse[TimeRes] = {
    RestResponse(ResultHeadSet.success, TimeRes(System.currentTimeMillis))
  }

  @ApiOperation(value = "更新配置", notes = "[内部]更新配置")
  @RequestMapping(value = Array("/conf"), method = Array(RequestMethod.PUT))
  def reloadConfig: RestResponse.NullType = {
    Try {
      AIOpsConf.reload()
      EsConf.reload()
      SarConf.reload()
    } match {
      case Success(_) => RestResponse.NULL_SUCCESS
      case Failure(exception) =>
        LOG.error("Reload config failed!", exception)
        RestResponse.NULL_FAILURE
    }
  }
}
