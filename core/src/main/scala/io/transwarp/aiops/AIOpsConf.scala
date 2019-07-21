package io.transwarp.aiops

import java.io.{FileInputStream, InputStream}
import java.nio.file.{Files, Paths}

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import io.transwarp.aiops.AIOpsConf.ConfField.ConfField
import org.apache.hadoop.conf.Configuration

import scala.util.{Failure, Success, Try}

object AIOpsConf extends Logger {
  private val yamlMapper = new ObjectMapper(new YAMLFactory).registerModule(DefaultScalaModule)
  // FIXME: Don't use hdfs-site.xml
  // The following code is for plain module
  var hdfsConf: Configuration = _ // new Configuration
  private var config: Map[String, Any] = _

  reload()

  /**
    * Reload all configurations.
    */
  def reload(): Unit = {
    // The following code is for plain module
    // Configuration.addDefaultResource("hdfs-site.xml")
    config = loadConf(getClass.getClassLoader.getResourceAsStream("default-config.yml")) match {
      case Some(defaultConf) =>
        val customConfPath = System.getProperty("external.config")
        Option(customConfPath).filter(p => Files.isRegularFile(Paths.get(p)))
          .flatMap(f => loadConf(new FileInputStream(f))) match {
          case Some(customConf) =>
            LOG.info("Custom configuration file loaded: {}", customConfPath)
            defaultConf ++ customConf
          case None =>
            LOG.warn("Cannot find external config file: {}", customConfPath)
            defaultConf
        }
      case None => throw new RuntimeException("Cannot load default configurations")
    }
  }

  /**
    * Load a configuration file from a source.
    *
    * @param src The inputStream of the source
    * @return Configurations
    */
  private def loadConf(src: InputStream): Option[Map[String, Any]] =
    Try(yamlMapper.readValue(src, classOf[Map[String, Any]])) match {
      case Success(value) => Option(value)
      case Failure(_) =>
        LOG.error("Config yaml parsing error")
        None
    }

  /**
    * Get module specific configurations.
    *
    * @param field Module name
    * @return Configurations
    */
  def getConfWithName(field: ConfField): Option[Map[String, Any]] =
    config.get(field.toString).map(_.asInstanceOf[Map[String, Any]])

  def getMapper: ObjectMapper = yamlMapper

  object ConfField extends Enumeration {
    type ConfField = Value
    val SPRINGBOOT = Value("springboot")
    val ELASTICSEARCH = Value("elasticsearch")
    val SAR = Value("sar")
    val LOG = Value("log")
    val JSTACK = Value("jstack")
  }

}
