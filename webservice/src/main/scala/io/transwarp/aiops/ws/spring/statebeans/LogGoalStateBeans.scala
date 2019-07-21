package io.transwarp.aiops.ws.spring.statebeans

import io.transwarp.aiops.log.loader.InceptorFilterState
import org.springframework.context.annotation.Bean

import scala.beans.BeanProperty

class LogStateBeans

@Bean
case class InceptorLogGoalStateBeans(@BeanProperty goalID: String
                                     , @BeanProperty date: Long = -1) extends LogStateBeans {
  def this() = {
    this(goalID = null)
  }
}

@Bean
case class InceptorLogGoalTimeLineStateBeans(@BeanProperty goalID: String, @BeanProperty date: Long = -1,
                                             @BeanProperty from: Int = 0, @BeanProperty to: Int = 0) extends LogStateBeans {
  def this() = {
    this(goalID = null)
  }
}

@Bean
case class InceptorTimelineBeans(@BeanProperty date: Long = -1,
                                 @BeanProperty startIndex: Int = 0,
                                 @BeanProperty endIndex: Int = 0) extends InceptorFilterStateBeans {
  def this() = {
    this(date = -1)
  }

  def same(o: InceptorFilterState): Boolean = {
    startTime == o.startTime && endTime == o.endTime && smartType == o.smartType && goalType == o.goalType &&
      minDuration == o.minDuration && maxDuration == o.maxDuration && sortBy == o.sortBy && order == o.order
  }

  def toFilterState: InceptorFilterState = InceptorFilterState(startTime, endTime, smartType, goalType, minDuration, maxDuration, sortBy, order)
}

@Bean
case class InceptorSessionBeans(@BeanProperty date: Long = -1) extends LogStateBeans {
  def this() = {
    this(date = -1)
  }
}

@Bean
case class InceptorFlatGoalsBeans(@BeanProperty date: Long = -1, @BeanProperty from: Int = 0,
                                  @BeanProperty to: Int = 0) extends InceptorFilterStateBeans {
  def this() = {
    this(date = -1)
  }

  def toInceptorFilterState: InceptorFilterState = {
    InceptorFilterState(startTime, endTime, smartType, goalType, minDuration, maxDuration, sortBy, order)
  }

  def same(o: InceptorFilterState): Boolean = {
    startTime == o.startTime && endTime == o.endTime && smartType == o.smartType && goalType == o.goalType &&
      minDuration == o.minDuration && maxDuration == o.maxDuration && sortBy == o.sortBy && order == o.order
  }
}

/**
  * @param smartType 0: all; 1: smart type;
  * @param goalType  four bit from 0 to 15
  *                  1000: compile error
  *                  0100: complete success
  *                  0010: complete error
  *                  0001: incomplete
  * @param sortBy    Goal sort:
  *                  0: sort by start time;
  *                  1: sort by end time;
  *                  2: sort by duration;
  *                  Session sort:
  *                  3: sort by max duration;
  *                  4: sort by average duration;
  *                  5: sort by exception number;
  * @param order     true: ascend; false: descend;
  */
@Bean
class InceptorFilterStateBeans(@BeanProperty val startTime: Long = -1,
                               @BeanProperty val endTime: Long = -1,
                               @BeanProperty val smartType: Int = 0,
                               @BeanProperty val goalType: Int = -1,
                               @BeanProperty val minDuration: Long = -1,
                               @BeanProperty val maxDuration: Long = -1,
                               @BeanProperty val sortBy: Int = 0,
                               @BeanProperty val order: Boolean = true) extends LogStateBeans {
  def this() = {
    this(startTime = -1)
  }
}

object SortByType extends Enumeration {
  type SortByType = Value
  val ByStart = Value(0)
  val ByEnd = Value(1)
  val ByDuration = Value(2)
  val ByMaxDur = Value(3)
  val ByAvgDur = Value(4)
  val ByExcpNum = Value(5)
}
