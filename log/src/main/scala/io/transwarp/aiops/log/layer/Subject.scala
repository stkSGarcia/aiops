package io.transwarp.aiops.log.layer

import java.util

trait Subject {
  val observers = new util.ArrayList[Observer]

  def register(observer: Observer): Unit = {
    if (observer == null) {
      throw new NullPointerException("can't register null observer")
    }
    if (!observers.contains(observer)) {
      observers.add(observer)
    }
  }

  def unregister(observer: Observer) = {
    if (observer == null) {
      throw new NullPointerException("can't unregister null observer")
    }
    observers.remove(observer)
  }

  def notifyObservers() = {
    var index = 0
    while (index < observers.size()) {
      observers.get(index).update(this)
      index += 1
    }
  }

  def getUpdate(): AnyRef

}
