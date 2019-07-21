package io.transwarp.aiops.log.layer.accum

import io.transwarp.aiops.Component
import io.transwarp.aiops.Component.Component
import io.transwarp.aiops.log.layer.Subject
import io.transwarp.aiops.log.loader.{DateInfo, LogCache, LogCacheType}
import io.transwarp.aiops.log.message._

import scala.collection.mutable.ArrayBuffer

class InceptorGoalAccum extends GoalAccum {
  override val component: Component = Component.INCEPTOR
  val cache = new LogCache
  val durationThreshold: Long = 30 * 60 * 1000

  override def update(subject: Subject): Unit = {
    val goalWithTask = subject.getUpdate.asInstanceOf[LogGoalWithTask[InceptorGoalID]]
    val goalTaskBean = goalWithTask.genGoalTaskBean
    val goalTaskThinBean = goalTaskBean.toGoalTaskThinBean
    val sessionId = goalWithTask.logGoal.id.sessionID
    val startDate = goalWithTask.startTime.toLocalDate
    val endDate = goalWithTask.endTime.toLocalDate
    val (errorNum, normalNum) = if (goalWithTask.flag == GoalStatus.COMPLETE_SUCCESS) (0, 1) else (1, 0)
    val longDurationNum = if (goalWithTask.duration.getMillis > durationThreshold) 1 else 0
    val dates = if (startDate equals endDate) Array(startDate) else Array(startDate, endDate)

    dates.foreach(date => {
      // InceptorGoalSortByEnd
      cache.get(date, LogCacheType.InceptorGoalSortByEnd).fold {
        val goalSortByEnd = LogCacheType.newInstance(LogCacheType.InceptorGoalSortByEnd)
        goalSortByEnd += goalWithTask
        cache.put(date, LogCacheType.InceptorGoalSortByEnd, goalSortByEnd)
        null
      }(f => {
        f += goalWithTask
        null
      })

      // InceptorGoalMap
      cache.get(date, LogCacheType.InceptorGoalMap).fold {
        val goalMap = LogCacheType.newInstance(LogCacheType.InceptorGoalMap)
        goalMap += goalWithTask.logGoal.id.toString -> goalWithTask
        cache.put(date, LogCacheType.InceptorGoalMap, goalMap)
        null
      }(f => {
        f += goalWithTask.logGoal.id.toString -> goalWithTask
        null
      })

      // InceptorGoalBeanMap

      cache.get(date, LogCacheType.InceptorGoalBeanMap).fold {
        val goalBeanMap = LogCacheType.newInstance(LogCacheType.InceptorGoalBeanMap)
        goalBeanMap += goalTaskBean.id -> goalTaskBean
        cache.put(date, LogCacheType.InceptorGoalBeanMap, goalBeanMap)
        null
      }(f => {
        f += goalTaskBean.id -> goalTaskBean
        null
      })

//      // InceptorGoalThinBeanMap
//      cache.get(date, LogCacheType.InceptorGoalThinBeanMap).fold {
//        val goalThinBeanMap = LogCacheType.newInstance(LogCacheType.InceptorGoalThinBeanMap)
//        goalThinBeanMap += goalTaskThinBean.id -> goalTaskThinBean
//        cache.put(date, LogCacheType.InceptorGoalThinBeanMap, goalThinBeanMap)
//        null
//      }(f => {
//        f += goalTaskThinBean.id -> goalTaskThinBean
//        null
//      })

      // InceptorGoalBySession
      cache.get(date, LogCacheType.InceptorGoalBySession).fold {
        val goalsBuffer = new ArrayBuffer[LogGoalWithTask[InceptorGoalID]]
        goalsBuffer += goalWithTask
        val goalBySession = LogCacheType.newInstance(LogCacheType.InceptorGoalBySession)
        goalBySession += sessionId -> goalsBuffer
        cache.put(date, LogCacheType.InceptorGoalBySession, goalBySession)
        null
      }(f => f.get(sessionId).fold {
        val goalsBuffer = new ArrayBuffer[LogGoalWithTask[InceptorGoalID]]
        goalsBuffer += goalWithTask
        f.put(sessionId, goalsBuffer)
        null
      }(p => {
        p += goalWithTask
        null
      }))

      // InceptorDateInfo
      cache.get(date, LogCacheType.InceptorDateInfo).fold {
        val dateInfo = LogCacheType.newInstance(LogCacheType.InceptorDateInfo)
        dateInfo += DateInfo.ERROR -> errorNum
        dateInfo += DateInfo.LONG_DURATION -> longDurationNum
        dateInfo += DateInfo.NORMAL -> normalNum
        dateInfo += DateInfo.Total -> 1
        cache.put(date, LogCacheType.InceptorDateInfo, dateInfo)
        null
      }(f => {
        f(DateInfo.ERROR) += errorNum
        f(DateInfo.LONG_DURATION) += longDurationNum
        f(DateInfo.NORMAL) += normalNum
        f(DateInfo.Total) += 1
        null
      })
    })
  }

  override def accumCache: LogCache = cache

  //  private def sortByStart = {
  //    val ordering = new Ordering[LogGoalWithTask[InceptorGoalID]] {
  //      override def compare(a: LogGoalWithTask[InceptorGoalID], b: LogGoalWithTask[InceptorGoalID]): Int = {
  //        if (a.logGoal.id.startTime isBefore b.logGoal.id.startTime) 1 else -1
  //      }
  //    }
  //    val minHeap = new PriorityQueue[LogGoalWithTask[InceptorGoalID]]()(ordering)
  //    val indexMap = new HashMap[String, Int]
  //    goalBySession.keySet.foreach(key => {
  //      val initElem = goalBySession.get(key).get
  //      minHeap.enqueue(initElem(0))
  //      indexMap += (key -> 1)
  //    })
  //
  //    while (!minHeap.isEmpty) {
  //      val goal = minHeap.dequeue()
  //      //      goalSortByStart += goal
  //      val sessionID = goal.logGoal.id.sessionID
  //
  //      val sessionGoals = goalBySession.get(sessionID).get
  //      val curIndex = indexMap.get(sessionID).get
  //      if (curIndex < sessionGoals.size) {
  //        minHeap.enqueue(sessionGoals(curIndex))
  //        indexMap.put(sessionID, curIndex + 1)
  //      }
  //    }
  //  }
}
