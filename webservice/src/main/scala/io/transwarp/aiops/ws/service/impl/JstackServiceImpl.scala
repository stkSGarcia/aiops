package io.transwarp.aiops.ws.service.impl

import java.io.File

import io.transwarp.aiops.jstack.analyzer.{BlackWhiteListHandler, JstackAccum, JstackAnalyze}
import io.transwarp.aiops.ws.controller.converter.JstackConverter._
import io.transwarp.aiops.ws.controller.response.{AnalyzeRes, BlackWhiteRes, JstackRes, RestResponse}
import io.transwarp.aiops.ws.service.JstackService
import org.springframework.stereotype.Service

@Service
class JstackServiceImpl extends JstackService {

  override def serve(files: Array[File]): RestResponse[JstackRes] = {
    new JstackAccum(files).accum.convert
  }

  override def analyze(fileName: String): RestResponse[AnalyzeRes] = {
    new JstackAnalyze(fileName).accum.convert
  }

  override def showCallstackList(component: String): RestResponse[BlackWhiteRes] = {
    BlackWhiteListHandler.getInstance.showCallstackBlackWhiteList(component).convert
  }

  override def showThreadGroupList: RestResponse[BlackWhiteRes] = {
    BlackWhiteListHandler.getInstance.showThreadGroupBlackWhiteList.convert
  }
}
