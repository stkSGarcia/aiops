package io.transwarp.aiops.jstack.conf

import org.scalatest.FunSuite

class JstackConfSuite extends FunSuite {
  test("JstackConf") {
    JstackConf.getConf.stackTraceCompMap
    JstackConf.getConf.threadGroup
  }
}
