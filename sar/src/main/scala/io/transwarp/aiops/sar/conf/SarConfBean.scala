package io.transwarp.aiops.sar.conf

import com.fasterxml.jackson.annotation.{JsonIgnoreProperties, JsonProperty}
import io.transwarp.aiops.ConfBean

@JsonIgnoreProperties(ignoreUnknown = true)
case class SarConfBean(@JsonProperty("config_dir") configFilePath: String) extends ConfBean
