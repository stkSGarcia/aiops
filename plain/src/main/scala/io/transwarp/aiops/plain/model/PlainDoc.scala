package io.transwarp.aiops.plain.model

import io.transwarp.aiops.Component

import scala.beans.BeanProperty

class PlainDoc(var id: String,
               var timestamp: String,
               var userName: String,
               var component: String,
               var problem: String,
               var solution: String) {
  def setField(fieldName: String, fieldValue: String): Boolean = fieldName match {
    case PlainDocField.ID =>
      this.id = fieldValue
      true
    case PlainDocField.TIMESTAMP =>
      this.timestamp = fieldValue
      true
    case PlainDocField.USERNAME =>
      this.userName = fieldValue
      true
    case PlainDocField.COMPONENT =>
      if (Component.exist(fieldValue)) {
        this.component = fieldValue
        true
      } else false
    case PlainDocField.PROBLEM =>
      this.problem = fieldValue
      true
    case PlainDocField.SOLUTION =>
      this.solution = fieldValue
      true
  }
}

object PlainDocField {
  val ID: String = "_id"
  val TIMESTAMP: String = "timestamp"
  val USERNAME: String = "userName"
  val COMPONENT: String = "component"
  val COMPONENT_KEYWORD: String = "component.keyword"
  val PROBLEM: String = "problemDetails"
  val SOLUTION: String = "solution"
}

case class Doc(@BeanProperty timestamp: String,
               @BeanProperty userName: String,
               @BeanProperty component: String,
               @BeanProperty problemDetails: String,
               @BeanProperty solution: String)
