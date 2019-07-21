package io.transwarp.aiops.jstack.conf

import java.util.regex.Pattern

import com.fasterxml.jackson.annotation.{JsonIgnoreProperties, JsonProperty}
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import io.transwarp.aiops.Component.Component
import io.transwarp.aiops.{ComponentKeyDeserializer, ConfBean}

@JsonIgnoreProperties(ignoreUnknown = true)
case class JstackConfBean(@JsonProperty("stack_trace")
                          @JsonDeserialize(keyUsing = classOf[ComponentKeyDeserializer])
                          stackTraceCompMap: Map[Component, BlackWhiteList],
                          @JsonProperty("thread_group") threadGroup: BlackWhiteList,
                          @JsonProperty("thread_group_min_size") threadGroupMinSize: Int,
                          @JsonProperty("thread_group_name_min_length") threadGroupNameMinLength: Int) extends ConfBean

case class BlackWhiteList(@JsonProperty("whiteList") whiteList: Array[Pattern],
                          @JsonProperty("blackList") blackList: Array[Pattern])
