package io.transwarp.aiops.jstack.parser

import org.scalatest.FunSuite

class EntryParserSuite extends FunSuite {
  test("Entry parser") {
    val parser = EntryParser.fromInputStream(getClass.getClassLoader.getResourceAsStream("jstack001"))
    parser.iterator.foreach(l => {
      println("=====================")
      println(l)
    })
  }
}
