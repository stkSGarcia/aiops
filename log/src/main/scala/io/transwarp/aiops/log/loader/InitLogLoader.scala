package io.transwarp.aiops.log.loader

import java.util

import io.transwarp.aiops.Component
import io.transwarp.aiops.Component.Component
import io.transwarp.aiops.log.layer.accum.{GoalAccum, InceptorGoalAccum}
import io.transwarp.aiops.log.layer.atom.{InceptorEntitySO, RawSubject}
import io.transwarp.aiops.log.layer.goal.{InceptorGoalSO, InceptorGoalSqlSO}
import io.transwarp.aiops.log.layer.task.InceptorGoalTaskSO
import io.transwarp.aiops.log.layer.view.InceptorViewObserver
import io.transwarp.aiops.log.message._

import scala.collection.mutable.{ArrayBuffer, _}


class InitLogLoader(rawSubject: RawSubject = new RawSubject, components: util.Set[Component],
                    accums: ArrayBuffer[GoalAccum] = new ArrayBuffer[GoalAccum]) {

//  private val cacheMap = new HashMap[String, AnyRef]
  private val cacheMap = new HashMap[Component, LogCache]

  constructNetwork()

  def postMsg(entity: LogEntity): Unit = {
    rawSubject.postMessage(entity)
  }

  def genCache: HashMap[Component, LogCache] = {
     accums.foreach(goalAccum => {
        cacheMap += goalAccum.component -> goalAccum.accumCache
     })
    cacheMap
  }

  private def constructNetwork(): Unit = {
    val compIter = components.iterator
    while (compIter.hasNext) {
      compIter.next match {
        case Component.INCEPTOR => constructInceptorNetwork
        case _ => {}
      }
    }
  }

  private def constructInceptorNetwork(): Unit = {
    val inceptorSubject = new InceptorEntitySO

    // construct goal
    val inceptorGoalSubject = new InceptorGoalSO

    // construct goal with sql info
    val inceptorGoalSQLSubject = new InceptorGoalSqlSO

    // contruct goal with tasks
    val inceptorGoalTaskSubject = new InceptorGoalTaskSO

    // construct goal accumulator
    val inceptorGoalAccum = new InceptorGoalAccum
    //    val inceptorGoalAccumTransfer = new InceptorGoalBySessionAccum
//    val inceptorGoalAccumTransfer = new InceptorGoalSortByEndAccum

    // contruct goal viewer
    val inceptorViewObserver = new InceptorViewObserver

    // register observers

    //register observers for rawSubject
    rawSubject.register(inceptorSubject)

    //register observers for inceptorSubject
    inceptorSubject.register(inceptorGoalSubject)

    //register observers for inceptorGoalSubject
    inceptorGoalSubject.register(inceptorGoalSQLSubject)

    //register observers for inceptorGoalSQLSubject
    inceptorGoalSQLSubject.register(inceptorGoalTaskSubject)

    //register test goal observers for inceptorGoalTaskSubject
//    inceptorGoalTaskSubject.register(inceptorViewObserver)

    //register goal accumulator for inceptorGoalTaskSubject
    inceptorGoalTaskSubject.register(inceptorGoalAccum)

    accums += inceptorGoalAccum
  }

}




