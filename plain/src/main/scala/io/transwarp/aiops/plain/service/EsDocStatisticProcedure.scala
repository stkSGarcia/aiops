package io.transwarp.aiops.plain.service

import io.transwarp.aiops.plain.dao.{DaoRes, PlainDocDao}
import io.transwarp.aiops.plain.model.{ResStatus, State, StatisticsState}

class EsDocStatisticProcedure extends Procedure {
  override def work(s: State): Unit = {
    val state = s.asInstanceOf[StatisticsState]
    val statRes = PlainDocDao.docStatistics(state.startTime, state.endTime)
    statRes.status match {
      case DaoRes.Success =>
        state.status = ResStatus.SUCCESS
        state.result.totalDocs = statRes.totalDocs
        state.result.compStats = statRes.compStats
        state.result.personStats = statRes.personStats
        LOG.info(s"[ES] get doc statistics info successfully [Time span: ${state.startTime}, ${state.endTime}].")
      case DaoRes.Failure =>
        state.status = ResStatus.FAILURE
        state.errMsg = s"[ES] get doc statistics info failed [Time span: ${state.startTime}, ${state.endTime}]."
        LOG.error(s"[ES] get doc statistics info failed [Time span: ${state.startTime}, ${state.endTime}].")
    }
  }
}
