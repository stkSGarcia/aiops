package io.transwarp.aiops.ws.controller.response

import io.swagger.annotations.{ApiModel, ApiModelProperty}

/**
  * Created by hippo on 9/3/18.
  */
@ApiModel(description = "Sar分析结果")
case class SarReportRes(@ApiModelProperty("Sar分析结果集合") results: Array[SarResult]) extends ResData

@ApiModel(description = "Sar结果")
class SarResult {
  @ApiModelProperty("errorMsg") var errorMsg: String = ""
  @ApiModelProperty("fileName") var fileName: String = _

  // Conf
  @ApiModelProperty("titleDesc") var titleDesc: Array[String] = _
  @ApiModelProperty("titleName") var titleName: Array[String] = _
  @ApiModelProperty("titleEnabled") var titleEnabled: Array[Boolean] = _
  @ApiModelProperty("warnLevel") var warnLevel: Array[Int] = _

  // Data
  @ApiModelProperty("totalRecordsNumber") var totalRecordsNumber: Int = _
  @ApiModelProperty("details") var details: Array[String] = _
  @ApiModelProperty("wasError") var wasError: Array[Array[Boolean]] = _
  @ApiModelProperty("errorPercent") var errorPercent: Array[String] = _
  @ApiModelProperty("errorNum") var errorNum: Array[Int] = _
}
