package io.transwarp.aiops.plain.service

import io.transwarp.aiops.plain.dao.PlainDocDao

object Test {
  def main(args: Array[String]): Unit = {
    val res = PlainDocDao.searchByCondition(0L,
      1537286400000L,
      "tos",
      0,
      30)
    res.result.foreach(f => println(s"${f.timestamp}: ${f.component}"))
  }
}
