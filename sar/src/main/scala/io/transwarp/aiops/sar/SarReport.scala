package io.transwarp.aiops.sar

import java.io._

import io.transwarp.aiops.sar.conf.SarConf

import scala.collection.mutable.ArrayBuffer
import scala.collection.JavaConversions._

case class SarConfFileTitle(tag: String, description: String, result: String, level: Int) {
  def enabled = !(description == null || description.isEmpty || result == null || result.isEmpty)
}

class SarConfiguration(confFile: Array[String]) {
  val TopK = 2

  var title: Array[SarConfFileTitle] = null
  val data: ArrayBuffer[Array[Double]] = new ArrayBuffer[Array[Double]]()

  var reader: BufferedReader = null
  try {
    var index = 0
    while (index < confFile.length) {
      val line = confFile(index)
      if (title != null) {
        data += line.split(",").map(e => e.toDouble)
      } else {
        val titleStr = line.split(",")
        title = new Array[SarConfFileTitle](titleStr.length)
        var i = 0
        while (i < titleStr.length) {
          val t = titleStr(i).split("\\:")
          title(i) = SarConfFileTitle(t(0),
            if (t.length >= 2) t(1) else "",
            if (t.length >= 3) t(2) else "",
            if (t.length >= 4) t(3).toInt else 0)
          i += 1
        }
      }
      index += 1
    }
  } catch {
    case t: Throwable => t.printStackTrace()
  } finally {
    if (reader != null) {
      reader.close()
    }
  }

  val ruleLine = new Array[Double](title.length)
  var i = 0
  while (i < ruleLine.length) {
    var j = 0
    var topKValue = new Array[Double](TopK)
    def sortTopK(): Unit = {
      topKValue = topKValue.sortWith((x: Double, y: Double) => x < y)
    }
    if (!title(i).enabled) {
      ruleLine(i) = Double.MaxValue
    } else {
      while (j < data.length) {
        if (data(j)(i) > topKValue(0)) {
          topKValue(0) = data(j)(i)
          sortTopK()
        }
        j += 1
      }
      ruleLine(i) = topKValue.sum / TopK.toDouble
    }
    i += 1
  }
}

class SarReportBean() {
  var errorMsg: String = ""
  var fileName: String = _

  // Conf
  var titleDesc: Array[String] = _
  var titleName: Array[String] = _
  var titleEnabled: Array[Boolean] = _
  var warnLevel: Array[Int] = _


  // Data
  var totalRecordsNumber: Int = _
  var details: Array[String] = _
  var wasError: Array[Array[Boolean]] = _
  var errorPercent: Array[String] = _
  var errorNum: Array[Int] = _
}

class SarReport(targetFile: File, inputStream: InputStream = null, fileName: String = null, confFile: Array[String]) {

  val DetailLOGLimit = 20

  val sarBean: SarReportBean = new SarReportBean()

  def this(is: InputStream, fileName: String, confFile: Array[String]) = {
    this(null, is, fileName, confFile)
  }

  if (targetFile != null && !targetFile.exists()) {
    throw new Exception("Can't find file %s".format(targetFile.getAbsolutePath))
  }
  // if (!confFile.exists()) {
  //   throw new Exception("Can't file file %s".format(confFile.getAbsolutePath))
  // }

  var errorNum: Array[Int] = null

  var totalCount = 0

  private val detailsBuffer = new ArrayBuffer[String]()
  private val wasErrorBuffer = new ArrayBuffer[Array[Boolean]]()

  var report: Array[String] = null

  def level2Str(level: Int) = {
    level match {
      case 0 => ""
      case 1 => "INFO"
      case 2 => "WARN"
      case 3 => "ERROR"
    }
  }

  var summary: Seq[String] = null

  def init: SarReport = {
    val sar = if (inputStream == null) {
      new Sar(targetFile)
    } else {
      new Sar(inputStream)
    }

    val sarConf = new SarConfiguration(confFile)
    sarBean.fileName = if (fileName == null) targetFile.getName else fileName
    sarBean.titleDesc = sarConf.title.map(_.result)
    sarBean.titleName = sarConf.title.map(_.description)
    sarBean.titleEnabled = sarConf.title.map(_.enabled)
    sarBean.warnLevel = sarConf.title.map(_.level)

    errorNum = new Array[Int](sarConf.title.length)

    val sarData = sar.sarWithTimelines.toArray.sortWith(
      (x: (String, SarElement), y: (String, SarElement)) => x._1.compareTo(y._1) < 0)
    report = sarData.filter(kv => {
      val sarElement = kv._2
      val abnormalData = sarConf.title.zipWithIndex.filter(titleWithIndex => {
        titleWithIndex._1.enabled &&
          sarElement.getSarValue(titleWithIndex._1.tag) != null &&
          sarElement.getSarValue(titleWithIndex._1.tag).toDouble > sarConf.ruleLine(titleWithIndex._2)
      })
      totalCount += 1
      abnormalData.length > 0
    }).map(kv => {
      val ts = kv._1
      val sarElement = kv._2
      val sarTxt = "%s,%s".format(ts, sarConf.title.map(t => sarElement.getSarValue(t.tag)).mkString(","))
      val sarTxtArr = sarTxt.split(",")

      val errorPos = new Array[Boolean](sarTxtArr.length)
      var inserted = false
      sarConf.title.zipWithIndex.filter(titleWithIndex => {
        errorPos(titleWithIndex._2) = titleWithIndex._1.enabled &&
          sarElement.getSarValue(titleWithIndex._1.tag) != null &&
          sarElement.getSarValue(titleWithIndex._1.tag).toDouble > sarConf.ruleLine(titleWithIndex._2)
        errorPos(titleWithIndex._2)
      }).map(titleWithIndex => {
        if (!inserted && detailsBuffer.length < DetailLOGLimit && titleWithIndex._1.level >= 2) {
          detailsBuffer += sarTxt
          inserted = true
        }
        errorNum(titleWithIndex._2) += 1
        "%s,%s".format(titleWithIndex._1.description, titleWithIndex._1.result)
      }).mkString(";") + "(%s)".format(sarTxt)
    })

    sarBean.details = detailsBuffer.toArray
    sarBean.wasError = wasErrorBuffer.toArray
    sarBean.totalRecordsNumber = totalCount
    sarBean.errorNum = errorNum
    sarBean.errorPercent = errorNum.map(e => "%.2f%%".format(e.toDouble * 100.0 / totalCount.toDouble))

    summary = {
      Seq("总计%s条记录".format(totalCount)) ++
        sarConf.title.zipWithIndex.filter(_._1.enabled).map(titleWithIdx => {
          val title = titleWithIdx._1
          val idx = titleWithIdx._2
          "%s(%s):%s(%.2f%%)".format(title.description,
            level2Str(title.level), errorNum(idx),
            errorNum(idx).toDouble * 100.0 / totalCount.toDouble)
        })
    }
    this
  }
}

object SarReport {
  def main(args: Array[String]): Unit = {
    val sarFilePath = args(0)
    val sarReport = new SarReport(new File(sarFilePath), null, null, SarConf.configFile)
    sarReport.init

    println(sarReport.report.mkString("\n"))
    println("Summary:")
    println(sarReport.summary.mkString("\n"))
  }
}
