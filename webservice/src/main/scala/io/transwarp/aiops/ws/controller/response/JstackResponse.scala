package io.transwarp.aiops.ws.controller.response

import java.util

import io.swagger.annotations.{ApiModel, ApiModelProperty}
import io.transwarp.aiops.jstack.analyzer.JstackEntry
import io.transwarp.aiops.jstack.message.EntryListWithLevel
import io.transwarp.aiops.ws.controller.response.JstackType._

import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer

@ApiModel(description = "Jstack分析结果(上传)")
case class JstackRes(@ApiModelProperty("组件") components: Array[String],
                     @ApiModelProperty("文件结果集合") fileInfo: Array[JstackFile],
                     @ApiModelProperty("总体统计结果") totalInfo: JstackFile) extends ResData

@ApiModel(description = "Jstack分析结果(对接)")
case class AnalyzeRes(@ApiModelProperty("组件") components: Array[String],
                      @ApiModelProperty("文件结果") fileInfo: JstackFile,
                      @ApiModelProperty("历史分析记录") allHistory: Array[String]) extends ResData

@ApiModel(description = "单个Jstack文件分析结果")
case class JstackFile(@ApiModelProperty("文件名") fileName: String,
                      @ApiModelProperty("时间戳") timeStamp: String,
                      @ApiModelProperty("相同CallStack的线程Map") callStackMap: mutable.HashMap[CallStack, EntryListWithLevel],
                      @ApiModelProperty("无CallStack的线程List") noCallStackArray: EntryList,
                      @ApiModelProperty("锁Map") lockMap: mutable.HashMap[LockAddress, (LockList, WaitLockList)],
                      @ApiModelProperty("热点方法List") methodList: ArrayBuffer[JstackMethod],
                      @ApiModelProperty("线程族Map") groupMap: mutable.HashMap[GroupName, EntryListWithLevel])

//@ApiModel(description = "单个Jstack文件分析结果")
//case class JstackFile(@ApiModelProperty("文件名") fileName: String,
//                      @ApiModelProperty("时间戳") timeStamp: String,
//                      @ApiModelProperty("相同CallStack的线程Map") callStackMap: mutable.HashMap[CallStack, EntryListWithLevel],
//                      @ApiModelProperty("无CallStack的线程List") noCallStackArray: EntryList,
//                      @ApiModelProperty("锁Map") lockMap: mutable.HashMap[LockAddress, (LockList, WaitLockList)],
//                      @ApiModelProperty("热点方法List") methodList: ArrayBuffer[JstackMethod],
//                      @ApiModelProperty("线程族Map") groupMap: mutable.HashMap[GroupName, EntryListWithLevel],
//                      @ApiModelProperty("历史文件List") history: ArrayBuffer[String])

@ApiModel(description = "热点方法信息")
case class JstackMethod(@ApiModelProperty("方法名") method: String,
                        @ApiModelProperty("线程List") entryList: EntryList)

@ApiModel(description = "线程族信息")
case class JstackGroup(@ApiModelProperty("线程族名") group: String,
                       @ApiModelProperty("线程List") entryListWithLevel: EntryListWithLevel)

@ApiModel(description = "类型说明")
object JstackType {
  @ApiModelProperty("持有锁List") type LockList = ArrayBuffer[JstackEntry]
  @ApiModelProperty("等待锁List") type WaitLockList = LockList
  @ApiModelProperty("锁地址") type LockAddress = String
  @ApiModelProperty("CallStack") type CallStack = String
  @ApiModelProperty("CallStack") type GroupName = String
  @ApiModelProperty("线程List") type EntryList = ArrayBuffer[JstackEntry]
}

@ApiModel(description = "Callstack 黑白名单")
case class BlackWhiteRes(@ApiModelProperty("黑白名单") blackWhiteList: BlackWhite) extends ResData

@ApiModel(description = "黑白名单")
case class BlackWhite(@ApiModelProperty("白名单") whiteList: util.List[String],
                      @ApiModelProperty("黑名单") blackList: util.List[String])

