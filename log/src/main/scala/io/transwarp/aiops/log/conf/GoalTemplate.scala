package io.transwarp.aiops.log.conf

import java.util
import java.util.regex.Pattern
import scala.beans.BeanProperty
import io.transwarp.aiops.log.message.LogEntity

class GoalTemplate(@BeanProperty val component: String = null, @BeanProperty val id: Int = -1, @BeanProperty val name: String = null,
                   @BeanProperty val desc: String = null, @BeanProperty val pre: String = null, @BeanProperty val post: String = null,
                   @BeanProperty val taskList: util.ArrayList[TaskTemplate] = null) {
  val globalID = component +":" +  id
  @transient val prePattern = Pattern.compile(pre)
  @transient val postPattern = Pattern.compile(post)

  def satisfyPre(logEntity: LogEntity): Boolean = {
    satisfyRegex(logEntity, prePattern)
  }

  def satisfyPost(logEntity: LogEntity): Boolean = {
    satisfyRegex(logEntity, postPattern)
  }

  private def satisfyRegex(logEntity: LogEntity, regex: Pattern): Boolean = {
    val matcher = regex.matcher(logEntity.content)
    matcher.find
  }

}

class TaskTemplate(@BeanProperty val id: Int, @BeanProperty val name: String = null, @BeanProperty val desc: String = null,
                   @BeanProperty val pre: String = null, @BeanProperty val occur: String = null, @BeanProperty val post: String = null,
                   @BeanProperty val optional: Boolean = false,
                   @BeanProperty val taskList: util.ArrayList[TaskTemplate] = null) {

  private val prePattern = Pattern.compile(pre)
  private val occurPattern = if (occur == null) null else Pattern.compile(occur)
  private val postPattern = Pattern.compile(post)


  @transient var level = -1
  var globalID: String = null
  @transient var superTask: TaskTemplate = null
  @transient val preLocator = PatternLocator(prePattern, this, TaskMark.PRE)
  @transient val postLocator = PatternLocator(postPattern, this, TaskMark.POST)

  def setSuperTaskAndGlobalID(taskTemplate: TaskTemplate): Unit = {
    if (superTask != null || globalID != null) {
      throw new RuntimeException("Super Task and Global ID Can't be set twice")
    }
    superTask = taskTemplate
    globalID = if(superTask == null) s"${id}" else (superTask.globalID + ":" + id)
  }

  def setLevel(newLevel: Int): Unit = {
    if (newLevel < 0) {
      throw new RuntimeException("Illegal level: level must be larger or equal to 0")
    }

    if (level != -1) {
      throw new RuntimeException("Task Level Can't be set twice")
    } else {
      level = newLevel
    }
  }

  def satisfyPre(logEntity: LogEntity): Boolean = {
    satisfyRegex(logEntity, prePattern)
  }

  def satisfyOccur(logEntity: LogEntity): Boolean = {
    satisfyRegex(logEntity, occurPattern)
  }

  def satisfyPost(logEntity: LogEntity): Boolean = {
    satisfyRegex(logEntity, postPattern)
  }

  private def satisfyRegex(logEntity: LogEntity, regex: Pattern): Boolean = {
    val matcher = regex.matcher(logEntity.content)
    matcher.find
  }

}
