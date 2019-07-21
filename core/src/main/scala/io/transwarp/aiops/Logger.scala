package io.transwarp.aiops

import org.slf4j
import org.slf4j.LoggerFactory

trait Logger {
  val LOG: slf4j.Logger = LoggerFactory.getLogger(this.getClass)
}
