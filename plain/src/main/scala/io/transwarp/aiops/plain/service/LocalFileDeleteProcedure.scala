package io.transwarp.aiops.plain.service

import io.transwarp.aiops.plain.conf.EsConf

class LocalFileDeleteProcedure extends FileSystemDeleteProcedure {
  override def getFileNameSchema: String = s"file:///${EsConf.getConf.minesLocalDataDir}"
}
