package io.transwarp.aiops.plain.service

import io.transwarp.aiops.plain.dao.{DaoRes, PlainDocDao}
import io.transwarp.aiops.plain.model.{ResStatus, SearchByConditionState, State}

class EsSearchByConditionProcedure extends Procedure {
  override def work(s: State): Unit = {
    val state = s.asInstanceOf[SearchByConditionState]
    val searchRes = PlainDocDao.searchByCondition(state.startTime,
      state.endTime,
      state.component,
      (state.page - 1) * state.size,
      state.size)
    searchRes.status match {
      case DaoRes.Success =>
        state.status = ResStatus.SUCCESS
        state.result.suggestions = searchRes.result
        state.result.page = state.page
        state.result.size = state.size
        state.result.total = searchRes.total
        LOG.info(s"[ES] search by condition successfully." +
          s" Time: [${state.startTime} - ${state.endTime}]" +
          s" Component: ${state.component}" +
          s" page: [${state.page} ${state.size}].")
      case DaoRes.Failure =>
        state.status = ResStatus.FAILURE
        state.errMsg = s"[ES] search by condition failed." +
          s" Time: [${state.startTime} - ${state.endTime}]" +
          s" Component: ${state.component}" +
          s" page: [${state.page} ${state.size}]."
        LOG.error(s"[ES] search by condition failed." +
          s" Time: [${state.startTime} - ${state.endTime}]" +
          s" Component: ${state.component}" +
          s" page: [${state.page} ${state.size}].")
    }
  }
}
