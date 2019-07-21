package io.transwarp.aiops.log.compile

import io.transwarp.aiops.log.conf.{GoalTempCombos, PatternLocator, TaskTemplate}
import io.transwarp.aiops.log.message.LogEntity

import scala.collection.mutable.ArrayBuffer

case class LogToken(index: Int, locator: PatternLocator) {

   def matchTempPre(taskTemplate: TaskTemplate): Boolean = {
      locator.equals(taskTemplate.preLocator)
   }

   def matchTempPost(taskTemplate: TaskTemplate): Boolean = {
      locator.equals(taskTemplate.postLocator)
   }
}

class PatternTokenizer(input: Array[LogEntity], goalTempCombos: GoalTempCombos) {

  def parse(): Array[LogToken] = {
    val tokens = ArrayBuffer[LogToken]()
    var index = 0
    var curEntity: LogEntity = null
    while (index < input.length) {
      curEntity = input(index)
      val (hasMatch, patternLocator) = goalTempCombos.searchPattern(curEntity)
      if (hasMatch) {
         tokens += new LogToken(index, patternLocator)
      }
      index += 1
    }
    // add EOF token
    tokens += LogToken(index, null)
    tokens.toArray
  }

}
