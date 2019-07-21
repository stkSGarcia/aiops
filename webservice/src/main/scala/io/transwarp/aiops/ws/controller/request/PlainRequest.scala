package io.transwarp.aiops.ws.controller.request

import org.springframework.context.annotation.Bean

/**
  * Created by hippo on 9/4/18.
  */
@Bean
case class PlainSaveReq(userName: String,
                        component: String,
                        problem: String,
                        solution: String) extends WebRequest

@Bean
case class PlainSearchReq(query: String) extends WebRequest

@Bean
case class PlainSearchByPageReq(query: String,
                                page: Int,
                                size: Int) extends WebRequest

@Bean
case class PlainUpdateReq(id: String,
                          userName: String,
                          component: String,
                          problem: String,
                          solution: String) extends WebRequest

@Bean
case class PlainSearchByConditionReq(startTime: Long,
                                     endTime: Long,
                                     component: String,
                                     page: Int,
                                     size: Int) extends WebRequest
