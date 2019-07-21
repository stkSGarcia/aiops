package io.transwarp.aiops.log.conf

import java.util
import java.util.regex.Pattern

import io.transwarp.aiops.log.compile.LogToken
import io.transwarp.aiops.log.conf.TaskMark.TaskMark
import io.transwarp.aiops.log.message.LogEntity

object TaskMark extends Enumeration {
  type TaskMark = Value
  val UNKNOWN = Value("UNKNOWN")
  val PRE = Value("PRE")
  val OCCUR = Value("OCCUR")
  val POST = Value("POST")

}

case class PatternLocator(pattern: Pattern, taskTemp: TaskTemplate, taskMark: TaskMark) {

  def matchPattern(entity: LogEntity): Boolean = {
    pattern.matcher(entity.content).find()
  }

  override def toString(): String = {
    s"[Pattern: ${pattern.toString}, TaskTempGID: ${taskTemp.globalID}, Mark: ${taskMark}]"
  }

}

class GoalTempCombos(val goalTemp: GoalTemplate,
                     val taskEndMap: util.HashMap[TaskTemplate, util.HashSet[PatternLocator]] =
                     new util.HashMap[TaskTemplate, util.HashSet[PatternLocator]],
                     val taskTempList: util.HashMap[String, TaskTemplate] = new util.HashMap[String, TaskTemplate]) {

  combosCompile

  def isTaskEnd(token: LogToken, taskTemp: TaskTemplate): Boolean = {

    val taskEnd = taskEndMap.get(taskTemp)
    taskEnd.contains(token.locator)
  }

  def searchPattern(entity: LogEntity): (Boolean, PatternLocator) = {
    var index = 0
    var hasMatch = false
    var targetLocator: PatternLocator = null
    var currTaskTemp: TaskTemplate = null
    val taskTempIter = taskTempList.values().iterator
    while (taskTempIter.hasNext && !hasMatch) {
      currTaskTemp = taskTempIter.next
      if (currTaskTemp.preLocator.matchPattern(entity)) {
        hasMatch = true
        targetLocator = currTaskTemp.preLocator
      } else if (currTaskTemp.postLocator.matchPattern(entity)) {
        hasMatch = true
        targetLocator = currTaskTemp.postLocator
      }
    }
    (hasMatch, targetLocator)
  }

  private def combosCompile(): Unit = {
    val taskList = goalTemp.taskList
    if (taskList != null) {
      var index = 0
      var currTask: TaskTemplate = null
      /*
       * top level task iter
       *
       * 1.set level
       * 2.set globalID
       * 3.construct task list
       * 4.construct taskEndMap
       */
      index = 0
      while (index < taskList.size) {
        currTask = taskList.get(index)
        currTask.setLevel(0)
        currTask.setSuperTaskAndGlobalID(null)
        taskTempList.put(currTask.globalID, currTask)
        taskEndMap.put(currTask, getTaskEndMark(currTask, index))
        //        println(s"task with GID ${currTask.globalID} has been inserted")
        traverse(currTask)
        index += 1
      }
    } else {
      throw new RuntimeException(s"No task exists for current goal [id = ${goalTemp.id}, name = ${goalTemp.name}]")
    }
  }

  private def traverse(taskTemp: TaskTemplate): Unit = {
    val taskList = taskTemp.taskList
    if (taskList != null) {
      var index = 0
      var currTask: TaskTemplate = null
      val level = taskTemp.level + 1
      while (index < taskList.size) {
        currTask = taskList.get(index)
        currTask.setLevel(level)
        currTask.setSuperTaskAndGlobalID(taskTemp)
        taskTempList.put(currTask.globalID, currTask)
        taskEndMap.put(currTask, getTaskEndMark(currTask, index))
        traverse(currTask)
        index += 1
      }
    }
  }

  private def getTaskEndMark(taskTemplate: TaskTemplate, taskIndex: Int): util.HashSet[PatternLocator] = {
    val res = new util.HashSet[PatternLocator]
    val superTask = taskTemplate.superTask
    if (superTask == null) {
      // top level task
      val taskList = goalTemp.taskList
      var index = taskIndex + 1
      var curTask: TaskTemplate = null
      while (index < taskList.size) {
        curTask = taskList.get(index)
        res.add(curTask.preLocator)
        res.add(curTask.postLocator)
        index += 1
      }
    } else {
      // add end mark of superTask
      res.addAll(taskEndMap.get(superTask))

      // add superTask
      res.add(superTask.preLocator)
      res.add(superTask.postLocator)

      // add sibling of this task
      val taskList = superTask.taskList
      var index = taskIndex + 1
      var curTask: TaskTemplate = null
      while (index < taskList.size) {
        curTask = taskList.get(index)
        res.add(curTask.preLocator)
        res.add(curTask.postLocator)
        index += 1
      }
    }
    res
  }

}

//case class LogFormat(config: mutable.LinkedHashMap[String, String])
//
//object Component extends Enumeration {
//  type Component = Value
//  val UNKNOWN = Value("UNKNOWN")
//  val TEST = Value("TEST")
//  val INCEPTOR = Value("INCEPTOR")
//  val OS = Value("OS")
//  val MANAGER = Value("MANAGER")
//  val HYPERBASE = Value("HYPERBASE")
//  val SEARCH = Value("SEARCH")
//  val HADOOP = Value("HADOOP")
//  val SHIVA = Value("SHIVA")
//  val SOPHON = Value("SOPHON")
//  val TOS = Value("TOS")
//  val WILDCARD = Value("WILDCARD")
//
//  def withCaseIgnoreName(comp: String): Component = {
//     Component.withName(comp.toUpperCase)
//  }
//}
//
//
//object LogConf {
//  var instance: LogConf = null
//  val lock = new Array[Byte](0)
//  def newInstance: LogConf = {
//    if (instance == null) {
//      lock.synchronized {
//        if (instance == null) {
//           instance = new LogConf
//        }
//      }
//    }
//    instance
//  }
//
//  def goalCombosMap = newInstance.goalCombosMap
//}
//
//
//class LogConf private {
//  val testPath = "log/test.json"
//  val inceptorPath = "log/goalTemplate/inceptor.json"
//  val goalCombosMap: util.HashMap[Component, GoalTempCombos] = new util.HashMap
//  val formatFile = "log/log_format.json"
//  var logFormats: Map[String, LogFormat] = _
//
//  init
//
//  def init() = {
//    initTemplate
//    loadFormat
//  }
//
//  private def initTemplate(): Unit = {
//    val templateMap = loadTemplateFromJSON
//    val componentIter = templateMap.keySet.iterator
//    var currComp: Component = null
//    while (componentIter.hasNext) {
//      currComp = componentIter.next
//      goalCombosMap.put(currComp, new GoalTempCombos(templateMap.get(currComp)))
//    }
//  }
//
//  private def loadTemplateFromJSON(): util.HashMap[Component, GoalTemplate] = {
//    val tmpMap = new util.HashMap[Component, GoalTemplate]
//
//    // inceptor
//    val inceptorTmpStr = IOUtils.toString(getClass.getClassLoader.getResourceAsStream(inceptorPath))
//    val inceptorTemplate = JSON.parseObject(inceptorTmpStr, classOf[GoalTemplate])
//    tmpMap.put(Component.INCEPTOR, inceptorTemplate)
//
//    tmpMap
//  }
//
//
//  /**
//    * Load the default configuration file for log format.
//    */
//  def loadFormat(): Unit = {
//    val inputStream = getClass.getClassLoader.getResourceAsStream(formatFile)
//    var formatMap: Map[String, LogFormat] = Map()
//    val mapper = new ObjectMapper with ScalaObjectMapper
//    mapper.registerModule(DefaultScalaModule)
//    val configMap = mapper.readValue[Map[String, mutable.LinkedHashMap[String, String]]](inputStream)
//    configMap.foreach { case (name, format) => formatMap += (name -> LogFormat(format)) }
//    logFormats = formatMap
//  }
//
//}
