package io.transwarp.aiops.ws.controller.request

import io.transwarp.aiops.jstack.conf.BlackWhiteList
import org.springframework.context.annotation.Bean

@Bean
case class BlackWhiteSaveReq(blackWhiteList: BlackWhiteList) extends WebRequest
