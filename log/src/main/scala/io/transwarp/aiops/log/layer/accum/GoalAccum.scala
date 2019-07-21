package io.transwarp.aiops.log.layer.accum

import io.transwarp.aiops.Component.Component
import io.transwarp.aiops.log.layer.Observer
import io.transwarp.aiops.log.loader.LogCache

trait GoalAccum extends Observer {
  val component: Component

  def accumCache: LogCache

}
