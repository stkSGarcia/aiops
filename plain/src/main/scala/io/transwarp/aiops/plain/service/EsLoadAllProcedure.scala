package io.transwarp.aiops.plain.service

import io.transwarp.aiops.plain.conf.EsConf
import io.transwarp.aiops.plain.dao.{DaoRes, PlainDocDao}
import io.transwarp.aiops.plain.model.{ResStatus, State}
import org.apache.commons.io.IOUtils
import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.fs.{FileStatus, Path}

import scala.collection.mutable

class EsLoadAllProcedure extends Procedure {
  override def work(state: State): Unit = {
    val rootPath = new Path(EsConf.getConf.minesDataDir)
    val fs = rootPath.getFileSystem(new Configuration)
    if (!fs.exists(rootPath)) {
      state.errMsg = s"[FS] history data path $rootPath doesn't exist."
      LOG.error(s"[FS] history data path $rootPath doesn't exist.")
    }

    def getFileRecursive(dir: FileStatus): Iterator[FileStatus] = {
      val directories = fs.listStatus(dir.getPath).filter(_.isDirectory)
      val files = fs.listStatus(dir.getPath).filter(_.isFile)
      files.toIterator ++ directories.toIterator.flatMap(getFileRecursive)
    }

    def getJsonFromFile(dir: FileStatus): String = {
      IOUtils.toString(fs.open(dir.getPath))
    }

    var slice = mutable.Map[String, String]()
    getFileRecursive(fs.getFileStatus(rootPath)).zipWithIndex.foreach {
      case (file, idx) =>
        if ((idx != 0) && ((idx % EsConf.getConf.bulkSize) == 0)) {
          PlainDocDao.bulkSave(slice) match {
            case DaoRes.Success => state.status = ResStatus.SUCCESS
            case DaoRes.Failure =>
              state.status = ResStatus.FAILURE
              state.errMsg = s"[ES] failed to load all history data to index ${EsConf.getConf.dataIndex}."
              LOG.error(s"[ES] failed to load all history data to index ${EsConf.getConf.dataIndex}.")
          }
          slice = mutable.Map[String, String]()
          slice += (file.getPath.getName -> getJsonFromFile(file))
        } else {
          slice += (file.getPath.getName -> getJsonFromFile(file))
        }
    }

    if (slice.nonEmpty) {
      PlainDocDao.bulkSave(slice) match {
        case DaoRes.Success => state.status = ResStatus.SUCCESS
        case DaoRes.Failure =>
          state.status = ResStatus.FAILURE
          state.errMsg = s"[ES] failed to load all history data to index ${EsConf.getConf.dataIndex}."
          LOG.error(s"[ES] failed to load all history data to index ${EsConf.getConf.dataIndex}.")
      }
    }

    if (state.status == ResStatus.SUCCESS) {
      LOG.info(s"[ES] load all history data to index ${EsConf.getConf.dataIndex} successfully.")
    }
  }
}
