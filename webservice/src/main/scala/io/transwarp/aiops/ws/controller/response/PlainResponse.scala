package io.transwarp.aiops.ws.controller.response

import io.swagger.annotations.{ApiModel, ApiModelProperty}
import io.transwarp.aiops.plain.model.PlainDoc

import scala.collection.mutable

/**
  * Created by hippo on 9/4/18.
  */
@ApiModel(description = "知识库查询结果")
case class PlainSearchRes(@ApiModelProperty("结果文档集合") docs: Array[PlainDoc]) extends ResData

@ApiModel(description = "知识库分页查询结果")
case class PlainSearchByPageRes(@ApiModelProperty("结果文档集合") docs: Array[PlainDoc],
                                @ApiModelProperty("页码") page: Int,
                                @ApiModelProperty("记录总数") total: Long,
                                @ApiModelProperty("每页大小") size: Int) extends ResData

@ApiModel(description = "知识库统计信息(仪表盘)查询结果")
class PlainStatisticsRes extends ResData {
  @ApiModelProperty("文档总数") var totalDocs: Long = _
  @ApiModelProperty("文档统计按组件") var compStats: mutable.HashMap[String, Long] = _
  @ApiModelProperty("文档统计按人员") var personStats: Array[PlainPersonState] = _
  @ApiModelProperty("总查询数目") var totalQueries: Long = _
  @ApiModelProperty("各时间段文档总数") var intervalQueries: Array[PlainIntervalState] = _
  @ApiModelProperty("TOP回答") var topAnswers: Array[PlainDoc] = _
  @ApiModelProperty("TOP查询") var topQueries: Array[String] = _
}

@ApiModel(description = "按人员统计结果")
case class PlainPersonState(@ApiModelProperty("人员名称") name: String,
                            @ApiModelProperty("文档数目") count: Long)

@ApiModel(description = "按时间段统计结果")
case class PlainIntervalState(@ApiModelProperty("时间戳") timestamp: Long,
                              @ApiModelProperty("文档数目") count: Long)

@Deprecated
@ApiModel(description = "知识库统计信息(临时)查询结果")
class PlainStatsRes extends ResData {
  // history statistics
  @ApiModelProperty("文档总数") var docAmount: Int = _
  @ApiModelProperty("按人员统计文档数目") var docPerPerson: Array[PlainStatsPerson] = _
  @ApiModelProperty("按组件统计文档数目") var docPerComp: Array[PlainStatsComp] = _
  // new day statistics
  @ApiModelProperty("当日新增文档数目") var docPerDay: Int = _
  @ApiModelProperty("当日新增文档按人员分类") var docPerPersonPerDay: Array[PlainStatsPerson] = _
  // es meta stat
  @ApiModelProperty("ES Shard数目") var shardNum: Int = _
}

@Deprecated
@ApiModel(description = "按人员统计子结构")
case class PlainStatsPerson(@ApiModelProperty("人员名称") person: String,
                            @ApiModelProperty("文档数目") count: Int)

@Deprecated
@ApiModel(description = "按组件统计子结构")
case class PlainStatsComp(@ApiModelProperty("组件名称") comp: String,
                          @ApiModelProperty("文档数目") count: Int)
