package io.transwarp.aiops.ws.feature.log

import io.transwarp.aiops.Component
import io.transwarp.aiops.log.loader.LogCacheType.Goals
import io.transwarp.aiops.log.loader.{DateInfo, LogCacheType}
import io.transwarp.aiops.log.message._
import io.transwarp.aiops.ws.spring.controller._
import io.transwarp.aiops.ws.spring.statebeans.SortByType.SortByType
import io.transwarp.aiops.ws.spring.statebeans._
import io.transwarp.aiops.ws.spring.util.WebUtils
import org.joda.time.{DateTimeZone, LocalDate, LocalTime}

import scala.collection.mutable.{ArrayBuffer, HashMap, PriorityQueue}
import scala.util.Sorting

object InceptorLogServices {
  val comp = Component.INCEPTOR

  def date(implicit sessionId: String): LogResponse = CacheMap.getLogCache(sessionId, comp).map(logCache => {
    val res = logCache.keySet
      .flatMap(date => {
        logCache.get(date, LogCacheType.InceptorDateInfo).map(infoMap =>
          DateBean(date.toDateTime(new LocalTime(0, DateTimeZone.UTC), DateTimeZone.UTC).getMillis,
            infoMap(DateInfo.ERROR),
            infoMap(DateInfo.LONG_DURATION),
            infoMap(DateInfo.NORMAL)))
      }).to[ArrayBuffer].sortBy(_.date)
    InceptorDateResponse(res)
  }).getOrElse(InceptorDateResponse(ArrayBuffer()))

  def timeLine(bean: InceptorTimelineBeans)(implicit sessionId: String): LogResponse = {
    val targetDate = new LocalDate(bean.date)
    val start = bean.startIndex
    val end = bean.endIndex
    CacheMap.logGet(sessionId, comp, targetDate, LogCacheType.InceptorSessionFilterState) match {
      case Some(x) if bean same x =>
        val cache = CacheMap.logGetAndCheck(sessionId, comp, targetDate, LogCacheType.InceptorSessionFilter)
        InceptorTimelineResponse(cache.goalsBySession.slice(start, end), cache.size)
      case _ =>
        val cache = sortTimeline(targetDate, SortByType(bean.sortBy), bean.order)
        val goalBeanMap = CacheMap.logGetAndCheck(sessionId, comp, targetDate, LogCacheType.InceptorGoalBeanMap)

        val data = cache
          .map { case (id, goals) =>
            val filteredGoals = goals.filter(filter(_, bean)).map(f => goalBeanMap(f.logGoal.id.toString).toGoalTaskThinBean)
            (id, filteredGoals)
          }
          .filter { case (_, goals) => goals.nonEmpty }
          .map { case (id, goals) => SessionGoalBean(id, goals.toArray) }
        CacheMap.logPut(sessionId, comp, targetDate, LogCacheType.InceptorSessionFilter, InceptorTimelineResponse(data, data.size))
        CacheMap.logPut(sessionId, comp, targetDate, LogCacheType.InceptorSessionFilterState, bean.toFilterState)
        InceptorTimelineResponse(data.slice(start, end), data.size)
    }
  }

  private def sortTimeline(date: LocalDate, sortType: SortByType, order: Boolean)(implicit sessionId: String): ArrayBuffer[(String, LogCacheType.Goals)] = {

    def data: ArrayBuffer[(String, Long, Double, Int, LogCacheType.Goals)] =
      CacheMap.logGet(sessionId, comp, date, LogCacheType.InceptorSessionSort).getOrElse {
        val goalBySession = CacheMap.logGetAndCheck(sessionId, comp, date, LogCacheType.InceptorGoalBySession)
        val cache = goalBySession.map { case (id, goals) =>
          var maxDur = 0L
          var totalDur = 0L
          var excpNum = 0
          goals.foreach(goal => {
            val duration = goal.duration.getMillis
            if (duration > maxDur) maxDur = duration
            totalDur += duration
            if (goal.flag != GoalStatus.COMPLETE_SUCCESS) excpNum += 1
          })
          (id, maxDur, totalDur.toDouble / goals.size.toDouble, excpNum, goals)
        }.to[ArrayBuffer]
        CacheMap.logPut(sessionId, comp, date, LogCacheType.InceptorSessionSort, cache)
        cache
      }

    sortType match {
      case SortByType.ByMaxDur =>
        if (!order) {
          CacheMap.logGet(sessionId, comp, date, LogCacheType.InceptorSessionSortByMaxDurDesc).getOrElse {
            CacheMap.logGet(sessionId, comp, date, LogCacheType.InceptorSessionSortByMaxDurAsc) match {
              case Some(someCache) =>
                val reverseCache: ArrayBuffer[(String, LogCacheType.Goals)] = new ArrayBuffer[(String, Goals)]()
                for (i <- (0 until someCache.length).reverse) {
                  reverseCache.append(someCache(i))
                }
                CacheMap.logPut(sessionId, comp, date, LogCacheType.InceptorSessionSortByMaxDurDesc, reverseCache)
                reverseCache

              case None =>
                val ordering: Ordering[Long] = if (order) Ordering[Long] else Ordering[Long].reverse
                val cache = data.sortBy(_._2)(ordering).map { case (id, _, _, _, beans) => (id, beans) }
                CacheMap.logPut(sessionId, comp, date, LogCacheType.InceptorSessionSortByMaxDurDesc, cache)
                cache
            }
          }
        } else {
          CacheMap.logGet(sessionId, comp, date, LogCacheType.InceptorSessionSortByMaxDurAsc).getOrElse {
            val cache: ArrayBuffer[(String, LogCacheType.Goals)] = CacheMap.logGet(sessionId, comp, date, LogCacheType.InceptorSessionSortByMaxDurDesc).get
            val reverseCache: ArrayBuffer[(String, LogCacheType.Goals)] = new ArrayBuffer[(String, Goals)]()
            for (i <- (0 until cache.length).reverse) {
              reverseCache.append(cache(i))
            }
            CacheMap.logPut(sessionId, comp, date, LogCacheType.InceptorSessionSortByMaxDurAsc, reverseCache)
            reverseCache
          }
        }

      case SortByType.ByAvgDur =>
        if (!order) {
          CacheMap.logGet(sessionId, comp, date, LogCacheType.InceptorSessionSortByAvgDurDesc).getOrElse {
            CacheMap.logGet(sessionId, comp, date, LogCacheType.InceptorSessionSortByAvgDurAsc) match {
              case Some(someCache) =>
                val reverseCache: ArrayBuffer[(String, LogCacheType.Goals)] = new ArrayBuffer[(String, Goals)]()
                for (i <- (0 until someCache.length).reverse) {
                  reverseCache.append(someCache(i))
                }
                CacheMap.logPut(sessionId, comp, date, LogCacheType.InceptorSessionSortByAvgDurDesc, reverseCache)
                reverseCache

              case None =>
                val ordering: Ordering[Double] = if (order) Ordering[Double] else Ordering[Double].reverse
                val cache = data.sortBy(_._3)(ordering).map { case (id, _, _, _, beans) => (id, beans) }
                CacheMap.logPut(sessionId, comp, date, LogCacheType.InceptorSessionSortByAvgDurDesc, cache)
                cache
            }

          }
        } else {
          CacheMap.logGet(sessionId, comp, date, LogCacheType.InceptorSessionSortByAvgDurAsc).getOrElse {
            CacheMap.logGet(sessionId, comp, date, LogCacheType.InceptorSessionSortByAvgDurDesc) match {
              case Some(someCache) =>
                val reverseCache: ArrayBuffer[(String, LogCacheType.Goals)] = new ArrayBuffer[(String, Goals)]()
                for (i <- (0 until someCache.length).reverse) {
                  reverseCache.append(someCache(i))
                }
                CacheMap.logPut(sessionId, comp, date, LogCacheType.InceptorSessionSortByAvgDurAsc, reverseCache)
                reverseCache

              case None =>
                val ordering: Ordering[Int] = if (order) Ordering[Int] else Ordering[Int].reverse
                val cache = data.sortBy(_._4)(ordering).map { case (id, _, _, _, beans) => (id, beans) }
                CacheMap.logPut(sessionId, comp, date, LogCacheType.InceptorSessionSortByAvgDurAsc, cache)
                cache
            }
          }
        }

      case SortByType.ByExcpNum =>
        if (!order) {
          CacheMap.logGet(sessionId, comp, date, LogCacheType.InceptorSessionSortByExcpNumDesc).getOrElse {
            CacheMap.logGet(sessionId, comp, date, LogCacheType.InceptorSessionSortByExcpNumAsc) match {
              case Some(someCache) =>
                val reverseCache: ArrayBuffer[(String, LogCacheType.Goals)] = new ArrayBuffer[(String, Goals)]()
                for (i <- (0 until someCache.length).reverse) {
                  reverseCache.append(someCache(i))
                }
                CacheMap.logPut(sessionId, comp, date, LogCacheType.InceptorSessionSortByExcpNumDesc, reverseCache)
                reverseCache

              case None =>
                val ordering: Ordering[Int] = if (order) Ordering[Int] else Ordering[Int].reverse
                val cache = data.sortBy(_._4)(ordering).map { case (id, _, _, _, beans) => (id, beans) }
                CacheMap.logPut(sessionId, comp, date, LogCacheType.InceptorSessionSortByExcpNumDesc, cache)
                cache
            }

          }
        } else {
          CacheMap.logGet(sessionId, comp, date, LogCacheType.InceptorSessionSortByExcpNumAsc).getOrElse {
            CacheMap.logGet(sessionId, comp, date, LogCacheType.InceptorSessionSortByExcpNumDesc) match {
              case Some(someCache) =>
                val reverseCache: ArrayBuffer[(String, LogCacheType.Goals)] = new ArrayBuffer[(String, Goals)]()
                for (i <- (0 until someCache.length).reverse) {
                  reverseCache.append(someCache(i))
                }
                CacheMap.logPut(sessionId, comp, date, LogCacheType.InceptorSessionSortByExcpNumAsc, reverseCache)
                reverseCache

              case None =>
                val ordering: Ordering[Int] = if (order) Ordering[Int] else Ordering[Int].reverse
                val cache = data.sortBy(_._4)(ordering).map { case (id, _, _, _, beans) => (id, beans) }
                CacheMap.logPut(sessionId, comp, date, LogCacheType.InceptorSessionSortByExcpNumAsc, cache)
                cache
            }
          }

        }
    }
  }

  def session(bean: InceptorSessionBeans)(implicit sessionId: String): LogResponse = {
    val targetDate = new LocalDate(bean.date)
    val inceptorGoalBySession = CacheMap.logGetAndCheck(sessionId, comp, targetDate, LogCacheType.InceptorGoalBySession)
    val res = inceptorGoalBySession.map { case (id, goals) =>
      SessionInfoBean(id, goals.head.startTime.getMillis, goals.last.endTime.getMillis)
    }.to[ArrayBuffer]
    InceptorSessionResponse(res, res.minBy(_.startTime).startTime, res.maxBy(_.endTime).endTime)
  }

  //  def flatGoals(sessionID: String, inceptorFlatGoalsBeans: InceptorFlatGoalsBeans): LogResponse = {
  //    val date = new LocalDate(inceptorFlatGoalsBeans.date)
  //    val from = inceptorFlatGoalsBeans.from
  //    val to = inceptorFlatGoalsBeans.to
  //
  //    val goalSortByStart = sortByStart(sessionID, date)
  //    val goalBeanMap = CacheMap.logGetAndCheck(sessionID, comp, date, LogCacheType.InceptorGoalBeanMap)
  //    val beanSortByStart = MsgTransferUtil.inceptorGoalArray2ThinBeans(goalSortByStart, from, to, goalBeanMap)
  //
  //    InceptorFlatGoalsResponse(beanSortByStart)
  //  }

  def flatGoals(sessionID: String, inceptorFlatGoalsBeans: InceptorFlatGoalsBeans): LogResponse = {
    val date = new LocalDate(inceptorFlatGoalsBeans.date)
    val from = inceptorFlatGoalsBeans.from
    val to = inceptorFlatGoalsBeans.to

    CacheMap.logGet(sessionID, comp, date, LogCacheType.InceptorFlatGoalFilter) match {
      case Some(x) if inceptorFlatGoalsBeans same x._1 => {
        val (_, goalArray) = x
        InceptorFlatGoalsResponse(goalArray.slice(from, to), goalArray.size)
      }
      case _ => {
        val goalArray = flatGoalsFilter(sessionID, inceptorFlatGoalsBeans)
        CacheMap.logPut(sessionID, comp, date, LogCacheType.InceptorFlatGoalFilter,
          (inceptorFlatGoalsBeans.toInceptorFilterState, goalArray))
        InceptorFlatGoalsResponse(goalArray.slice(from, to), goalArray.size)

      }
    }
  }


  private def flatGoalsFilter(sessionID: String, inceptorFlatGoalsBeans: InceptorFlatGoalsBeans): Array[GoalTaskThinBean]

  = {
    val sortType = SortByType(inceptorFlatGoalsBeans.sortBy)
    val date = new LocalDate(inceptorFlatGoalsBeans.date)

    val sortedGoalsCache = sortType match {
      case SortByType.ByStart => {
        CacheMap.logGet(sessionID, comp, date, LogCacheType.InceptorGoalSortByStart)
      }
      case SortByType.ByEnd => {
        CacheMap.logGet(sessionID, comp, date, LogCacheType.InceptorGoalSortByEnd)
      }
      case SortByType.ByDuration => {
        CacheMap.logGet(sessionID, comp, date, LogCacheType.InceptorGoalSortByDuration)
      }
    }

    def needReverse() = {
      (sortType == SortByType.ByStart && inceptorFlatGoalsBeans.order == false) || (sortType == SortByType.ByEnd
        && inceptorFlatGoalsBeans.order == false) || (sortType == SortByType.ByDuration && inceptorFlatGoalsBeans.order ==
        true)
    }

    // if cache found, get sorted data first, filter next
    // else, filter first, sort data next
    val filtered = if (sortedGoalsCache.isEmpty) {
      // TODO: Double check this part
      val goals = CacheMap.logGetAndCheck(sessionID, comp, date, LogCacheType.InceptorGoalSortByEnd).toArray

      val sortedGoals = sortType match {
        case SortByType.ByStart => {
          val start = sortByStart(goals)
          CacheMap.logPut(sessionID, comp, date, LogCacheType.InceptorGoalSortByStart, start.to[ArrayBuffer])
          start
        }
        case SortByType.ByEnd => {
          goals
        }
        case SortByType.ByDuration => {
          val duration = sortByDuration(goals)
          CacheMap.logPut(sessionID, comp, date, LogCacheType.InceptorGoalSortByDuration, duration.to[ArrayBuffer])
          duration
        }
      }
      val filteredGoals = sortedGoals.filter(filter(_, inceptorFlatGoalsBeans))

      if (needReverse) filteredGoals.reverse else filteredGoals
    } else {
      val cacheGoals = sortedGoalsCache.get.toArray
      val sortedGoals = if (needReverse) cacheGoals.reverse else cacheGoals
      sortedGoals.filter(filter(_, inceptorFlatGoalsBeans))
    }

    val goalBeanMap = CacheMap.logGetAndCheck(sessionID, comp, date, LogCacheType.InceptorGoalBeanMap)
    MsgTransferUtil.inceptorGoalArray2ThinBeans(filtered, 0, filtered.size, goalBeanMap)
  }

  def getGoalDetail(sessionID: String, goalBean: InceptorLogGoalStateBeans): LogResponse = {
    if (goalBean.goalID == null) throw new IllegalArgumentException("Can't find goal by id null")

    val date = new LocalDate(goalBean.date)
    val goalBeanMap = CacheMap.logGetAndCheck(sessionID, comp, date, LogCacheType.InceptorGoalBeanMap)
    val currGoal = goalBeanMap.get(goalBean.goalID).get
    InceptorGoalResponse(currGoal)
  }


  def getGoalTimeLine(sessionID: String, goalBean: InceptorLogGoalTimeLineStateBeans): LogResponse = {
    val date = new LocalDate(goalBean.date)

    val goalSessionSortByExcpNum = sortTimeline(date, SortByType.ByExcpNum, false)(sessionID).toArray
    val goalBeanMap = CacheMap.logGetAndCheck(sessionID, comp, date, LogCacheType.InceptorGoalBeanMap)

    val goalBySession = CacheMap.logGetAndCheck(sessionID, comp, date, LogCacheType.InceptorGoalBySession)

    def getSessionIDFromGoalID: String = {
      val endindex = goalBean.goalID.indexOf(":")
      goalBean.goalID.substring(0, endindex)
    }

    val goalSessionID = getSessionIDFromGoalID
    val targetSessionArray = goalBySession.get(goalSessionID).get.toArray
    val targetSessionTuple = SessionGoalBean(goalSessionID, MsgTransferUtil.inceptorGoalArray2ThinBeans(targetSessionArray, 0,
      targetSessionArray.size, goalBeanMap))

    val currPageSessionMap = goalSessionSortByExcpNum.slice(goalBean.from, goalBean.to)

    def pageSessionTransfer: Array[SessionGoalBean] = currPageSessionMap.map { case (id, goals) =>
      SessionGoalBean(id, MsgTransferUtil.inceptorGoalBuffer2ThinBeans(goals, 0, goals.size, goalBeanMap).toArray)
    }

    InceptorGoalTimeLineResponse(targetSessionTuple, pageSessionTransfer, goalSessionSortByExcpNum.size)
  }


  private def filter(goal: LogGoalWithTask[InceptorGoalID], filterBean: InceptorFilterStateBeans): Boolean

  = {

    def satisfyStartEnd: Boolean = {
      val satisfyStart = (filterBean.startTime < 0) || goal.startTime.isAfter(filterBean.startTime) || goal.startTime.isEqual(filterBean.startTime)
      val satisfyEnd = (filterBean.endTime < 0) || goal.endTime.isBefore(filterBean.endTime) || goal.endTime.isEqual(filterBean.endTime)
      satisfyStart && satisfyEnd
    }

    def satisfySmart: Boolean = {
      def isSmartGoal: Boolean = {
        goal.flag match {
          case GoalStatus.COMPLETE_SUCCESS => false
          case _ => true
        }
      }

      (filterBean.smartType == 0) || (filterBean.smartType == 1 && isSmartGoal)
    }

    def satisfyGoalType: Boolean = {
      WebUtils.decodeGoalType(filterBean.goalType).contains(goal.flag)
    }

    def satisfyDuration: Boolean = {
      val satisfyMin = (filterBean.minDuration < 0) || (goal.duration.getMillis >= filterBean.minDuration)
      val satisfyMax = (filterBean.maxDuration < 0) || (goal.duration.getMillis <= filterBean.maxDuration)
      satisfyMin && satisfyMax
    }

    satisfySmart && satisfyStartEnd && satisfyGoalType && satisfyDuration
  }


  private def sortByStart(sessionID: String, date: LocalDate): Array[LogGoalWithTask[InceptorGoalID]]

  = {
    val sortByStartCache = CacheMap.logGet(sessionID, comp, date, LogCacheType.InceptorGoalSortByStart)

    val goalSortByStart = sortByStartCache match {
      case None => {
        val goalBySession = CacheMap.logGetAndCheck(sessionID, comp, date, LogCacheType.InceptorGoalBySession)
        val gsbs = sortByStart(goalBySession)
        CacheMap.logPut(sessionID, comp, date, LogCacheType.InceptorGoalSortByStart, gsbs)
        gsbs
      }
      case Some(cache) => {
        cache
      }
    }
    goalSortByStart.toArray
  }

  private def sortByStart(goalBySession: HashMap[String, ArrayBuffer[LogGoalWithTask[InceptorGoalID]]]): ArrayBuffer[LogGoalWithTask[InceptorGoalID]]

  = {
    val res = new ArrayBuffer[LogGoalWithTask[InceptorGoalID]]
    val ordering = new Ordering[LogGoalWithTask[InceptorGoalID]] {
      override def compare(a: LogGoalWithTask[InceptorGoalID], b: LogGoalWithTask[InceptorGoalID]): Int = {
        if (a.logGoal.id.startTime isBefore b.logGoal.id.startTime) 1 else -1
      }
    }
    val minHeap = new PriorityQueue[LogGoalWithTask[InceptorGoalID]]()(ordering)
    val indexMap = new HashMap[String, Int]
    goalBySession.keySet.foreach(key => {
      val initElem = goalBySession.get(key).get
      minHeap.enqueue(initElem(0))
      indexMap += (key -> 1)
    })

    while (!minHeap.isEmpty) {
      val goal = minHeap.dequeue()
      res += goal
      val sessionID = goal.logGoal.id.sessionID
      val sessionGoals = goalBySession.get(sessionID).get
      val curIndex = indexMap.get(sessionID).get
      if (curIndex < sessionGoals.size) {
        minHeap.enqueue(sessionGoals(curIndex))
        indexMap.put(sessionID, curIndex + 1)
      }
    }
    res
  }


  private def sortByStart(goals: Array[LogGoalWithTask[InceptorGoalID]]): Array[LogGoalWithTask[InceptorGoalID]]

  = {
    val ascOrdering = new Ordering[LogGoalWithTask[InceptorGoalID]] {
      override def compare(a: LogGoalWithTask[InceptorGoalID], b: LogGoalWithTask[InceptorGoalID]): Int = {
        if (a.logGoal.id.startTime isBefore b.logGoal.id.startTime) -1 else 1
      }
    }
    val result = goals.clone
    Sorting.quickSort(result)(ascOrdering)
    result
  }

  private def sortByEnd(goals: Array[LogGoalWithTask[InceptorGoalID]]): Array[LogGoalWithTask[InceptorGoalID]]

  = {
    val ascOrdering = new Ordering[LogGoalWithTask[InceptorGoalID]] {
      override def compare(a: LogGoalWithTask[InceptorGoalID], b: LogGoalWithTask[InceptorGoalID]): Int = {
        if (a.logGoal.id.endTime isBefore b.logGoal.id.endTime) -1 else 1
      }
    }
    val result = goals.clone
    Sorting.quickSort(result)(ascOrdering)
    result
  }


  private def sortByDuration(goals: Array[LogGoalWithTask[InceptorGoalID]]): Array[LogGoalWithTask[InceptorGoalID]]

  = {
    val descOrdering = new Ordering[LogGoalWithTask[InceptorGoalID]] {
      override def compare(a: LogGoalWithTask[InceptorGoalID], b: LogGoalWithTask[InceptorGoalID]): Int = {
        if (a.duration.getMillis > b.duration.getMillis) -1 else 1
      }
    }
    val result = goals.clone
    Sorting.quickSort(result)(descOrdering)
    result
  }

}
