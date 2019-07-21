package io.transwarp.aiops.plain.service

import io.transwarp.aiops.plain.dao.{DaoRes, PlainDocDao}
import io.transwarp.aiops.plain.model.{DocIdState, ResStatus, State}

class EsDeleteProcedure extends Procedure {
  override def work(s: State): Unit = {
    val state = s.asInstanceOf[DocIdState]
    PlainDocDao.deleteById(state.id) match {
      case DaoRes.Success =>
        state.status = ResStatus.SUCCESS
        LOG.info(s"[ES] delete doc successfully [ID = ${state.id}].")
      case DaoRes.Failure =>
        state.status = ResStatus.FAILURE
        state.errMsg = s"[ES] delete doc failed [ID = ${state.id}]."
        LOG.error(s"[ES] delete doc failed [ID = ${state.id}].")
    }
  }
}
