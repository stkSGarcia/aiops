package io.transwarp.aiops.ws.feature.log

import java.io._
import java.util.UUID

import io.transwarp.aiops.Utils
import io.transwarp.aiops.log.loader.LogCacheType
import io.transwarp.aiops.ws.spring.controller.{CacheMap, GlobalCacheType}
import org.apache.commons.compress.archivers.tar.{TarArchiveEntry, TarArchiveInputStream}
import org.apache.commons.compress.archivers.zip.{ZipArchiveEntry, ZipArchiveInputStream}
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream
import org.apache.commons.compress.utils.IOUtils

import scala.collection.mutable.ArrayBuffer

object LogServices {


  def normalizeFile(sessionID: String, index: Int, fileNum: Int, inputFile: (String, InputStream),
                    targetBuffer: ArrayBuffer[File]) = {
    val fn = inputFile._1
    val inputIS = inputFile._2
    val pf = postFix(fn)

    if (!isZipFile(pf)) {
      val tmpDir = new File(Utils.TempDir, "%s".format(UUID.randomUUID.toString))
      if (!tmpDir.exists) {
        tmpDir.mkdirs
      }
      // copy upload file to tmp dir
      val copyFile = new File(tmpDir, fn)
      val os = new FileOutputStream(copyFile)
      val buffer = new Array[Byte](8 * 1024)
      var len = inputIS.read(buffer)
      while (len != -1) {
        os.write(buffer, 0, len)
        len = inputIS.read(buffer)
      }
      os.close()
      targetBuffer += copyFile
      CacheMap.logPutGlobal(sessionID, GlobalCacheType.UnzipRateCache, (index + 1) / fileNum.toFloat)
    } else {
      val tmpDir = new File(Utils.TempDir, "%s".format(UUID.randomUUID.toString))
      if (!tmpDir.exists) {
        tmpDir.mkdirs
      }
      // copy upload zip file to tmp dir
      val copyFile = new File(tmpDir, fn)

      println(s"copy file path ${copyFile.getAbsolutePath}")

      val os = new FileOutputStream(copyFile)
      val buffer = new Array[Byte](8 * 1024)
      var len = inputIS.read(buffer)
      while (len != -1) {
        os.write(buffer, 0, len)
        len = inputIS.read(buffer)
      }
      os.close

      //      val is = new FileInputStream(copyFile)

      def decompressZip = {
        // compute total size

        var zipInputStream = new ZipArchiveInputStream(new FileInputStream(copyFile))
        var entry: ZipArchiveEntry = zipInputStream.getNextZipEntry
        var totalSize: Long = 0

        while (entry != null) {
          if (!entry.isDirectory) {
            totalSize += entry.getSize
          }
          entry = zipInputStream.getNextZipEntry
        }
        zipInputStream.close

        // compute decompress size
        zipInputStream = new ZipArchiveInputStream(new FileInputStream(copyFile))
        entry = zipInputStream.getNextZipEntry
        var iterSize: Long = 0
        while (entry != null) {
          if (!entry.isDirectory) {
            val curFile = new File(tmpDir, entry.getName)
            val parentDir = curFile.getParentFile
            if (!parentDir.exists) {
              if (!parentDir.mkdir) {
                throw new RuntimeException("can't create parent path")
              }
            }
            IOUtils.copy(zipInputStream, new FileOutputStream(curFile))
            iterSize += entry.getSize
            CacheMap.logPutGlobal(sessionID, GlobalCacheType.UnzipRateCache, (index / fileNum.toFloat) + ((iterSize / totalSize.toFloat) / fileNum))
          }
          entry = zipInputStream.getNextZipEntry
        }
        zipInputStream.close
        copyFile.delete

        listFiles(tmpDir).foreach(file => targetBuffer += file)
      }

      def decompressTarOrGz = {
        // compute total size
        var tarInputStream = pf match {
          case "tar" => new TarArchiveInputStream(new FileInputStream(copyFile))
          case "gz" => new TarArchiveInputStream(new GzipCompressorInputStream(new FileInputStream(copyFile)))
        }
        var entry: TarArchiveEntry = tarInputStream.getNextTarEntry
        var totalSize: Long = 0

        while (entry != null) {
          if (!entry.isDirectory) {
            totalSize += entry.getSize
          }
          entry = tarInputStream.getNextTarEntry
        }
        tarInputStream.close

        // compute decompress size
        tarInputStream = pf match {
          case "tar" => new TarArchiveInputStream(new FileInputStream(copyFile))
          case "gz" => new TarArchiveInputStream(new GzipCompressorInputStream(new FileInputStream(copyFile)))
        }
        entry = tarInputStream.getNextTarEntry
        var iterSize: Long = 0
        while (entry != null) {
          if (!entry.isDirectory) {
            val curFile = new File(tmpDir, entry.getName)
            val parentDir = curFile.getParentFile
            if (!parentDir.exists) {
              if (!parentDir.mkdir) {
                throw new RuntimeException("can't create parent path")
              }
            }
            IOUtils.copy(tarInputStream, new FileOutputStream(curFile))
            iterSize += entry.getSize
            CacheMap.logPutGlobal(sessionID, GlobalCacheType.UnzipRateCache, iterSize / totalSize.toFloat)
          }
          entry = tarInputStream.getNextTarEntry
        }
        tarInputStream.close
        copyFile.delete

        listFiles(tmpDir).foreach(file => targetBuffer += file)
      }

      pf match {
        case "zip" => decompressZip
        case "tar" | "gz" => decompressTarOrGz
      }
    }

  }


  private def zipFileName(fn: String) = {
    "a.%s".format(postFix(fn))
  }

  private def postFix(fn: String) = {
    fn.split("\\.").last
  }

  private def isZipFile(f: File) = {
    val pf = postFix(f.getName)
    pf.equals("zip") || pf.equals("gz") || pf.equals("tar")
  }

  private def isZipFile(pf: String) = {
    pf.equals("zip") || pf.equals("gz") || pf.equals("tar")
  }

  private def listFiles(f: File): Iterator[File] = {
    Utils.listFiles(f, (f: File) => {
      !(f.getName.equals(".") || f.getName.equals("..") || isZipFile(f) || f.getName.startsWith("."))
    })
  }

}
