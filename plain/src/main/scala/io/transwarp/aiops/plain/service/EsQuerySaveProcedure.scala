package io.transwarp.aiops.plain.service

import io.transwarp.aiops.plain.dao.{DaoRes, PlainDocDao}
import io.transwarp.aiops.plain.model.{ResStatus, SearchByPageState, SearchState, State}

import scala.util.{Failure, Success, Try}

class EsQuerySaveProcedure extends Procedure {
  override def work(s: State): Unit = {
    val state = s.asInstanceOf[SearchState]
    if (state.query.trim != "") {
      val shouldSave = Try(state.asInstanceOf[SearchByPageState]) match {
        case Success(pageState) => if (pageState.page == 1) true else false
        case Failure(_) => true
      }

      if (shouldSave) {
        PlainDocDao.querySave(state.timestamp, state.query, state.result) match {
          case DaoRes.Success =>
            state.status = ResStatus.SUCCESS
            LOG.info(s"[ES] query save successfully, timestamp: ${state.timestamp},\nquery: ${state.query}")
          case DaoRes.Failure =>
            state.status = ResStatus.FAILURE
            state.errMsg = s"[ES] query save failed, timestamp: ${state.timestamp},\nquery: ${state.query}"
            LOG.error(s"[ES] query save failed, timestamp: ${state.timestamp},\nquery: ${state.query}")
        }
      }
    }
  }
}
