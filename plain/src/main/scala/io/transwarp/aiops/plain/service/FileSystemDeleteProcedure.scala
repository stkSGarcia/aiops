package io.transwarp.aiops.plain.service

import io.transwarp.aiops.AIOpsConf
import io.transwarp.aiops.plain.conf.EsConf
import io.transwarp.aiops.plain.model.{DocIdState, ResStatus, State}
import org.apache.hadoop.fs.{FileStatus, Path}

import scala.util.{Failure, Success, Try}

class FileSystemDeleteProcedure extends Procedure {
  override def work(s: State): Unit = {
    val state = s.asInstanceOf[DocIdState]
    val rootPath = new Path(getFileNameSchema)
    val fs = rootPath.getFileSystem(AIOpsConf.hdfsConf)
    if (!fs.exists(rootPath)) {
      state.status = ResStatus.FAILURE
      LOG.error(s"[FS] delete file [id = ${state.id}] failed, history data path $rootPath doesn't exist.")
    }
    var fileDeleted = false

    def deleteFileRecursive(dir: FileStatus): Unit = {
      fs.listStatus(dir.getPath).foreach(fileStatus => {
        if (fileStatus.isFile && (fileStatus.getPath.getName == state.id)) {
          fs.delete(fileStatus.getPath, true)
          fileDeleted = true
          // TODO: delete empty directory after file deleting
          // val parentPath = fileStatus.getPath.getParent
          // fs.delete(fileStatus.getPath, true)
          // // if parent path is empty, delete the parent path
          // if (fs.listStatus(parentPath).filter(_.isFile).length == 0){
          //    fs.delete(parentPath, true)
          // }
        }
        if ((!fileDeleted) && fileStatus.isDirectory) {
          deleteFileRecursive(fileStatus)
        }
      })
    }

    Try(deleteFileRecursive(fs.getFileStatus(rootPath))) match {
      case Success(_) =>
        if (fileDeleted) {
          state.status = ResStatus.SUCCESS
          LOG.info(s"[FS] file [id = ${state.id}] has been deleted.")
        } else {
          state.status = ResStatus.FAILURE
          state.errMsg = s"[FS] file [id = ${state.id}] to be deleted hasn't been found from path $rootPath."
          LOG.warn(s"[FS] file [id = ${state.id}] to be deleted hasn't been found from path $rootPath.")
        }
      case Failure(f) =>
        state.status = ResStatus.FAILURE
        state.errMsg = s"[FS] error happened during file [id = ${state.id}] deleting from path $rootPath."
        LOG.error(s"[FS] error happened during file [id = ${state.id}] deleting from path $rootPath.", f)
    }
  }

  def getFileNameSchema: String = EsConf.getConf.minesDataDir
}
