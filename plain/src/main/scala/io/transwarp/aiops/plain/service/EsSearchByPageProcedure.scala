package io.transwarp.aiops.plain.service

import io.transwarp.aiops.plain.dao.{DaoRes, PlainDocDao}
import io.transwarp.aiops.plain.model.{ResStatus, SearchByPageState, State}
import io.transwarp.aiops.plain.service.utils.{Analyzer, Tokenizer}

class EsSearchByPageProcedure extends Procedure {
  override def work(s: State): Unit = {
    val state = s.asInstanceOf[SearchByPageState]
    val searchRes = if (state.query == null || state.query.length == 0) {
      PlainDocDao.scanPageSearch((state.page - 1) * state.size, state.size)
    } else {
      val tokenizer = new Tokenizer(state.query)
      val analyzer = new Analyzer(tokenizer)
      val norm = analyzer.normalString
      val exception = analyzer.exceptionString

      PlainDocDao.fullTxtCompoundPageSearch(norm, exception, (state.page - 1) * state.size, state.size)
    }

    searchRes.status match {
      case DaoRes.Success =>
        state.status = ResStatus.SUCCESS
        state.result.suggestions = searchRes.result
        state.result.page = state.page
        state.result.size = state.size
        state.result.total = searchRes.total
        LOG.info(s"[ES] search successfully,\npage id is ${state.page},\nquery is ${state.query}")
      case DaoRes.Failure =>
        state.status = ResStatus.FAILURE
        state.errMsg = s"[ES] Search failed,\npage id is ${state.page},\nquery is ${state.query}"
        LOG.error(s"[ES] search failed,\npage id is ${state.page},\nquery is ${state.query}")
    }
  }
}
