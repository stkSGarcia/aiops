package io.transwarp.aiops.ws.service.impl

import java.util.UUID

import io.transwarp.aiops.plain.factory.MinesFactory
import io.transwarp.aiops.plain.model._
import io.transwarp.aiops.ws.controller.converter.PlainConverter._
import io.transwarp.aiops.ws.controller.response._
import io.transwarp.aiops.ws.service.PlainService
import org.springframework.stereotype.Service

/**
  * Created by hippo on 9/3/18.
  */
@Service
class PlainServiceImpl extends PlainService {
  override def save(userName: String,
                    component: String,
                    problem: String,
                    solution: String): RestResponse.NullType = {
    val state = new SaveState
    state.id = UUID.randomUUID.toString
    state.timestamp = System.currentTimeMillis.toString
    state.userName = userName
    state.component = component
    state.problem = problem
    state.solution = solution
    MinesFactory.save.work(state)
    state.convert
  }

  override def update(id: String,
                      userName: String,
                      component: String,
                      problem: String,
                      solution: String): RestResponse.NullType = {
    val state = new SaveState
    state.id = id
    state.timestamp = System.currentTimeMillis.toString
    state.userName = userName
    state.component = component
    state.problem = problem
    state.solution = solution
    MinesFactory.update.work(state)
    state.convert
  }

  override def search(query: String): RestResponse[PlainSearchRes] = {
    val queryTime = System.currentTimeMillis
    val state = new SearchState
    state.query = query
    state.timestamp = queryTime
    MinesFactory.search.work(state)
    state.convert
  }

  override def searchByPage(query: String, page: Int, size: Int): RestResponse[PlainSearchByPageRes] = {
    val queryTime = System.currentTimeMillis
    val state = new SearchByPageState
    state.query = query
    state.timestamp = queryTime
    state.page = page
    state.size = size
    MinesFactory.searchByPage.work(state)
    state.convert
  }

  override def searchById(id: String): RestResponse[PlainSearchRes] = {
    val state = new SearchByIdState
    state.id = id
    MinesFactory.searchById.work(state)
    state.convert
  }

  override def searchByCondition(startTime: Long,
                                 endTime: Long,
                                 component: String,
                                 page: Int,
                                 size: Int): RestResponse[PlainSearchByPageRes] = {
    val state = new SearchByConditionState(startTime, endTime, component, page, size)
    MinesFactory.searchByCondition.work(state)
    state.convert
  }

  override def truncate: RestResponse.NullType = {
    val state = new State
    MinesFactory.esTruncate.work(state)
    state.convert
  }

  override def reload: RestResponse.NullType = {
    val state = new State
    MinesFactory.esReload.work(state)
    state.convert
  }

  override def delete(id: String): RestResponse.NullType = {
    val state = new DocIdState
    state.id = id
    MinesFactory.delete.work(state)
    state.convert
  }

  override def statistics(startTime: Long, endTime: Long): RestResponse[PlainStatisticsRes] = {
    val state = new StatisticsState(startTime, endTime)
    MinesFactory.statistics.work(state)
    state.convert
  }

  @Deprecated
  override def stats: RestResponse[PlainStatsRes] = {
    val state = new StatsState
    MinesFactory.stats.work(state)
    val data = new PlainStatsRes
    data.docAmount = state.result.docAmount
    data.docPerPerson = state.result.docPerPerson.map(item => PlainStatsPerson(item._1, item._2))
    data.docPerComp = state.result.docPerComp.map(item => PlainStatsComp(item._1, item._2))
    data.docPerDay = state.result.docPerDay
    data.docPerPersonPerDay = state.result.docPerPersonPerDay.map(item => PlainStatsPerson(item._1, item._2))
    data.shardNum = state.result.shardNum
    if (state.status == ResStatus.SUCCESS) {
      RestResponse(ResultHeadSet.success, data)
    } else {
      RestResponse(ResultHeadSet.failure, data)
    }
  }
}
