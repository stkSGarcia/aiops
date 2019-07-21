package io.transwarp.aiops.plain.service

import com.alibaba.fastjson.JSON
import io.transwarp.aiops.AIOpsConf
import io.transwarp.aiops.plain.conf.EsConf
import io.transwarp.aiops.plain.model._
import io.transwarp.aiops.plain.util.ProcedureUtil
import org.apache.hadoop.fs.{FSDataOutputStream, Path}

class FileSystemSaveProcedure extends Procedure {
  override def work(s: State): Unit = {
    val state = s.asInstanceOf[SaveState]
    val fileNameSchema = s"$getFileNameSchema/${ProcedureUtil.getDate}/${state.userName}"
    val path = new Path(fileNameSchema)
    val fs = path.getFileSystem(AIOpsConf.hdfsConf)
    if (!fs.exists(path)) {
      fs.mkdirs(path)
      LOG.info(s"[FS] file saving path $path created.")
    }

    var fsOutputStream: FSDataOutputStream = null
    val filePath = new Path(path, state.id)
    try {
      fsOutputStream = fs.create(filePath)
      JSON.writeJSONString(fsOutputStream, Doc(state.timestamp,
        state.userName,
        state.component,
        state.problem,
        state.solution))
      state.status = ResStatus.SUCCESS
      LOG.info(s"[FS] file $filePath created.")
    } catch {
      case t: Throwable =>
        state.status = ResStatus.FAILURE
        state.errMsg = "[FS] Error happened in file created or data written."
        LOG.warn("[FS] Error happened in file created or data written.", t)
    } finally {
      if (fsOutputStream != null) {
        fsOutputStream.close()
      }
    }
  }

  def getFileNameSchema: String = EsConf.getConf.minesDataDir
}
