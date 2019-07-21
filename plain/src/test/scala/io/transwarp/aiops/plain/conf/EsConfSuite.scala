package io.transwarp.aiops.plain.conf

import org.scalatest.FunSuite

class EsConfSuite extends FunSuite {
  test("EsConfg") {
    val str1 = EsConf.getJsonString(EsConf.getConf.setting)
    val str2 = EsConf.getJsonString(EsConf.getConf.data_mapping)
    val str3 = EsConf.getJsonString(EsConf.getConf.query_mapping)
    println(str1)
    println(str2)
    println(str3)
  }
}
