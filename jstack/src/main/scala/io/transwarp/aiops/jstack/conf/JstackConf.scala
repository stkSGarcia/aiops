package io.transwarp.aiops.jstack.conf

import io.transwarp.aiops.AIOpsConf.ConfField
import io.transwarp.aiops.jstack.structure.{BlackWhiteListResponse, StackTraceComponentsResponse}
import io.transwarp.aiops.{Component, Conf}

object JstackConf extends Conf(ConfField.JSTACK, classOf[JstackConfBean]) {
  def getStackTraceComponents: StackTraceComponentsResponse =
    StackTraceComponentsResponse(Option(getConf.stackTraceCompMap).map(_.keySet.map(_.toString).toArray).orNull)

  def getCallstackBlackWhiteList(compName: String): BlackWhiteListResponse =
    Option(getConf.stackTraceCompMap) match {
      case Some(conf) =>
        conf.get(Component withCaseIgnoreName compName) match {
          case Some(x) => BlackWhiteListResponse(x.whiteList.map(_.toString), x.blackList.map(_.toString))
          case None => BlackWhiteListResponse(null, null)
        }
      case None => BlackWhiteListResponse(null, null)
    }

  def getThreadGroupBlackWhiteList: BlackWhiteListResponse =
    BlackWhiteListResponse(getConf.threadGroup.whiteList.map(_.toString), getConf.threadGroup.blackList.map(_.toString))
}
