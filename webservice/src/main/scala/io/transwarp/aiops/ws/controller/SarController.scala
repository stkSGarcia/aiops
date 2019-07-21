package io.transwarp.aiops.ws.controller

import io.swagger.annotations.{Api, ApiOperation, ApiParam}
import io.transwarp.aiops.ws.controller.response.{RestResponse, SarReportRes}
import io.transwarp.aiops.ws.service.SarService
import javax.annotation.Resource
import org.springframework.web.bind.annotation._
import org.springframework.web.multipart.MultipartFile

/**
  * Created by hippo on 9/3/18.
  */
@Api(value = "sar", description = "sar文件分析相关操作")
@RestController
@RequestMapping(path = Array(PathV1.SAR))
class SarController {
  @Resource
  private var sarService: SarService = _

  @ApiOperation(value = "Sar分析", notes = "分析Sar文件")
  @RequestMapping(method = Array(RequestMethod.POST))
  def sar(@ApiParam(value = "sar文件", required = true)
          @RequestParam("file") file: MultipartFile): RestResponse[SarReportRes] = {
    sarService.serve(file.getOriginalFilename, file.getInputStream)
  }
}
