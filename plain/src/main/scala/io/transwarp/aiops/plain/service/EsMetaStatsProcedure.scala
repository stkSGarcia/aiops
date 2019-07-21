//package io.transwarp.aiops.plain.service
//
//import io.transwarp.aiops.Logger
//import io.transwarp.aiops.plain.dao.{DaoRes, PlainDocDao}
//import io.transwarp.aiops.plain.state.StatsState
//import io.transwarp.aiops.plain.state.Result
//
//class EsMetaStatsProcedure extends Procedure[StatsState] with Logger {
//
//  override def work(stateBeans: StatsState): Unit = {
//    val metaRes = PlainDocDao.getMeta
//    metaRes.status match {
//      case DaoRes.Success => {
//        stateBeans.result.docAmount = metaRes.docAmount
//        stateBeans.result.shardNum = metaRes.shardNum
//        stateBeans.result.status = Result.successStringResult
//      }
//      case DaoRes.Failure => {
//        LOG.error("failed to get meta info from elasticsearch")
//        stateBeans.result.status = Result.failureStringResult
//      }
//    }
//
//  }
//}
