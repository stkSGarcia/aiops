package io.transwarp.aiops.ws.service

import java.io.InputStream

import io.transwarp.aiops.ws.controller.response.{RestResponse, SarReportRes}

/**
  * Created by hippo on 9/3/18.
  */
trait SarService {
  def serve(fileName: String, inputStream: InputStream): RestResponse[SarReportRes]
}
