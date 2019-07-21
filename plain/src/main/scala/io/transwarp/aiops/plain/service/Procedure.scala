package io.transwarp.aiops.plain.service

import io.transwarp.aiops.Logger
import io.transwarp.aiops.plain.model.State

trait Procedure extends Logger {
  def work(s: State): Unit
}
