package io.transwarp.aiops.ws.feature.log

import java.io.{File, FileInputStream, FileOutputStream}

import io.transwarp.aiops.Utils
import io.transwarp.aiops.ws.spring.controller.{CacheMap, GlobalCacheType}
import org.apache.commons.compress.archivers.tar.{TarArchiveEntry, TarArchiveInputStream}
import org.apache.commons.compress.archivers.zip.{ZipArchiveEntry, ZipArchiveInputStream}
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream
import org.apache.commons.compress.utils.IOUtils

import scala.collection.mutable.ArrayBuffer

object Unzip {


  def normalizeFile(sessionID: String, index: Int, fileNum: Int, inputFile: File, targetBuffer: ArrayBuffer[File]) = {
    val fn = inputFile.getName
    val pf = postFix(fn)

    if (!isZipFile(pf)) {
      targetBuffer += inputFile
      if (sessionID != null) {
        CacheMap.logPutGlobal(sessionID, GlobalCacheType.UnzipRateCache, (index + 1) / fileNum.toFloat)
      }
    } else {
      val tmpDir = inputFile.getParentFile

      def decompressZip = {
        // compute total size
        var zipInputStream = new ZipArchiveInputStream(new FileInputStream(inputFile))
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
        zipInputStream = new ZipArchiveInputStream(new FileInputStream(inputFile))
        entry = zipInputStream.getNextZipEntry
        var iterSize: Long = 0
        while (entry != null) {
          if (!entry.isDirectory) {
            val curFile = new File(tmpDir, entry.getName)
            if (!(curFile.getName.charAt(0) == '.')) {
              val parentDir = curFile.getParentFile
              if (!parentDir.exists) {
                if (!parentDir.mkdirs) {
                  throw new RuntimeException("can't create parent path")
                }
              }
              IOUtils.copy(zipInputStream, new FileOutputStream(curFile))
              iterSize += entry.getSize
              if (sessionID != null) {
                CacheMap.logPutGlobal(sessionID, GlobalCacheType.UnzipRateCache, (index / fileNum.toFloat) + ((iterSize / totalSize.toFloat) / fileNum))
              }
            }
          }
          entry = zipInputStream.getNextZipEntry
        }
        zipInputStream.close
        inputFile.delete

        listFiles(tmpDir).foreach(file => targetBuffer += file)
      }

      def decompressTarOrGz = {
        // compute total size
        var tarInputStream = pf match {
          case "tar" => new TarArchiveInputStream(new FileInputStream(inputFile))
          case "gz" => new TarArchiveInputStream(new GzipCompressorInputStream(new FileInputStream(inputFile)))
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
          case "tar" => new TarArchiveInputStream(new FileInputStream(inputFile))
          case "gz" => new TarArchiveInputStream(new GzipCompressorInputStream(new FileInputStream(inputFile)))
        }

        entry = tarInputStream.getNextTarEntry
        var iterSize: Long = 0
        while (entry != null) {
          if (!entry.isDirectory) {
            val curFile = new File(tmpDir, entry.getName)
            if (!(curFile.getName.charAt(0) == '.')) {
              val parentDir = curFile.getParentFile
              if (!parentDir.exists) {
                if (!parentDir.mkdirs) {
                  throw new RuntimeException("can't create parent path")
                }
              }
              IOUtils.copy(tarInputStream, new FileOutputStream(curFile))
              iterSize += entry.getSize
              if (sessionID != null) {
                CacheMap.logPutGlobal(sessionID, GlobalCacheType.UnzipRateCache, iterSize / totalSize.toFloat)
              }
            }
          }
          entry = tarInputStream.getNextTarEntry
        }
        tarInputStream.close
        inputFile.delete

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

