package io.transwarp.aiops.ws.spring.util

import io.transwarp.aiops.log.message.GoalStatus
import io.transwarp.aiops.log.message.GoalStatus._

import scala.collection.mutable.HashSet

object WebUtils {

  def decodeGoalType(goalType: Int): HashSet[GoalStatus] = {
    val res = new HashSet[GoalStatus]
    if (goalType == -1) {
      res.add(GoalStatus.COMPILE_ERROR)
      res.add(GoalStatus.COMPLETE_SUCCESS)
      res.add(GoalStatus.COMPLETE_ERROR)
      res.add(GoalStatus.INCOMPLETE)
    } else {
      if ((goalType & 0x0001) != 0) {
        res.add(GoalStatus.INCOMPLETE)
      }
      if ((goalType & 0x0002) != 0) {
        res.add(GoalStatus.COMPLETE_ERROR)
      }
      if ((goalType & 0x0004) != 0) {
        res.add(GoalStatus.COMPLETE_SUCCESS)
      }
      if ((goalType & 0x0008) != 0) {
        res.add(GoalStatus.COMPILE_ERROR)
      }
    }
    res
  }

}
