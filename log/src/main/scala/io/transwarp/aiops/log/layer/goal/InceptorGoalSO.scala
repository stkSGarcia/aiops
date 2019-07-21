package io.transwarp.aiops.log.layer.goal

import java.util

import io.transwarp.aiops.Component
import io.transwarp.aiops.log.conf.{GoalTemplate, LogConf}
import io.transwarp.aiops.log.layer.{Subject, SubjectObserver}
import io.transwarp.aiops.log.message.GoalStatus.GoalStatus
import io.transwarp.aiops.log.message._
import io.transwarp.aiops.log.parser.Level
import org.joda.time.Duration

class InceptorGoalSO extends SubjectObserver {
  val goalTemplate = LogConf.goalCombosMap.get(Component.INCEPTOR).goalTemp
  val cacheMap = new util.HashMap[String, util.ArrayList[LogEntity]]
  var logGoal: LogGoal[InceptorGoalID] = _


  override def getUpdate(): AnyRef = {
    logGoal
  }

  override def update(subject: Subject): Unit = {
    val entity = subject.getUpdate().asInstanceOf[InceptorLogEntity]
    if (entity.logEntity.level == Level.END) {
      flushCacheMap()
    } else {
      val res = putToSession(entity.session, entity.logEntity)
      if (res._1) {
        logGoal = res._2
        //      println(s"InceptorGoalSubject generate new goal [ goalId: ${logGoal.id.toString},\n"
        //        + s"                  goalPre: ${logGoal.entities(logGoal.preIndex).content},\n"
        //        + s"                  goalPost: ${logGoal.entities(logGoal.postIndex).content}]")
        notifyObservers
      }
    }
  }

  private def flushCacheMap() = {
    val mapIter = cacheMap.entrySet.iterator()
    while (mapIter.hasNext) {
      val entry = mapIter.next
      val sessionID = entry.getKey
      val entityPool = entry.getValue
      logGoal = generateLogGoal(GoalStatus.INCOMPLETE, sessionID, "goal post mark is missing", goalTemplate, entityPool)
      notifyObservers
    }
    cacheMap.clear
  }

  private def generateLogGoal(flag: GoalStatus, sessionID: String, flagMsg: String, goalTemplate: GoalTemplate,
                              entityPool: util.ArrayList[LogEntity]): LogGoal[InceptorGoalID] = {

    val goalId = new InceptorGoalID(sessionID, entityPool.get(0).timeStamp, entityPool.get(entityPool.size - 1).timeStamp)
    if (entityPool == null) {
      LogGoal[InceptorGoalID](flag, flagMsg, goalId, goalTemplate, null, null)
    } else {
      val entities = new Array[LogEntity](entityPool.size)
      entityPool.toArray(entities)
      val duration = new Duration(entities.head.timeStamp, entities.last.timeStamp)
      LogGoal[InceptorGoalID](flag, flagMsg, goalId, goalTemplate, duration, entities)
    }
  }

  /**
    * put logEntity to corresponding session and check whether a goal has generated
    *
    * @param sessionID
    * @param logEntity
    * @return
    */
  private def putToSession(sessionID: String, logEntity: LogEntity): (Boolean, LogGoal[InceptorGoalID]) = {

    if (goalTemplate.satisfyPre(logEntity)) {
      if (cacheMap.containsKey(sessionID)) {
        // duplicated goal pre appear before post
        val entityPool = cacheMap.get(sessionID)
        val logGoal = generateLogGoal(GoalStatus.INCOMPLETE, sessionID, "goal post mark is missing", goalTemplate,
          entityPool)

        // clear cache of current session
        val newEntityPool = new util.ArrayList[LogEntity]
        newEntityPool.add(logEntity)
        cacheMap.put(sessionID, newEntityPool)

        (true, logGoal)
      } else {
        // goal pre for a new session
        val newEntityPool = new util.ArrayList[LogEntity]
        newEntityPool.add(logEntity)
        cacheMap.put(sessionID, newEntityPool)

        (false, null)
      }

    } else if (goalTemplate.satisfyPost(logEntity)) {

      if (cacheMap.containsKey(sessionID)) {
        // a new goal
        val entityPool = cacheMap.get(sessionID)
        entityPool.add(logEntity)

        val logGoal = generateLogGoal(GoalStatus.COMPLETE, sessionID,
          "complete goal found [both pre and post mark found]", goalTemplate, entityPool)

        // clear cache of current session
        cacheMap.remove(sessionID)
        (true, logGoal)

      } else {
        // goal post appear before pre
        val entityPool = new util.ArrayList[LogEntity]
        entityPool.add(logEntity)
        val logGoal = generateLogGoal(GoalStatus.COMPILE_ERROR, sessionID,
          "goal pre mark is missing while post mark found", goalTemplate, entityPool)
        (true, logGoal)
      }

    } else {
      if (cacheMap.containsKey(sessionID)) {
        val entityPool = cacheMap.get(sessionID)
        entityPool.add(logEntity)
      }
      (false, null)
    }
  }

}
