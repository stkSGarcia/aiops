package io.transwarp.aiops.ws.service.impl

import java.io.{File, FileInputStream, FileOutputStream, InputStream}
import java.util.UUID

import io.transwarp.aiops.Utils
import io.transwarp.aiops.sar.conf.SarConf
import io.transwarp.aiops.sar.{SarReport, SarReportBean}
import io.transwarp.aiops.ws.controller.converter.SarConverter._
import io.transwarp.aiops.ws.controller.response.{RestResponse, SarReportRes}
import io.transwarp.aiops.ws.service.SarService
import org.springframework.stereotype.Service

import scala.sys.process._

/**
  * Created by hippo on 9/3/18.
  */
@Service
class SarServiceImpl extends SarService {

  override def serve(fileName: String, inputStream: InputStream): RestResponse[SarReportRes] = {
    analyse(fileName, inputStream).convert
  }

  private def analyse(fileName: String, inputStream: InputStream): Array[SarReportBean] = {
    try {
      val pf = postFix(fileName)
      if (!isZipFile(pf)) {
        Array(new SarReport(inputStream, fileName, SarConf.configFile).init.sarBean)
      } else {
        val tmpDir = new File(Utils.TempDir, "%s".format(UUID.randomUUID().toString))
        if (!tmpDir.exists()) {
          tmpDir.mkdirs()
        }
        val zipFile = new File(tmpDir, zipFileName(fileName))
        var zipOutputStream: FileOutputStream = null

        try {
          zipOutputStream = new FileOutputStream(zipFile)
          val buf = new Array[Byte](64 * 1024)
          var len: Int = inputStream.read(buf)
          while (len > 0) {
            zipOutputStream.write(buf)
            len = inputStream.read(buf)
          }
          zipOutputStream.close()
          zipOutputStream = null

          val command = if (pf.equals("zip")) {
            "unzip -q %s -d %s".format(zipFile.getAbsolutePath, tmpDir)
          } else {
            "tar -xf %s -C %s".format(zipFile.getAbsolutePath, tmpDir)
          }
          println(command)
          val res = command !

          if (res == 0) {
            listFiles(tmpDir).map(
              zf => {
                var is: InputStream = null
                try {
                  is = new FileInputStream(zf)
                  val sr = new SarReport(is, zf.getName, SarConf.configFile).init
                  sr.sarBean
                } catch {
                  case t: Throwable => {
                    val srb = new SarReportBean()
                    srb.errorMsg = t.getStackTraceString
                    srb
                  }
                } finally {
                  if (is != null) {
                    is.close()
                  }
                }
              }).toArray
          } else {
            val srb = new SarReportBean()
            srb.errorMsg = "zip file error"
            Array(new SarReportBean())
          }
        } catch {
          case t: Throwable => {
            t.printStackTrace()
            val srb = new SarReportBean()
            srb.errorMsg = t.getStackTraceString
            Array(srb)
          }
        } finally {
          if (zipOutputStream != null) {
            zipOutputStream.close()
          }
        }
      }
    } catch {
      case t: Throwable => {
        t.printStackTrace()
        val srb = new SarReportBean()
        srb.errorMsg = t.getStackTraceString
        Array(srb)
      }
    }
  }

  private def zipFileName(fn: String) = {
    "a.%s".format(postFix(fn))
  }

  private def postFix(fn: String) = {
    fn.split("\\.").last
  }

  private def isZipFile(pf: String) = {
    pf.equals("zip") || pf.equals("gz") || pf.equals("tar")
  }

  private def listFiles(f: File): Iterator[File] = {
    Utils.listFiles(f, (f: File) => {
      !(f.getName.equals(".") || f.getName.equals("..") || isZipFile(f) || f.getName.startsWith("."))
    })
  }

  private def isZipFile(f: File) = {
    val pf = postFix(f.getName)
    pf.equals("zip") || pf.equals("gz") || pf.equals("tar")
  }
}
