//package io.transwarp.aiops.log.compile
//
//import java.util
//
//import io.transwarp.aiops.log.conf.{Component, GoalTempCombos, LogConf, TaskTemplate}
//import io.transwarp.aiops.log.message.Status
//import io.transwarp.aiops.log.message.LogTask
//import org.junit.{Assert, Before, Test}
//
//class TaskAnalyserTest {
//   var goalCombos: GoalTempCombos = null
//   var taskTempMap: util.HashMap[String, TaskTemplate] = null
//
//
//  @Before
//  def before() = {
//    LogConf.init
//    goalCombos = LogConf.goalCombosMap.get(Component.INCEPTOR)
//    taskTempMap = goalCombos.taskTempList
//  }
//
//
//  /**
//    * Test correct input
//    */
//  @Test
//  def testCorrectSample(): Unit = {
//
//    val tokenList = new util.ArrayList[LogToken]
//    var taskTemp = taskTempMap.get("0")
//    tokenList.add(LogToken(0, taskTemp.preLocator))
//    tokenList.add(LogToken(26, taskTemp.postLocator))
//    taskTemp = taskTempMap.get("1")
//    tokenList.add(LogToken(27, taskTemp.preLocator))
//    tokenList.add(LogToken(37, taskTemp.postLocator))
//    taskTemp = taskTempMap.get("2")
//    tokenList.add(LogToken(40, taskTemp.preLocator))
//    taskTemp = taskTempMap.get("2:0")
//    tokenList.add(LogToken(69, taskTemp.preLocator))
//    tokenList.add(LogToken(70, taskTemp.postLocator))
//    tokenList.add(LogToken(75, taskTemp.preLocator))
//    tokenList.add(LogToken(76, taskTemp.postLocator))
//    taskTemp = taskTempMap.get("2")
//    tokenList.add(LogToken(82, taskTemp.postLocator))
//    tokenList.add(LogToken(-1, null))
//
//    val tokenArray = new Array[LogToken](tokenList.size)
//    tokenList.toArray(tokenArray)
//
//    val analyzer = new TaskAnalyzer(tokenArray, goalCombos)
//    val tasks = analyzer.analyse
//    var index = 0
//
//    Assert.assertTrue(equalTask(tasks(0), LogTask(Status.SUCCESS, taskTempMap.get("0"), preIndex = 0, postIndex = 26)))
//    Assert.assertTrue(equalTask(tasks(1), LogTask(Status.SUCCESS, taskTempMap.get("1"), preIndex = 27, postIndex = 37)))
//    Assert.assertTrue(equalTask(tasks(2), LogTask(Status.SUCCESS, taskTempMap.get("2"), preIndex = 40, postIndex = 82)))
//    Assert.assertTrue(equalTask(tasks(2).subTasks(0), LogTask(Status.SUCCESS, taskTempMap.get("2:0"), preIndex = 69, postIndex = 70)))
//    Assert.assertTrue(equalTask(tasks(2).subTasks(1), LogTask(Status.SUCCESS, taskTempMap.get("2:0"), preIndex = 75, postIndex = 76)))
//  }
//
//
// @Test
//  def testPreMiss() = {
//
//   val tokenList = new util.ArrayList[LogToken]
//   var taskTemp = taskTempMap.get("0")
//   // miss pre for the first task
////   tokenList.add(LogToken(0, taskTemp.preLocator))
//   tokenList.add(LogToken(26, taskTemp.postLocator))
//   taskTemp = taskTempMap.get("1")
//   tokenList.add(LogToken(27, taskTemp.preLocator))
//   tokenList.add(LogToken(37, taskTemp.postLocator))
//   taskTemp = taskTempMap.get("2")
//   tokenList.add(LogToken(40, taskTemp.preLocator))
//   taskTemp = taskTempMap.get("2:0")
//   tokenList.add(LogToken(69, taskTemp.preLocator))
//   tokenList.add(LogToken(70, taskTemp.postLocator))
//   tokenList.add(LogToken(75, taskTemp.preLocator))
//   tokenList.add(LogToken(76, taskTemp.postLocator))
//   taskTemp = taskTempMap.get("2")
//   tokenList.add(LogToken(82, taskTemp.postLocator))
//   tokenList.add(LogToken(-1, null))
//
//   val tokenArray = new Array[LogToken](tokenList.size)
//   tokenList.toArray(tokenArray)
//
//   val analyzer = new TaskAnalyzer(tokenArray, goalCombos)
//   val tasks = analyzer.analyse
//   var index = 0
//
//   Assert.assertTrue(equalTask(tasks(0), LogTask(Status.SUCCESS, taskTempMap.get("0"), preIndex = 0, postIndex = 26)))
//   Assert.assertTrue(equalTask(tasks(1), LogTask(Status.SUCCESS, taskTempMap.get("1"), preIndex = 27, postIndex = 37)))
//   Assert.assertTrue(equalTask(tasks(2), LogTask(Status.SUCCESS, taskTempMap.get("2"), preIndex = 40, postIndex = 82)))
//   Assert.assertTrue(equalTask(tasks(2).subTasks(0), LogTask(Status.SUCCESS, taskTempMap.get("2:0"), preIndex = 69, postIndex = 70)))
//   Assert.assertTrue(equalTask(tasks(2).subTasks(1), LogTask(Status.SUCCESS, taskTempMap.get("2:0"), preIndex = 75, postIndex = 76)))
//
// }
//
//
//
//  private def equalTask(srcTask: LogTask, dstTask: LogTask): Boolean = {
//    (srcTask.flag == dstTask.flag) && srcTask.taskTemplate.globalID.equals(dstTask.taskTemplate.globalID) && (srcTask.preIndex == dstTask.preIndex) && (srcTask.postIndex == dstTask.postIndex)
//  }
//
//
//}
