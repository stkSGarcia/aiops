package io.transwarp.aiops

import io.transwarp.aiops.AIOpsConf.ConfField.ConfField

/**
  * Implement this trait to create module `Conf`s.
  *
  * @param confField Module name
  * @param beanClass Class of ConfBean
  * @tparam T Type of ConfBean
  */
abstract class Conf[T <: ConfBean](confField: ConfField, beanClass: Class[T]) extends Logger {
  private var config: T = _

  reload()

  def getConf: T = config

  def reload(): Unit = {
    config = loadConf
    if (config == null) throw new RuntimeException(s"Cannot load config at ${this.getClass.getName}")
  }

  def loadConf: T = AIOpsConf.getConfWithName(confField)
    .map(AIOpsConf.getMapper.convertValue(_, beanClass)).getOrElse(null.asInstanceOf[T])
}

trait ConfBean
