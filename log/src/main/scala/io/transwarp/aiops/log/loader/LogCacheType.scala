package io.transwarp.aiops.log.loader

import io.transwarp.aiops.log.loader.DateInfo.DateInfo
import io.transwarp.aiops.log.message._

import scala.collection.mutable.{ArrayBuffer, HashMap}
import scala.reflect.runtime.universe._

case class LogCacheType[A](name: String, typeTag: TypeTag[A])

object LogCacheType {
  type Goal = LogGoalWithTask[InceptorGoalID]
  type Goals = ArrayBuffer[LogGoalWithTask[InceptorGoalID]]
  type SessionGoals = ArrayBuffer[(String, Goals)]

  val InceptorDateInfo = LogCacheType("InceptorDateInfo", typeTag[HashMap[DateInfo, Int]])

  val InceptorGoalMap = LogCacheType("InceptorGoalMap", typeTag[HashMap[String, Goal]])

  val InceptorGoalBySession = LogCacheType("InceptorGoalBySession", typeTag[HashMap[String, Goals]])

//  val InceptorGoalThinBeanMap = LogCacheType("InceptorGoalThinBeanMap", typeTag[HashMap[String, GoalTaskThinBean]])

  val InceptorGoalBeanMap = LogCacheType("InceptorGoalBeanMap", typeTag[HashMap[String, GoalTaskBean]])

  val InceptorGoalSortByStart = LogCacheType("InceptorGoalSortByStart", typeTag[Goals])

  val InceptorGoalSortByEnd = LogCacheType("InceptorGoalSortByEnd", typeTag[Goals])

  val InceptorGoalSortByDuration = LogCacheType("InceptorGoalSortByDuration", typeTag[Goals])

  val InceptorSessionSort = LogCacheType("InceptorSessionSort", typeTag[ArrayBuffer[(String, Long, Double, Int, Goals)]])

  val InceptorSessionSortByMaxDurDesc = LogCacheType("InceptorSessionSortByMaxDurDesc", typeTag[SessionGoals])

  val InceptorSessionSortByMaxDurAsc = LogCacheType("InceptorSessionSortByMaxDurAsc", typeTag[SessionGoals])

  val InceptorSessionSortByAvgDurDesc = LogCacheType("InceptorSessionSortByAvgDurDesc", typeTag[SessionGoals])

  val InceptorSessionSortByAvgDurAsc = LogCacheType("InceptorSessionSortByAvgDurAsc", typeTag[SessionGoals])

  val InceptorSessionSortByExcpNumDesc = LogCacheType("InceptorSessionSortByExcpNumDesc", typeTag[SessionGoals])

  val InceptorSessionSortByExcpNumAsc = LogCacheType("InceptorSessionSortByExcpNumAsc", typeTag[SessionGoals])

  // filter cache

  val InceptorSessionFilter = LogCacheType("InceptorSessionFilter", typeTag[InceptorTimelineResponse])

  val InceptorSessionFilterState = LogCacheType("InceptorSessionFilterState", typeTag[InceptorFilterState])

  val InceptorFlatGoalFilter = LogCacheType("InceptorFlatGoalFilter", typeTag[(InceptorFilterState, Array[GoalTaskThinBean])])

//  val InceptorFlatGoalFilter = LogCacheType("InceptorFlatGoalFilter", typeTag[InceptorFlatGoalsResponse])
//
//  val InceptorFlatGoalFilterState = LogCacheType("InceptorFlatGoalFilterState", typeTag[InceptorFilterState])


  def newInstance[A: TypeTag](logCacheType: LogCacheType[A]): A = logCacheType.typeTag.mirror.runtimeClass(typeOf[A]).newInstance.asInstanceOf[A]
}

object DateInfo extends Enumeration {
  type DateInfo = Value
  val ERROR = Value
  val LONG_DURATION = Value
  val NORMAL = Value
  val Total = Value
}
