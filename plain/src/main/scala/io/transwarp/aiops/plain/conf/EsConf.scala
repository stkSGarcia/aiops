package io.transwarp.aiops.plain.conf

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import io.transwarp.aiops.AIOpsConf.ConfField
import io.transwarp.aiops.Conf

object EsConf extends Conf(ConfField.ELASTICSEARCH, classOf[EsConfBean]) {
  private val mapper = (new ObjectMapper).registerModule(DefaultScalaModule)

  def getJsonString(data: Any): String = mapper writeValueAsString data
}
