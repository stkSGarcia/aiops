package io.transwarp.aiops.ws.controller.response

import io.swagger.annotations.{ApiModel, ApiModelProperty}

/**
  * Created by hippo on 9/4/18.
  */
trait ResData

@ApiModel("Restful API 响应数据结构")
case class RestResponse[T <: ResData](@ApiModelProperty(value = "响应结果状态", required = true)
                                      var head: ResultHead = ResultHeadSet.success,
                                      @ApiModelProperty("响应结果数据")
                                      var data: T)

object RestResponse {
  type NullType = RestResponse[Null]
  val NULL_SUCCESS = new NullType(ResultHeadSet.success, null)
  val NULL_FAILURE = new NullType(ResultHeadSet.failure, null)
}

@ApiModel("Restful API 响应头部")
case class ResultHead(@ApiModelProperty(value = "状态码", required = true)
                      var resultCode: String,
                      @ApiModelProperty("响应消息")
                      var message: String)

object ResultHeadSet {
  val success = ResultHead("111", "SUCCESS")
  val failure = ResultHead("000", "FAILURE")
}
