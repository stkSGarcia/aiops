package io.transwarp.aiops.ws.config

import org.springframework.context.annotation.{Bean, Configuration}
import springfox.documentation.builders.{ApiInfoBuilder, PathSelectors, RequestHandlerSelectors}
import springfox.documentation.service.ApiInfo
import springfox.documentation.spi.DocumentationType
import springfox.documentation.spring.web.plugins.Docket
import springfox.documentation.swagger2.annotations.EnableSwagger2

/**
  * Created by hippo on 9/5/18.
  */
@Configuration
@EnableSwagger2
//@ConditionalOnProperty(name = Array("swagger.enabled"), havingValue = "true")
class SwaggerConfig {
  @Bean
  def swaggerPlugins: Docket = {
    new Docket(DocumentationType.SWAGGER_2)
      .apiInfo(apiInfo)
      .select()
      .apis(RequestHandlerSelectors.basePackage("io.transwarp.aiops.ws.controller"))
      .paths(PathSelectors.any())
      .build()
  }

  private def apiInfo: ApiInfo = {
    new ApiInfoBuilder()
      .title("AIOps RESTful APIs")
      .description("AIOps Address：http://172.16.140.229:8008/")
      .contact("Transwarp")
      .version("1.0.0")
      .build()
  }

}
