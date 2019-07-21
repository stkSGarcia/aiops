package io.transwarp.aiops.plain.service

import io.transwarp.aiops.plain.dao.{DaoRes, PlainDocDao}
import io.transwarp.aiops.plain.model.{ResStatus, SearchByIdState, State}

class EsSearchByIdProcedure extends Procedure {
  override def work(s: State): Unit = {
    val state = s.asInstanceOf[SearchByIdState]
    val searchRes = PlainDocDao.searchById(state.id)
    searchRes.status match {
      case DaoRes.Success =>
        state.status = ResStatus.SUCCESS
        state.result.suggestions = searchRes.result
        LOG.info(s"[ES] retrieve doc by ID successfully, id=${state.id}")
      case DaoRes.Failure =>
        state.status = ResStatus.FAILURE
        state.errMsg = s"[ES] retrieve doc by ID successfully, id=${state.id}"
        LOG.error(s"[ES] retrieve doc by ID failed, id=${state.id}")
    }
  }
}
