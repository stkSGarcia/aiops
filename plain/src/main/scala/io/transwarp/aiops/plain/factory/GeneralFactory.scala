package io.transwarp.aiops.plain.factory

import io.transwarp.aiops.Logger
import io.transwarp.aiops.plain.model.{ResStatus, State}
import io.transwarp.aiops.plain.service.Procedure

import scala.collection.mutable.ArrayBuffer

abstract class GeneralFactory[T <: State] extends Logger {
  private val procedures = new ArrayBuffer[Procedure]

  def addProcedure(p: Procedure): Unit = procedures += p

  def work(state: T): Unit = {
    var stop = false
    var i = 0
    while (!stop && i < procedures.length) {
      procedures(i).work(state)
      state.status match {
        case ResStatus.FAILURE => stop = true
        case _ =>
      }
      i += 1
    }
  }
}
