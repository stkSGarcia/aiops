package io.transwarp.aiops.plain.service

import io.transwarp.aiops.plain.conf.EsConf
import io.transwarp.aiops.plain.dao.{DaoRes, PlainDocDao}
import io.transwarp.aiops.plain.model.{ResStatus, State}

class EsTruncateProcedure extends Procedure {
  override def work(state: State): Unit = {
    PlainDocDao.deleteIndex(EsConf.getConf.dataIndex) match {
      case DaoRes.Success =>
        LOG.info(s"[ES] index ${EsConf.getConf.dataIndex} delete successfully.")
      case DaoRes.Failure =>
        LOG.warn(s"[ES] index ${EsConf.getConf.dataIndex} delete failed.")
    }
    PlainDocDao.createIndex(EsConf.getConf.dataIndex, EsConf.getJsonString(EsConf.getConf.data_mapping)) match {
      case DaoRes.Success =>
        state.status = ResStatus.SUCCESS
        LOG.info(s"[ES] index ${EsConf.getConf.dataIndex} has been rebuilt successfully.")
      case DaoRes.Failure =>
        state.status = ResStatus.FAILURE
        LOG.error(s"[ES] index ${EsConf.getConf.dataIndex} truncate failed.")
    }

    PlainDocDao.deleteIndex(EsConf.getConf.queryIndex) match {
      case DaoRes.Success =>
        LOG.info(s"[ES] index ${EsConf.getConf.queryIndex} delete successfully.")
      case DaoRes.Failure =>
        LOG.warn(s"[ES] index ${EsConf.getConf.queryIndex} delete failed.")
    }
    PlainDocDao.createIndex(EsConf.getConf.queryIndex, EsConf.getJsonString(EsConf.getConf.query_mapping)) match {
      case DaoRes.Success =>
        state.status = ResStatus.SUCCESS
        LOG.info(s"[ES] index ${EsConf.getConf.queryIndex} has been rebuilt successfully.")
      case DaoRes.Failure =>
        state.status = ResStatus.FAILURE
        LOG.error(s"[ES] index ${EsConf.getConf.queryIndex} truncate failed.")
    }
  }
}
