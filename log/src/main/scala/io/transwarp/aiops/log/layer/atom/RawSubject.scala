package io.transwarp.aiops.log.layer.atom

import io.transwarp.aiops.log.layer.Subject
import io.transwarp.aiops.log.message.LogEntity

class RawSubject extends Subject {
  // file or in memory structure or database to denote log entity set
  var logEntity: LogEntity = _

  def postMessage(newLogEntity: LogEntity) {
//    println(s"RawSubject generate new logEntity: ${newLogEntity.content}")
//    println
    logEntity = newLogEntity
    notifyObservers
  }

  override def getUpdate(): AnyRef = {
    logEntity
  }

}
