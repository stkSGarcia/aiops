package io.transwarp.aiops.log.conf

import java.util
import java.util.regex.Pattern

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.{DeserializationContext, JsonDeserializer}
import io.transwarp.aiops.AIOpsConf.ConfField
import io.transwarp.aiops.Component.Component
import io.transwarp.aiops.{Component, Conf}
import org.joda.time.format.DateTimeFormat

object LogConf extends Conf(ConfField.LOG, classOf[LogConfBean]) {
  private var _goalCombosMap: util.HashMap[Component, GoalTempCombos] = _

  def goalCombosMap: util.HashMap[Component, GoalTempCombos] = {
    //    if (_goalCombosMap == null) {
    //      _goalCombosMap = new util.HashMap[Component, GoalTempCombos]()
    //      val mapper = new ObjectMapper
    //      config.get(LogField.MODEL_TEMPLATE).asInstanceOf[util.LinkedHashMap[String, Any]].asScala
    //        .foreach { case (compName, compTemplate) =>
    //          val component = Component.withCaseIgnoreName(compName)
    //          val template = JSON.parseObject(mapper.writeValueAsString(compTemplate), classOf[GoalTemplate])
    //          _goalCombosMap.put(component, new GoalTempCombos(template))
    //        }
    //    }
    _goalCombosMap
  }

  def identifyComponent(fileName: String): Component =
    getConf.compMap.find(fileName contains _._1).map(_._2).getOrElse(Component.UNKNOWN)
}

private[log] class LogFormatMapDeserializer extends JsonDeserializer[Map[Component, LogFormat]] {
  override def deserialize(jsonParser: JsonParser, deserializationContext: DeserializationContext): Map[Component, LogFormat] =
    jsonParser.getCodec.readValue(jsonParser, classOf[Map[String, Any]])
      .filter(Component exist _._1)
      .map { case (compName, compConfig) =>
        val component = Component withCaseIgnoreName compName
        component -> genLogFormat(component, compConfig)
      }

  def genLogFormat(component: Component, data: Any): LogFormat = {
    val compConfig = data.asInstanceOf[Map[String, Any]]
      .flatMap(f => (LogFormatField withNameOpt f._1).map(_ -> f._2)) // filter unknown fields

    val config = compConfig(LogFormatField.CONFIG).asInstanceOf[Map[String, Any]]
      .flatMap(f => (LogFormatField withNameOpt f._1).map(_ -> f._2)) // filter unknown fields
      .transform { case (k, v) => k match {
      case LogFormatField.TIME_FORMAT => DateTimeFormat.forPattern(v.toString)
      case LogFormatField.PATTERNS => v.asInstanceOf[List[String]].toArray
      case _ => v.toString
    }
    }

    val patternFields = config(LogFormatField.PATTERNS).asInstanceOf[Array[String]]

    val patterns = compConfig.filter(_._1 != LogFormatField.CONFIG)
      .transform { case (k, v) =>
        if (patternFields contains k.toString) Pattern.compile(v.toString)
        else v.asInstanceOf[List[String]].toArray

      }
    LogFormat(component, config, patterns)
  }
}

private[log] class ComponentMapDeserializer extends JsonDeserializer[Map[String, Component]] {
  override def deserialize(jsonParser: JsonParser, deserializationContext: DeserializationContext): Map[String, Component] =
    jsonParser.getCodec.readValue(jsonParser, classOf[Map[String, List[String]]])
      .map(_.swap)
      .flatMap { case (k, v) =>
        val comp = Component withCaseIgnoreName v
        k.map(_ -> comp).toMap
      }
}
