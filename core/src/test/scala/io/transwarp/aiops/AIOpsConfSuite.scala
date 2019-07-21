package io.transwarp.aiops

import io.transwarp.aiops.AIOpsConf.ConfField
import org.scalatest.FunSuite

class AIOpsConfSuite extends FunSuite {
  test("AIOps custom conf") {
    val confPath = getClass.getClassLoader.getResource("config.yml").getPath
    System.setProperty("external.config", confPath)
    AIOpsConf.getConfWithName(ConfField.SPRINGBOOT)
  }
}
