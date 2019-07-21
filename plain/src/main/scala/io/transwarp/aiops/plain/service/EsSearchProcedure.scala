package io.transwarp.aiops.plain.service

import io.transwarp.aiops.plain.conf.EsConf
import io.transwarp.aiops.plain.dao.{DaoRes, PlainDocDao}
import io.transwarp.aiops.plain.model._
import io.transwarp.aiops.plain.service.utils.{Analyzer, Tokenizer}

class EsSearchProcedure extends Procedure {
  override def work(s: State): Unit = {
    val state = s.asInstanceOf[SearchState]
    val searchRes = if (state.query == null || state.query.length == 0) {
      PlainDocDao.scanPageSearch(0, EsConf.getConf.searchPageSize)
    } else {
      // val queryTokenizer = new Tokenizer(state.query)
      // val tokens = queryTokenizer.parse
      // val analyzer = new Analyzer(tokens)
      // val norm = analyzer.getNormalString
      // val exception = analyzer.getExceptionString
      val tokenizer = new Tokenizer(state.query)
      val analyzer = new Analyzer(tokenizer)
      val norm = analyzer.normalString
      val exception = analyzer.exceptionString

      PlainDocDao.fullTxtCompoundPageSearch(norm, exception, 0, EsConf.getConf.searchPageSize)
    }

    searchRes.status match {
      case DaoRes.Success =>
        state.status = ResStatus.SUCCESS
        state.result.suggestions = searchRes.result
        LOG.info(s"[ES] search successfully,\nquery is ${state.query}")
      case DaoRes.Failure =>
        state.status = ResStatus.FAILURE
        state.errMsg = s"[ES] Search failed,\nquery is ${state.query}"
        LOG.error(s"[ES] search failed,\nquery is ${state.query}")
    }
  }
}
