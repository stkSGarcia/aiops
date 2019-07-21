package io.transwarp.aiops.ws.spring.statebeans

import org.springframework.context.annotation.Bean

import scala.beans.BeanProperty

@Bean
case class EmptyStateBeans()


@Bean
case class MinesDeleteStateBeans(@BeanProperty var id: String)

@Bean
case class MinesSearchStateBeans(@BeanProperty var query: String, @BeanProperty var pageId: Int)

@Bean
case class MinesUpdateStateBeans(@BeanProperty var id: String, @BeanProperty var userName: String,
                                 @BeanProperty var component: String, @BeanProperty var problemDetails: String,
                                 @BeanProperty var solution: String)

@Bean
case class MinesSaveStateBeans(@BeanProperty var userName: String, @BeanProperty var component: String,
                               @BeanProperty var problemDetails: String, @BeanProperty var solution: String)