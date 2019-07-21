package io.transwarp.aiops.log.message

import scala.collection.mutable.{ArrayBuffer, HashMap}


object MsgTransferUtil {

  def inceptorGoalArray2ThinBeans(goals: Array[LogGoalWithTask[InceptorGoalID]],
                                  from: Int,
                                  to: Int,
                                  goalBeanMap: HashMap[String, GoalTaskBean]): Array[GoalTaskThinBean] = {
    if (from > to) {
      throw new IllegalArgumentException("Illegal Index: from > to")
    }

    goals.slice(from, to + 1).map(goal => {
      goalBeanMap.get(goal.getIDString).get.toGoalTaskThinBean
    })
  }

  def inceptorGoalBuffer2ThinBeans(goals: ArrayBuffer[LogGoalWithTask[InceptorGoalID]],
                                   from: Int,
                                   to: Int,
                                   goalBeanMap: HashMap[String, GoalTaskBean]): ArrayBuffer[GoalTaskThinBean] = {
    if (from > to) {
      throw new IllegalArgumentException("Illegal Index: from > to")
    }

    goals.slice(from, to + 1).map(goal => {
      goalBeanMap.get(goal.getIDString).get.toGoalTaskThinBean
    })
  }
}

