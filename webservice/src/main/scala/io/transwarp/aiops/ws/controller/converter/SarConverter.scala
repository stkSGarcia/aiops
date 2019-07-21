package io.transwarp.aiops.ws.controller.converter

import io.transwarp.aiops.sar.SarReportBean
import io.transwarp.aiops.ws.controller.response.{RestResponse, ResultHeadSet, SarReportRes, SarResult}

object SarConverter {

  implicit class SarReportBean2Res(source: Array[SarReportBean]) {
    def convert: RestResponse[SarReportRes] = {
      // FIXME need to determine response status
      RestResponse(ResultHeadSet.success,
        SarReportRes(source.map(f => {
          val res = new SarResult
          res.errorMsg = f.errorMsg
          res.fileName = f.fileName
          res.titleDesc = f.titleDesc
          res.titleName = f.titleName
          res.titleEnabled = f.titleEnabled
          res.warnLevel = f.warnLevel
          res.totalRecordsNumber = f.totalRecordsNumber
          res.details = f.details
          res.wasError = f.wasError
          res.errorPercent = f.errorPercent
          res.errorNum = f.errorNum
          res
        }))
      )
    }
  }

}
