package io.transwarp.aiops.log.message

// classes for front end usage
case class GoalTaskBean(id: String, name: String, desc: String, goalStatus: String, statusMsg: String, duration: Long,
                        startTime: Long, endTime: Long, tasks: Array[TaskBean], entities: Array[LogEntity]) {
  def toGoalTaskThinBean: GoalTaskThinBean = {
    GoalTaskThinBean(id, name, desc, goalStatus, statusMsg, duration, startTime, endTime, tasks)
  }
}

case class GoalTaskThinBean(id: String, name: String, desc: String, goalStatus: String, statusMsg: String,
                            duration: Long, startTime: Long, endTime: Long, tasks: Array[TaskBean])

case class TaskBean(id: String = null, name: String, taskStatus: String, errorType: String, duration: Long,
                    desc: String, errorIndices: Array[Int] = null, startTime: Long, endTime: Long, preIndex: Int = -1,
                    occurIndex: Int = -1, postIndex: Int = -1, subTasks: Array[TaskBean] = null)

case class SessionGoalBean(id: String, goals: Array[GoalTaskThinBean])

case class SessionInfoBean(id: String, startTime: Long, endTime: Long)

case class DateBean(date: Long, errorNum: Int, longDurationNum: Int, normalNum: Int)
