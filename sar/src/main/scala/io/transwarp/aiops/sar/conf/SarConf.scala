package io.transwarp.aiops.sar.conf

import java.io.FileInputStream

import io.transwarp.aiops.AIOpsConf.ConfField
import io.transwarp.aiops.Conf

import scala.io.Source

object SarConf extends Conf(ConfField.SAR, classOf[SarConfBean]) {
  // FIXME: Find a better way to get config file.
  def configFile: Array[String] = Source.fromInputStream(
    getConf.configFilePath match {
      case "<default>" => getClass.getClassLoader.getResourceAsStream("sar-centers.csv")
      case path => new FileInputStream(path)
    },
    "UTF-8"
  ).getLines.toArray
}
