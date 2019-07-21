package io.transwarp.aiops.log.layer.view

import io.transwarp.aiops.Component
import io.transwarp.aiops.log.conf.LogConf
import io.transwarp.aiops.log.layer.{Observer, Subject}
import io.transwarp.aiops.log.message._

//object Test {
//  def main(args:Array[String])= {
//    println("haha")
//  }
//}
class InceptorViewObserver extends Observer {

  override def update(subject: Subject): Unit = {
    var depth: Int = 0
    val goalWithTask = subject.getUpdate.asInstanceOf[LogGoalWithTask[InceptorGoalID]]
    val goalTemplate = LogConf.goalCombosMap.get(Component.INCEPTOR).goalTemp
    val logGoal = goalWithTask.logGoal
    val tasks = goalWithTask.tasks
    println("------------------------------------------------------------------------")

    println("Goal ID: %s".format(logGoal.id))
    println("Goal Status: %s".format(logGoal.flag))
    println("Goal Status Msg: %s".format(logGoal.flagMsg))
    println("Goal Desc: ")
    println("Sql: ")
    println(logGoal.id.sql)
    println(logGoal.goalTemplate.desc)

    if (logGoal.flag != GoalStatus.COMPILE_ERROR) {
      println("Goal Pre: [content: \n%s], \nGoal Post: [content: \n%s]".format(logGoal.entities(0).content,
        logGoal.entities.last.content))
    }

    println()

    if (tasks == null) {
      println("no task has been found for current goal")
    } else {
      tasks.foreach((task) => {
        println("******************")
        printTask(task, logGoal, depth)
        println()
      })
    }

    println("------------------------------------------------------------------------")

  }

  private def printTask(task: LogTask, logGoal: LogGoal[InceptorGoalID], depth: Int): Unit = {
    println()
    val prefix: StringBuilder = new StringBuilder
    var index = 0
    while (index < depth) {
      prefix.append("  ")
      index += 1
    }
    val prefixString = prefix.toString
    println(prefixString + "Task GID: %s".format(task.taskTemplate.globalID))
    println(prefixString + "Task Status: %s".format(task.flag))
    println(prefixString + "Task Error Status: %s".format(task.errorType))
    if (task.errorType == ErrorType.SELF_ERROR) {
      println(prefixString + "Task Error Content:")
      println(prefixString + "  [")
      var errorIndex = 0
      while (errorIndex < task.errorIndices.length) {
        println(prefixString + "    " + logGoal.entities(task.errorIndices(errorIndex)).content)
        errorIndex += 1
      }
      println(prefixString + "  ]")
    }

    println(prefixString + "Task Pre: [index: %s, content: ".format(task.preIndex))
    println(prefixString + s"  ${logGoal.entities(task.preIndex)}]")
    val tasks = task.subTasks
    if (tasks != null) {
      println
      println(prefixString)
      tasks.foreach((task) => {
        printTask(task, logGoal, depth + 1)
      })
      println
      println(prefixString + "]")
      println
    }
    println(prefixString + "Task Post: [index: %s , content: ".format(task.postIndex))
    println(prefixString + s"  ${logGoal.entities(task.postIndex)}]")
  }

}
