package io.transwarp.aiops.ws

import java.text.SimpleDateFormat
import java.util
import java.util.{Calendar, Properties}

import com.fasterxml.jackson.databind.Module
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import io.transwarp.aiops.{AIOpsConf, ConfField}
import javax.servlet.http.{HttpSessionEvent, HttpSessionListener}
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.context.annotation.Bean
import org.springframework.scheduling.annotation.EnableAsync

import scala.collection.JavaConverters._

//@EnableSwagger2
@EnableAsync
@SpringBootApplication
class WebServiceConfiguration {
  val timeformat = new SimpleDateFormat("HH:mm:ss")

  @Bean
  def jacksonModuleScala: Module = new DefaultScalaModule

  @Bean
  // bean for http session listener
  def httpSessionListener: HttpSessionListener = {
    val listener = new HttpSessionListener {
      override def sessionCreated(se: HttpSessionEvent): Unit = {
        val now = Calendar.getInstance().getTime
        val timeString = timeformat.format(now)
        System.out.println("Session Created with session id+" + se.getSession.getId + " time: " + timeString)
      }

      override def sessionDestroyed(se: HttpSessionEvent): Unit = {
        // session
        // destroyed
        val now = Calendar.getInstance().getTime
        val timeString = timeformat.format(now)
        System.out.println("Session Destroyed, Session id:" + se.getSession.getId + " time: " + timeString)
        // CacheMap.clearSessionCache(se.getSession.getId)
      }
    }
    listener
  }
}

object WebService {
  def main(args: Array[String]): Unit = {
    // FIXME Fix the following line
    System.setProperty("es.set.netty.runtime.available.processors", "false")

    val app = new SpringApplication(classOf[WebServiceConfiguration])
    val properties = new Properties
    AIOpsConf.config.get(ConfField.SPRINGBOOT).asInstanceOf[util.LinkedHashMap[String, Any]].asScala
      .foreach { case (key, value) => properties.setProperty(key, String.valueOf(value)) }
    app.setDefaultProperties(properties)
    app.run()
  }
}
