package io.transwarp.aiops.ws.config

import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.config.annotation.{ViewControllerRegistry, WebMvcConfigurerAdapter}

@Configuration
class WebConfiguration extends WebMvcConfigurerAdapter {
  override def addViewControllers(registry: ViewControllerRegistry): Unit = {
    registry.addViewController("/{spring:\\w+}")
      .setViewName("forward:/")
    registry.addViewController("/**/{spring:\\w+}")
      .setViewName("forward:/")
    registry.addViewController("/{spring:\\w+}/**{spring:?!(\\.js|\\.css)$}")
      .setViewName("forward:/")
  }
}
