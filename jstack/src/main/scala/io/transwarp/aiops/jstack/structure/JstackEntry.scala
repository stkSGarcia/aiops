package io.transwarp.aiops.jstack.structure

import io.transwarp.aiops.jstack.structure.ThreadState.ThreadState

import scala.beans.BeanProperty

class JstackEntry {
  @BeanProperty var threadName: String = _
  @BeanProperty var id: String = _
  @BeanProperty var isDaemon: Boolean = false
  @BeanProperty var prio: String = _
  @BeanProperty var os_prio: String = _
  @BeanProperty var tid: String = _
  @BeanProperty var nid: String = _
  @BeanProperty var startLine: String = _
  @BeanProperty var callStack: String = _
  @BeanProperty var threadState: ThreadState = _
  @BeanProperty var method: String = _
  @BeanProperty var locks: Array[Lock] = _
  @BeanProperty var waitLocks: Array[Lock] = _

  override def toString: String = s"threadName: $threadName" +
    s"\nid: $id" +
    s"\nisDaemon: $isDaemon" +
    s"\nprio: $prio" +
    s"\nos_prio: $os_prio" +
    s"\ntid: $tid" +
    s"\nnid: $nid" +
    s"\nstartLine: $startLine" +
    s"\ncallStack:\n$callStack" +
    s"\nthreadState: $threadState" +
    s"\nmethod: $method" +
    s"\nlocks: " + (if (locks != null) locks.map(_.addr).mkString(",") else "null") +
    s"\nwaitLocks: " + (if (waitLocks != null) waitLocks.map(_.addr).mkString(",") else "null")
}

// FIXME: originLine is currently useless.
case class Lock(addr: String, name: String, originLine: String)
