package io.transwarp.aiops.jstack.structure

import io.transwarp.aiops.jstack.structure.ThreadState.ThreadState

object ThreadState extends Enumeration {
  type ThreadState = Value
  val NEW, RUNNABLE, BLOCKED, WAITING, TIMED_WAITING, TERMINATED = Value
}

object StartLineState extends Enumeration {
  type StartLineState = Value
  val RUNNABLE = Value("runnable")
  val WAITING_ON_CONDITION = Value("waiting on condition")
  val OBJECT_WAIT = Value("in object.wait()")
  val WAITING_FOR_MONITOR_ENTRY = Value("waiting for monitor entry")
  val BLOCKED = Value("blocked")
  val SLEEPING = Value("sleeping")

  def toThreadState(suit: Value): ThreadState = suit match {
    case RUNNABLE => ThreadState.RUNNABLE
    case WAITING_ON_CONDITION | OBJECT_WAIT | SLEEPING => ThreadState.WAITING
    case BLOCKED | WAITING_FOR_MONITOR_ENTRY => ThreadState.BLOCKED
  }

  def findState(input: String): Option[Value] = if (input == null) None else values.find(input contains _.toString)
}
