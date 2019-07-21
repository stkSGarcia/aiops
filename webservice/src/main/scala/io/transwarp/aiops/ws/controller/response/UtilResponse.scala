package io.transwarp.aiops.ws.controller.response

import io.swagger.annotations.{ApiModel, ApiModelProperty}

@ApiModel(description = "服务器时间")
case class TimeRes(@ApiModelProperty("服务器时间戳") timestamp: Long) extends ResData
