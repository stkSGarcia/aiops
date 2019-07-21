package io.transwarp.aiops.jstack.structure

case class JstackResponse(fileInfo: Array[JstackFileInfo], totalInfo: JstackFileInfo = null)

case class BlackWhiteListResponse(whiteList: Array[String], blackList: Array[String])

case class StackTraceComponentsResponse(components: Array[String])
