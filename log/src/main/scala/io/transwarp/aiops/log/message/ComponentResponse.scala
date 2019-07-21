package io.transwarp.aiops.log.message

import scala.collection.mutable.{ArrayBuffer, _}

// abstract response for all log requests
class LogResponse

/**
  * @param status ok - analyse ready
  *               wait - waiting for next stage
  *               unzip - unzipping
  *               analyse - analysing
  * @param rate   unzip or analyse rate
  * @param compSet
  */
case class LogInitResponse(status: String, rate: Float, compSet: Array[String]) extends LogResponse

class LogInitServiceResponse extends LogResponse

// res of Inceptor Component
case class InceptorDateResponse(dates: ArrayBuffer[DateBean]) extends LogResponse

case class InceptorTimelineResponse(goalsBySession: ArrayBuffer[SessionGoalBean], size: Int) extends LogInitServiceResponse

case class InceptorSessionResponse(sessions: ArrayBuffer[SessionInfoBean], min: Long, max: Long) extends LogResponse

case class InceptorFlatGoalsResponse(goals: Array[GoalTaskThinBean], size: Int) extends LogResponse

case class InceptorGoalResponse(goal: GoalTaskBean) extends LogResponse

case class InceptorGoalTimeLineResponse(targetSession: SessionGoalBean, goalsBySession: Array[SessionGoalBean], size: Int) extends LogResponse

case class InceptorFGFilterResponse(goals: Array[GoalTaskThinBean]) extends LogResponse




