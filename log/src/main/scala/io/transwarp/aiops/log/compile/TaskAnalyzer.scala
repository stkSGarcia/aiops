package io.transwarp.aiops.log.compile

import java.util

import io.transwarp.aiops.log.compile.AnalyseStatus.AnalyseStatus
import io.transwarp.aiops.log.conf.{GoalTempCombos, TaskTemplate}
import io.transwarp.aiops.log.message.{LogTaskContext, TaskStatus}

import scala.util.{Failure, Success, Try}

object AnalyseStatus extends Enumeration {
  type AnalyseStatus = Value
  val LEGAL = Value("Legal")
  val UNKNOWN = Value("Unknown")
  val MISSING = Value("Missing")
  val END = Value("End")
}

case class AnalyseMessage(status: AnalyseStatus, message: String)

class TaskAnalyzer(tokens: Array[LogToken], goalTempCombos: GoalTempCombos) {
  val goalTemp = goalTempCombos.goalTemp
  val taskTempList = goalTemp.taskList
  var index: Int = 0
  var depth: Int = 0

  def analyse(): (Boolean, String, Array[LogTaskContext]) = {
    if (tokens == null || tokens.length == 0) {
      (true, null, new Array[LogTaskContext](0))
    } else {
      index = 0
      if (taskTempList == null || taskTempList.size == 0) {
        throw new RuntimeException(s"No task for current goal[ id: ${goalTemp.id}; name: ${goalTemp.name}]")
      }

      Try(task(taskTempList, goalTemp.globalID)) match {
        case Success(taskArray) => (true, null, taskArray)
        case Failure(f) => (false, f.getMessage, null)
      }

    }
  }

  /**
    *
    * EBNF
    *
    * task = (EOF | super_task_POST | super_task_STOP | PRE POST | PRE STOP | PRE task POST | PRE task STOP) *
    *
    * 1. if no pre, some log entities might be lost
    * 2. STOP stand for task end token and eof token
    *
    */
  def task(taskTempList: util.ArrayList[TaskTemplate], taskGID: String): Array[LogTaskContext] = {
    val taskList = new util.ArrayList[LogTaskContext]
    var preTempIndex = 0
    var status: AnalyseMessage = AnalyseMessage(AnalyseStatus.LEGAL, null)
    var currTaskIndex: Int = -1
    var currPreIndex: Int = -1
    var hasPre: Boolean = false
    depth += 1
    while (index < tokens.length && (status.status == AnalyseStatus.LEGAL)) {
      if (isEOF(tokens(index))) {
        status = AnalyseMessage(AnalyseStatus.END, s"loop end, encounter EOF token for task GID ${taskGID}")
      } else {
        val res = expectedPRE(tokens(index), taskTempList, preTempIndex)
        hasPre = res._1
        currTaskIndex = res._2
        if (hasPre) {
          currPreIndex = index
          // find effective pre
          index += 1
          val currTask = taskTempList.get(currTaskIndex)

          if (taskStop(tokens(index), currTask)) {
            // stop
            taskList.add(LogTaskContext(TaskStatus.INCOMPLETE, currTask, preIndex = tokens(currPreIndex).index,
              postIndex = tokens(index).index - 1))
            preTempIndex = currTaskIndex
          } else if (tokens(index).matchTempPost(currTask)) {
            // post
            taskList.add(LogTaskContext(TaskStatus.COMPLETE, currTask, preIndex = tokens(currPreIndex).index, postIndex = tokens(index).index))
            preTempIndex = currTaskIndex
            index += 1
          } else if (currTask.taskList != null) {
            // task
            val sub = task(currTask.taskList, currTask.globalID)
            if (sub == null) {
              status = AnalyseMessage(AnalyseStatus.MISSING, s"no expected sub task found for task GID ${taskGID}")
            }
            if (tokens(index).matchTempPost(currTask)) {
              // post
              taskList.add(LogTaskContext(TaskStatus.COMPLETE, currTask, preIndex = tokens(currPreIndex).index,
                postIndex = tokens(index).index, subTasks = sub))
              preTempIndex = currTaskIndex
              index += 1
            } else if (taskStop(tokens(index), currTask)) {
              // stop
              taskList.add(LogTaskContext(TaskStatus.INCOMPLETE, currTask, preIndex = tokens(currPreIndex).index,
                postIndex = tokens(index).index - 1, subTasks = sub))
              preTempIndex = currTaskIndex
            } else {
              // no effective end token for current task
              status = AnalyseMessage(AnalyseStatus.MISSING, s"no effective end token for task GID ${taskGID}")
            }
          } else {
            // no effective token after pre
            status = AnalyseMessage(AnalyseStatus.MISSING, s"no effective token after pre for task GID ${taskGID}")
          }
        } else {
          // judge loop end
          val superTask = taskTempList.get(0).superTask
          if (superTask == null) {
            status = AnalyseMessage(AnalyseStatus.MISSING, s"can't find loop end for top level tasks [goalID ${taskGID}]")
          } else if (superTask.postLocator.equals(tokens(index).locator) || taskStop(tokens(index), superTask)) {
            status = AnalyseMessage(AnalyseStatus.END, s"loop end, super task [GID ${taskGID}] post encountered")
          } else {
            status = AnalyseMessage(AnalyseStatus.MISSING, s"can't find loop end for sub task of task [taskGID ${taskGID}]")
          }
        }
      }
    }

    if (status.status == AnalyseStatus.MISSING) {
      throw new RuntimeException(s"task division failed because ${status.message}")
    }

    depth -= 1
    if (taskList.size == 0) {
      null
    } else {
      val result = new Array[LogTaskContext](taskList.size)
      taskList.toArray(result)
      result
    }
  }


  private def initAnalyse() = {
    index = 0
    if (taskTempList == null || taskTempList.size == 0) {
      throw new RuntimeException(s"No task for current goal[ id: ${goalTemp.id}; name: ${goalTemp.name}]")
    }
  }

  private def expectedPRE(token: LogToken, tmpList: util.ArrayList[TaskTemplate], currTmpIndex: Int): (Boolean, Int) = {
    var reachMandatory = false
    var findPRE = false
    var index = currTmpIndex
    var res = (false, -1)
    def stopLoop: Boolean = {
      reachMandatory || findPRE
    }

    while (index < tmpList.size && !stopLoop) {
      val curTmp = tmpList.get(index)
      if (token.locator.equals(curTmp.preLocator)) {
        findPRE = true
        res = (true, index)
      } else if ((index != currTmpIndex) && !curTmp.optional) {
        reachMandatory = true
        res = (false, -1)
      } else {
        index += 1
      }
    }

    res
  }


//  private def expectedPRE(token: LogToken, tmpList: util.ArrayList[TaskTemplate], currTmpIndex: Int): (Boolean, Int) = {
//    if (token.locator.equals(tmpList.get(currTmpIndex).preLocator)) {
//      (true, currTmpIndex)
//    } else if (((currTmpIndex + 1) < tmpList.size) && token.locator.equals(tmpList.get(currTmpIndex + 1).preLocator)) {
//      (true, currTmpIndex + 1)
//    } else {
//      (false, -1)
//    }
//  }

  private def isEOF(token: LogToken): Boolean = {
     token.locator == null
  }

  private def taskStop(token: LogToken, taskTemp: TaskTemplate): Boolean = {
    isEOF(token) || goalTempCombos.isTaskEnd(token, taskTemp)
  }
}
