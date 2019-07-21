package io.transwarp.aiops.plain.util

import java.util.Calendar

object ProcedureUtil {
  def getDate = {
    val c = Calendar.getInstance()
    c.setTimeInMillis(System.currentTimeMillis())
    val yy = c.get(Calendar.YEAR)
    val mm = c.get(Calendar.MONTH) + 1
    val dd = c.get(Calendar.DAY_OF_MONTH)
    "%s-%s-%s".format(yy, mm, dd)
  }
}
