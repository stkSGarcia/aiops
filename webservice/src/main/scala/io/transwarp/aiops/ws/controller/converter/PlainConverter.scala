package io.transwarp.aiops.ws.controller.converter

import io.transwarp.aiops.Logger
import io.transwarp.aiops.plain.model._
import io.transwarp.aiops.ws.controller.response.RestResponse.NullType
import io.transwarp.aiops.ws.controller.response._

object PlainConverter extends Logger {

  implicit class State2Res[T <: State](state: T) {
    def convert: NullType = state.status match {
      case ResStatus.SUCCESS => RestResponse.NULL_SUCCESS
      case _ =>
        LOG.error(state.errMsg)
        RestResponse(ResultHead("000", state.errMsg), null)
    }
  }

  implicit class SearchState2Res(state: SearchState) {
    def convert: RestResponse[PlainSearchRes] = {
      state.status match {
        case ResStatus.SUCCESS =>
          RestResponse(ResultHeadSet.success, PlainSearchRes(state.result.suggestions))
        case _ =>
          LOG.error(state.errMsg)
          RestResponse(ResultHead("000", state.errMsg), null)
      }
    }
  }

  implicit class SearchByIdState2Res(state: SearchByIdState) {
    def convert: RestResponse[PlainSearchRes] = state.status match {
      case ResStatus.SUCCESS =>
        RestResponse(ResultHeadSet.success, PlainSearchRes(state.result.suggestions))
      case _ =>
        LOG.error(state.errMsg)
        RestResponse(ResultHead("000", state.errMsg), null)
    }
  }

  implicit class SearchByPageState2Res(state: SearchByPageState) {
    def convert: RestResponse[PlainSearchByPageRes] = state.status match {
      case ResStatus.SUCCESS =>
        RestResponse(ResultHeadSet.success,
          PlainSearchByPageRes(state.result.suggestions,
            state.result.page,
            state.result.total,
            state.result.size))
      case _ =>
        LOG.error(state.errMsg)
        RestResponse(ResultHead("000", state.errMsg), null)
    }
  }

  implicit class SearchByConditionState2Res(state: SearchByConditionState) {
    def convert: RestResponse[PlainSearchByPageRes] = state.status match {
      case ResStatus.SUCCESS =>
        RestResponse(ResultHeadSet.success,
          PlainSearchByPageRes(state.result.suggestions,
            state.result.page,
            state.result.total,
            state.result.size))
      case ResStatus.FAILURE =>
        LOG.error(state.errMsg)
        RestResponse(ResultHead("000", state.errMsg), null)
    }
  }

  implicit class StatisticsState2Res(state: StatisticsState) {
    def convert: RestResponse[PlainStatisticsRes] = {
      state.status match {
        case ResStatus.SUCCESS =>
          val data = new PlainStatisticsRes
          data.totalDocs = state.result.totalDocs
          data.compStats = state.result.compStats
          data.personStats = state.result.personStats.map(f => PlainPersonState(f._1, f._2))
          data.totalQueries = state.result.totalQueries
          data.intervalQueries = state.result.intervalQueries.map(f => PlainIntervalState(f._1, f._2))
          data.topAnswers = state.result.topAnswers
          data.topQueries = state.result.topQueries
          RestResponse(ResultHeadSet.success, data)
        case _ =>
          LOG.error(state.errMsg)
          RestResponse(ResultHead("000", state.errMsg), null)
      }
    }
  }

}
