package io.transwarp.aiops.log.layer.atom


import io.transwarp.aiops.Component
import io.transwarp.aiops.log.layer.{Subject, SubjectObserver}
import io.transwarp.aiops.log.message.{InceptorLogEntity, LogEntity}
import io.transwarp.aiops.log.parser.Level
import io.transwarp.aiops.log.pattern.InceptorPattern


class InceptorEntitySO extends SubjectObserver {
  var inceptorLogEntity: InceptorLogEntity = _

  override def update(subject: Subject): Unit = {
    val entity = subject.getUpdate.asInstanceOf[LogEntity]
    val isInceptorEntity = entity.component match {
      case Component.INCEPTOR | Component.WILDCARD => true
      case _ => false
    }

    if (entity.level == Level.END) {
      inceptorLogEntity = new InceptorLogEntity(null, entity)
      notifyObservers()
    } else {
      val filterRes = filterEntityWithSession(entity)
      if (isInceptorEntity && filterRes._1) {
        inceptorLogEntity = new InceptorLogEntity(filterRes._2, entity)
        //      println(s"InceptorSubject generate new Entity: [session: ${inceptorLogEntity.session}," +
        //        s" content: ${inceptorLogEntity.logEntity.content}]")
        notifyObservers()
      }
    }
  }

  override def getUpdate(): AnyRef = {
    inceptorLogEntity
  }

  /**
    * filter logEntity with session info
 *
    * @param logEntity
    * @return
    */
  private def filterEntityWithSession(logEntity: LogEntity): (Boolean, String) = {
      val matcher = InceptorPattern.sessionPattern.matcher(logEntity.content)
      if (matcher.find()) {
         val sessionId = matcher.group(0)
        (true, sessionId)
      } else {
        (false, null)
      }
  }

}