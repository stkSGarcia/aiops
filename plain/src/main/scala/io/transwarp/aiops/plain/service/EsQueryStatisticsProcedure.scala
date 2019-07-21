package io.transwarp.aiops.plain.service

import io.transwarp.aiops.plain.dao.{DaoRes, PlainDocDao}
import io.transwarp.aiops.plain.model.{ResStatus, State, StatisticsState}

class EsQueryStatisticsProcedure extends Procedure {
  override def work(s: State): Unit = {
    val state = s.asInstanceOf[StatisticsState]
    val statRes = PlainDocDao.queryStatistics(state.startTime, state.endTime)
    statRes.status match {
      case DaoRes.Success =>
        state.status = ResStatus.SUCCESS
        state.result.totalQueries = statRes.totalQueries
        state.result.intervalQueries = statRes.intervalQueries
        state.result.topAnswers = statRes.topAnswers
        state.result.topQueries = statRes.topQueries
        LOG.info(s"[ES] get query statistics info successfully [Time span: ${state.startTime}, ${state.endTime}].")
      case DaoRes.Failure =>
        state.status = ResStatus.FAILURE
        state.errMsg = s"[ES] get query statistics info failed [Time span: ${state.startTime}, ${state.endTime}]."
        LOG.error(s"[ES] get query statistics info failed [Time span: ${state.startTime}, ${state.endTime}].")
    }
  }
}
