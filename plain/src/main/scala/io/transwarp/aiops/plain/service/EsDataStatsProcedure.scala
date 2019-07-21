//package io.transwarp.aiops.plain.service
//
//
//import io.transwarp.aiops.Logger
//import io.transwarp.aiops.plain.dao.{DaoRes, PlainDocDao}
//import io.transwarp.aiops.plain.state.{Result, StatsState}
//
//class EsDataStatsProcedure extends Procedure[StatsState] with Logger {
//
//  override def work(stateBeans: StatsState): Unit = {
//
//
//    val aggPersonRes = PlainDocDao.aggDocByPerson
//
//    aggPersonRes.status match {
//      case DaoRes.Success => {
//        stateBeans.result.docPerPerson = if (aggPersonRes.aggBuffer == null || aggPersonRes.aggBuffer.size == 0) null
//        else aggPersonRes.aggBuffer.toArray
//        stateBeans.result.status = Result.successStringResult
//      }
//      case DaoRes.Failure => {
//        LOG.error("failed to get aggregation info [doc num per person] from elasticsearch")
//        stateBeans.result.status = Result.failureStringResult
//      }
//    }
//
//    val aggCompRes = PlainDocDao.aggDocByComp
//
//    aggCompRes.status match {
//      case DaoRes.Success => {
//        stateBeans.result.docPerComp = if (aggCompRes.aggBuffer == null || aggCompRes.aggBuffer.size == 0) null
//        else aggCompRes.aggBuffer.toArray
//        stateBeans.result.status = Result.successStringResult
//      }
//      case DaoRes.Failure => {
//        LOG.error("failed to get aggregation info [doc num per comp] from elasticsearch")
//        stateBeans.result.status = Result.failureStringResult
//      }
//    }
//  }
//}
