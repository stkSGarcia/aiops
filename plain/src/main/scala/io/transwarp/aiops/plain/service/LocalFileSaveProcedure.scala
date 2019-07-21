package io.transwarp.aiops.plain.service

import io.transwarp.aiops.plain.conf.EsConf

class LocalFileSaveProcedure extends FileSystemSaveProcedure {
  override def getFileNameSchema: String = EsConf.getConf.minesLocalDataDir
}
