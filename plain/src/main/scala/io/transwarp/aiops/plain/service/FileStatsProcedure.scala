//package io.transwarp.aiops.plain.service
//
//import io.transwarp.aiops.plain.dao.EsConf
//import io.transwarp.aiops.plain.state.StatsState
//import io.transwarp.aiops.plain.util.ProcedureUtil
//import org.apache.hadoop.conf.Configuration
//import org.apache.hadoop.fs.Path
//
//import scala.collection.mutable.ArrayBuffer
//
//class FileStatsProcedure extends Procedure[StatsState] {
//
//  override def work(stateBeans: StatsState): Unit = {
//    def getTodayFilePath: String = {
//      "%s/%s".format(EsConf.minesDataDir, ProcedureUtil.getDate)
//    }
//
//    val rootPath = new Path(getTodayFilePath)
//    val fs = rootPath.getFileSystem(new Configuration)
//    if (!fs.exists(rootPath)) {
//      stateBeans.result.docPerDay = 0
//      stateBeans.result.docPerPersonPerDay = null
//    } else {
//      val docPPPP = new ArrayBuffer[(String, Int)]
//      var count: Int = 0
//      fs.listStatus(rootPath).foreach(
//        dir => {
//          val curCount = fs.listStatus(dir.getPath).length
//          if (curCount != 0) {
//            count += curCount
//            docPPPP += ((dir.getPath.getName, curCount))
//          }
//        }
//      )
//      if (count != 0) {
//        stateBeans.result.docPerDay = count
//        stateBeans.result.docPerPersonPerDay = docPPPP.toArray.sortWith((x, y) => (x._2 > y._2))
//      } else {
//        stateBeans.result.docPerDay = 0
//        stateBeans.result.docPerPersonPerDay = null
//      }
//    }
//  }
//}
