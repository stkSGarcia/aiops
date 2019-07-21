package io.transwarp.aiops.log.layer.goal

import io.transwarp.aiops.log.layer.{Observer, Subject}
import io.transwarp.aiops.log.message.{InceptorGoalID, LogGoal}
import io.transwarp.aiops.log.pattern.InceptorPattern

class InceptorGoalSqlSO extends Subject with Observer {
  var logGoal: LogGoal[InceptorGoalID] = _

  override def getUpdate(): AnyRef = {
     logGoal
  }

  override def update(subject: Subject): Unit = {
    val currGoal = subject.getUpdate().asInstanceOf[LogGoal[InceptorGoalID]]
    if (currGoal.flag.compileSuccess()) {
      var index = 0
      var findSql = false
      var sql:String = null
      while (index < currGoal.entities.length && !findSql) {
        val matcher = InceptorPattern.sqlPattern.matcher(currGoal.entities(index).content)
        if (matcher.find) {
          sql = matcher.group(0)
          findSql = true
        }
        index += 1
      }
      if (!findSql) {
        sql = "Sql statement not found"
      }
      logGoal = currGoal.updateId(currGoal.id.updateSql(sql))
    } else {
      val sql = "no sql statement due to goal compile error"
     logGoal = currGoal.updateId(currGoal.id.updateSql(sql))
    }
    notifyObservers()
  }

}
