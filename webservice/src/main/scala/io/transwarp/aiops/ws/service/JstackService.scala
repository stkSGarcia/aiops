package io.transwarp.aiops.ws.service

import java.io.File

import io.transwarp.aiops.ws.controller.response.{AnalyzeRes, BlackWhiteRes, JstackRes, RestResponse}

/**
  * Created by hippo on 9/3/18.
  */
trait JstackService {

  def serve(files: Array[File]): RestResponse[JstackRes]

  def analyze(fileName: String): RestResponse[AnalyzeRes]

  def showCallstackList(component: String): RestResponse[BlackWhiteRes]

  def showThreadGroupList(): RestResponse[BlackWhiteRes]

}
