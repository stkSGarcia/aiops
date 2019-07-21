package io.transwarp.aiops.log.message

import java.util.UUID

import com.fasterxml.jackson.databind.annotation.JsonSerialize
import io.transwarp.aiops.log.conf.{GoalTemplate, TaskTemplate}
import io.transwarp.aiops.log.message.ErrorType.ErrorType
import io.transwarp.aiops.log.message.GoalStatus.GoalStatus
import io.transwarp.aiops.log.message.TaskStatus.TaskStatus
import io.transwarp.aiops.log.serde.CustomDateSerializer
import org.joda.time.format.{DateTimeFormat, PeriodFormat}
import org.joda.time.{DateTime, Duration}

trait GoalID {
  def getDesc: String = ""
}

object GoalStatus extends Enumeration {
  type GoalStatus = Value
  // error occur when compile goals or tasks
  val COMPILE_ERROR = Value("COMPILE_ERROR")

  // compile success and goal is complete and no error found
  val COMPLETE_SUCCESS = Value("COMPLETE_SUCCESS")

  // compile success and goal is complete but some errors found
  val COMPLETE_ERROR = Value("COMPLETE_ERROR")

  // compile success but goal is incomplete (post is missing)
  val INCOMPLETE = Value("INCOMPLETE")

  // compile success and goal is complete
  // this is a mid status (can't appear in final goal)
  val COMPLETE = Value("COMPLETE")

  implicit class SuitValue(suit: Value) {
    def dividable(): Boolean = {
      suit match {
        case COMPILE_ERROR => false
        case _ => true
      }
    }

    def compileSuccess(): Boolean = {
       suit match {
         case COMPILE_ERROR => false
         case _ => true
       }
    }
  }

}

object TaskStatus extends Enumeration {
  type TaskStatus = Value
  val INCOMPLETE = Value("INCOMPLETE")
  val COMPLETE = Value("COMPLETE")
}

object ErrorType extends Enumeration {
  type ErrorType = Value
  val NO_ERROR = Value(-1)
  val SELF_ERROR = Value(0)
  val SUB_ERROR = Value(1)
  val SELF_SUB_ERROR = Value(2)

  implicit class SuitValue(suit: Value) {
    def isSelfError(): Boolean = {
      suit match {
        case SELF_ERROR | SELF_SUB_ERROR => true
        case _ => false
      }
    }

    def hasError(): Boolean = {
      suit match {
        case NO_ERROR => false
        case _ => true
      }
    }
  }

}


case class InceptorGoalID(val sessionID: String, @JsonSerialize(using = classOf[CustomDateSerializer]) val startTime: DateTime,
                     @JsonSerialize(using = classOf[CustomDateSerializer]) val endTime: DateTime,
                     val sql: String = null, uid: String = UUID.randomUUID.toString) extends GoalID {
  val df = DateTimeFormat.forPattern("MM/dd/yyyy HH:mm:ss,SSS")

  def updateSql(newSql: String): InceptorGoalID = {
     InceptorGoalID(sessionID, startTime, endTime, newSql)
  }

  override def toString: String = {
//    sessionID + ":(" + df.print(startTime) + "):(" + df.print(endTime) + ")"
    s"$sessionID:$uid"
  }

  override def getDesc: String = {
    "sql:\n" + sql
  }
}

case class LogGoal[T <: GoalID](flag: GoalStatus, flagMsg: String, id: T, goalTemplate: GoalTemplate,
                                duration: Duration = null, entities: Array[LogEntity]) {
  def updateFlagAndMsg(newFlag: GoalStatus, newFlagMsg: String): LogGoal[T] = {
    LogGoal[T](newFlag, newFlagMsg, id, goalTemplate, duration, entities)
  }

  def updateId(newId: T): LogGoal[T] = {
    LogGoal[T](flag, flagMsg, newId, goalTemplate, duration, entities)
  }

  def getStartTime: DateTime = {
    if (entities == null) {
      null
    } else {
      entities.head.timeStamp
    }
  }

  def getEndTime: DateTime = {
    if (entities == null) {
      null
    } else {
      entities.last.timeStamp
    }
  }
}

case class LogGoalWithTask[T <: GoalID](logGoal: LogGoal[T], tasks: Array[LogTask]) {
  val pf = PeriodFormat.getDefault
  val df = DateTimeFormat.forPattern("MM/dd/yyyy HH:mm:ss,SSS")

  def flag = logGoal.flag

  def startTime = logGoal.getStartTime

  def duration = logGoal.duration

  def endTime = logGoal.getEndTime

  def getIDString = logGoal.id.toString

  def genGoalTaskBean: GoalTaskBean = {
    val desc = logGoal.goalTemplate.desc + s"\n${logGoal.id.getDesc}\n" + "\npre mark: \n" + logGoal.goalTemplate.pre +
      "\npost mark: \n" + logGoal.goalTemplate.post
    val start = logGoal.entities.head.timeStamp.getMillis
    val end = logGoal.entities.last.timeStamp.getMillis
    val dur = logGoal.duration.getMillis

    if (logGoal.flag == GoalStatus.COMPILE_ERROR) {
      GoalTaskBean(logGoal.id.toString, logGoal.goalTemplate.name, desc, logGoal.flag.toString,
        logGoal.flagMsg, duration = dur, startTime = start, endTime = end, null, null)
    } else {
      val taskBeans = tasks.map(_.genTaskBean(logGoal.entities))
      GoalTaskBean(logGoal.id.toString, logGoal.goalTemplate.name, desc, logGoal.flag.toString,
        logGoal.flagMsg, dur, start, end, taskBeans, logGoal.entities)
    }
  }
}

case class LogTask(flag: TaskStatus, taskTemplate: TaskTemplate, errorType: ErrorType, errorIndices: Array[Int] = null,
                   duration: Duration = null, preIndex: Int = -1, occurIndex: Int = -1, postIndex: Int = -1,
                   subTasks: Array[LogTask] = null) {
  val pf = PeriodFormat.getDefault
  val df = DateTimeFormat.forPattern("MM/dd/yyyy HH:mm:ss,SSS")

  def genTaskBean(entities: Array[LogEntity]): TaskBean = {
    //TODO: how to define id of tasks
    val start = entities(preIndex).timeStamp.getMillis
    val end = entities(postIndex).timeStamp.getMillis
    val dur = duration.getMillis
    val taskDes = taskTemplate.desc + "\npre mark: \n" + taskTemplate.pre + "\npost mark: \n" + taskTemplate.post
    if (subTasks == null) {
      TaskBean(name = taskTemplate.name, desc = taskDes, errorIndices = errorIndices,
        taskStatus = flag.toString, errorType = errorType.toString,
        duration = dur, startTime = start, endTime = end, preIndex = preIndex,
        occurIndex = occurIndex, postIndex = postIndex, subTasks = null)
    } else {
      val sub = new Array[TaskBean](subTasks.length)
      var index = 0
      while (index < sub.length) {
        sub(index) = subTasks(index).genTaskBean(entities)
        index += 1
      }
      TaskBean(name = taskTemplate.name, desc = taskDes, errorIndices = errorIndices,
        taskStatus = flag.toString, errorType = errorType.toString, duration = dur,
        startTime = start, endTime = end, preIndex = preIndex, occurIndex = occurIndex,
        postIndex = postIndex, subTasks = sub)
    }
  }
}

case class LogTaskContext(var flag: TaskStatus, var taskTemplate: TaskTemplate, var errorType: ErrorType = ErrorType.NO_ERROR,
                          var errorIndices: Array[Int] = null, var preIndex: Int = -1, var occurIndex: Int = -1,
                          var postIndex: Int = -1, var subTasks: Array[LogTaskContext] = null) {
  def genLeafLogTask(entities: Array[LogEntity]): LogTask = {
    val duration = new Duration(entities(preIndex).timeStamp, entities(postIndex).timeStamp)
    LogTask(flag, taskTemplate, errorType, errorIndices, duration, preIndex, occurIndex, postIndex, null)
  }

  def genLogTask(entities: Array[LogEntity], subTasks: Array[LogTask]): LogTask = {
    val duration = new Duration(entities(preIndex).timeStamp, entities(postIndex).timeStamp)
    LogTask(flag, taskTemplate, errorType, errorIndices, duration, preIndex, occurIndex, postIndex, subTasks)
  }
}





