package io.transwarp.aiops.ws.controller.converter

import io.transwarp.aiops.jstack.conf.BlackWhiteList
import io.transwarp.aiops.jstack.message._
import io.transwarp.aiops.ws.controller.response._

object JstackConverter {

  implicit class JstackResponse2Res(source: JstackResponse) {
    def convert: RestResponse[JstackRes] = {
      // FIXME need to determine response status
      RestResponse(ResultHeadSet.success,
        JstackRes(source.components,
          if (source.fileInfo == null) null else source.fileInfo.map(_.convert),
          if (source.totalInfo == null) null else source.totalInfo.convert))
    }
  }

  implicit class AnalyzeResponse2Res(source: AnalyzeResponse) {
    def convert: RestResponse[AnalyzeRes] = {
      // FIXME need to determine response status
      RestResponse(ResultHeadSet.success,
        AnalyzeRes(source.components,
          if (source.fileInfo == null) null else source.fileInfo.convert,
          source.allHistory))
    }
  }

  implicit class JstackFileInfo2Res(source: JstackFileInfo) {
    def convert: JstackFile = JstackFile(source.fileName,
      source.timeStamp,
      source.callStackMap,
      source.noCallStackArray,
      source.lockMap,
      if (source.methodList == null) null else source.methodList.map(_.convert),
      source.groupMap)

  }

  implicit class JstackMethodInfo2Res(source: MethodInfo) {
    def convert: JstackMethod = JstackMethod(source.method, source.entryList)
  }

  implicit class JstackGroupInfo2Res(source: GroupInfo) {
    def convert: JstackGroup = JstackGroup(source.group, source.entryListWithLevel)
  }

  implicit class BlackWhiteResponse2Res(source: BlackWhiteResponse) {
    def convert: RestResponse[BlackWhiteRes] = {
      RestResponse(ResultHeadSet.success,
        BlackWhiteRes(source.blackWhiteList.convert))
    }
  }

  implicit class BlackWhiteList2Res(source: BlackWhiteList) {
    def convert: BlackWhite = BlackWhite(source.whiteList, source.blackList)
  }


}
