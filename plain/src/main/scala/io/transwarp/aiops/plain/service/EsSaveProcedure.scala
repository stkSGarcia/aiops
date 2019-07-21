package io.transwarp.aiops.plain.service

import io.transwarp.aiops.plain.conf.EsConf
import io.transwarp.aiops.plain.dao.{DaoRes, PlainDocDao}
import io.transwarp.aiops.plain.model._

class EsSaveProcedure extends Procedure {
  override def work(s: State): Unit = {
    val state = s.asInstanceOf[SaveState]
    val plainDoc = new PlainDoc(state.id, state.timestamp, state.userName, state.component, state.problem, state.solution)
    PlainDocDao.save(plainDoc) match {
      case DaoRes.Success =>
        state.status = ResStatus.SUCCESS
        LOG.info(s"[ES] Save new doc [id = ${state.id}] into index ${EsConf.getConf.dataIndex} successfully.")
      case DaoRes.Failure =>
        state.status = ResStatus.FAILURE
        state.errMsg = s"[ES] Save new doc [id = ${state.id}] into index ${EsConf.getConf.dataIndex} failed."
        LOG.error(s"[ES] Save new doc [id = ${state.id}] into index ${EsConf.getConf.dataIndex} failed.")
    }
  }
}
