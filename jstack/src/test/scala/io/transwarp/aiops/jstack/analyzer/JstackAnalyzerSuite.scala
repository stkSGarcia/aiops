package io.transwarp.aiops.jstack.analyzer

import java.io.File

import org.scalatest.FunSuite

class JstackAnalyzerSuite extends FunSuite {
  test("Test JstackAnalyzer") {
    val fileList = Array("jstack001", "js-1.7").map(f => new File(getClass.getClassLoader.getResource(f).getPath))
    val analyzer = new JstackAnalyzer
    analyzer.analyze(fileList)
  }
}
