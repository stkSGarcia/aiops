package io.transwarp.aiops.log.layer.task

import java.util

import io.transwarp.aiops.Component
import io.transwarp.aiops.log.compile.{PatternTokenizer, TaskAnalyzer}
import io.transwarp.aiops.log.conf.{LogConf, TaskTemplate}
import io.transwarp.aiops.log.layer.{Subject, SubjectObserver}
import io.transwarp.aiops.log.message._

import scala.collection.mutable.ArrayBuffer


class InceptorGoalTaskSO extends SubjectObserver {
  var goalWithTask: LogGoalWithTask[InceptorGoalID] = _
  var currGoal: LogGoal[InceptorGoalID] = _
  val goalCombos = LogConf.goalCombosMap.get(Component.INCEPTOR)
  val goalTemplate = LogConf.goalCombosMap.get(Component.INCEPTOR).goalTemp
  val taskTempList: util.ArrayList[TaskTemplate] = goalTemplate.taskList

  override def getUpdate(): AnyRef = {
    goalWithTask
  }


  override def update(subject: Subject): Unit = {
    currGoal = subject.getUpdate().asInstanceOf[LogGoal[InceptorGoalID]]
    if (currGoal.flag.dividable()) {
      val tokenizer = new PatternTokenizer(currGoal.entities, goalCombos)
      val tokens = tokenizer.parse
      val taskDivider = new TaskAnalyzer(tokens, goalCombos)
      val (isSuccess, msg, taskContexts) = taskDivider.analyse

      if (isSuccess) {
        val tasks = checkError(taskContexts, currGoal.entities)
        var hasError = false
        var index = 0
        while (index < tasks.length && !hasError) {
          hasError = tasks(index).errorType.hasError
          index += 1
        }

        if (currGoal.flag == GoalStatus.INCOMPLETE) {
          if (hasError) {
            goalWithTask = LogGoalWithTask[InceptorGoalID](currGoal.updateFlagAndMsg(currGoal.flag,
              currGoal.flagMsg + ", goal divided into tasks sucessfully but has tasks with error"), tasks)
          } else {
            goalWithTask = LogGoalWithTask[InceptorGoalID](currGoal.updateFlagAndMsg(currGoal.flag,
              currGoal.flagMsg + ", goal divided into tasks sucessfully and all tasks are run successfully"), tasks)
          }
        } else {
          if (hasError) {
            goalWithTask = LogGoalWithTask[InceptorGoalID](currGoal.updateFlagAndMsg(GoalStatus.COMPLETE_ERROR,
              "goal divided into tasks sucessfully but has tasks with error"), tasks)
          } else {
            goalWithTask = LogGoalWithTask[InceptorGoalID](currGoal.updateFlagAndMsg(GoalStatus.COMPLETE_SUCCESS,
              "goal divided into tasks sucessfully and all tasks are run successfully"), tasks)
          }
        }
//        println("InceptorGoalTaskSubject generate new goalWithTask: "
//          + s"[status: ${goalWithTask.logGoal.flag}, status msg: ${goalWithTask.logGoal.flagMsg}, " +
//          s"goalID: ${goalWithTask.logGoal.id.toString}, tasknum: ${goalWithTask.tasks.length}]")
      } else {
        // task compile error
        goalWithTask = LogGoalWithTask[InceptorGoalID](currGoal.updateFlagAndMsg(GoalStatus.COMPILE_ERROR, msg), null)
//        println("InceptorGoalTaskSubject generate new goalWithTask: "
//          + s"[status: ${goalWithTask.logGoal.flag}, status msg: ${goalWithTask.logGoal.flagMsg}, " +
//          s"goalID: ${goalWithTask.logGoal.id.toString}, tasknum: 0")
      }
    } else {
      // goal compile error
      goalWithTask = LogGoalWithTask[InceptorGoalID](currGoal, null)
//      println("InceptorGoalTaskSubject generate new goalWithTask: "
//        + s"[status: ${goalWithTask.logGoal.flag}, status msg: ${goalWithTask.logGoal.flagMsg}, " +
//        s"goalID: ${goalWithTask.logGoal.id.toString}, tasknum: 0")
    }
    notifyObservers
  }

  private def checkError(taskContexts: Array[LogTaskContext], entities: Array[LogEntity]): Array[LogTask] = {
    if (taskContexts == null) {
      null
    } else if (taskContexts.length == 0) {
      new Array[LogTask](0)
    } else {
      val tasks = new Array[LogTask](taskContexts.length)
      var index = 0
      var currContext: LogTaskContext = null
      while (index < taskContexts.length) {
        currContext = taskContexts(index)
        if (currContext.subTasks != null) {
          val subTasks = checkError(currContext.subTasks, entities)
          var subIndex = 0
          var hasSubError = false
          while (subIndex < subTasks.length && !hasSubError) {
            hasSubError = subTasks(subIndex).errorType.hasError()
            subIndex += 1
          }
          val errorArray = findSelfError(currContext, entities)
          val hasSelfError = (errorArray != null)

          if (hasSelfError && hasSubError) {
            currContext.errorType = ErrorType.SELF_SUB_ERROR
            currContext.errorIndices = errorArray
          } else if ((!hasSelfError) && (!hasSubError)) {
            currContext.errorType = ErrorType.NO_ERROR
          } else if (hasSelfError) {
            currContext.errorType = ErrorType.SELF_ERROR
            currContext.errorIndices = errorArray
          } else {
            currContext.errorType = ErrorType.SUB_ERROR
          }

          tasks(index) = currContext.genLogTask(entities, subTasks)
        } else {
          val errorArray = findSelfError(currContext, entities)
          if (errorArray == null) {
            currContext.errorType = ErrorType.NO_ERROR
          } else {
            currContext.errorType = ErrorType.SELF_ERROR
            currContext.errorIndices = errorArray
          }
          tasks(index) = currContext.genLeafLogTask(entities)
        }
        index += 1
      }
      tasks
    }
  }


  private def findSelfError(taskContext: LogTaskContext, entities: Array[LogEntity]): Array[Int] = {
    val errorList = new ArrayBuffer[Int]
    if (taskContext.subTasks == null) {
      var index = taskContext.preIndex
      while (index <= taskContext.postIndex) {
        if (entities(index).level.isError) {
          errorList += index
        }
        index += 1
      }
    } else {
      val len = taskContext.postIndex - taskContext.preIndex + 1
      val bitMarker = new util.BitSet(len)
      bitMarker.set(0, len - 1, true)

      var index = 0
      var currContext: LogTaskContext = null
      while (index < taskContext.subTasks.length) {
        currContext = taskContext.subTasks(index)
        bitMarker.set(currContext.preIndex - taskContext.preIndex, currContext.postIndex - taskContext.preIndex, false)
        index += 1
      }

      index = taskContext.preIndex
      while (index <= taskContext.postIndex) {
        if (bitMarker.get(index - taskContext.preIndex) && entities(index).level.isError) {
          errorList += index
        }
        index += 1
      }
    }

    if (errorList.size == 0) {
      null
    } else {
      errorList.toArray
    }
  }


}
