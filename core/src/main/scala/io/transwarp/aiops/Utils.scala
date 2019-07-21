package io.transwarp.aiops

import java.io.{File, FileInputStream, FileOutputStream}
import java.security.AccessController

import org.apache.commons.compress.archivers.ArchiveInputStream
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream
import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream
import org.apache.commons.compress.utils.IOUtils
import sun.security.action.GetPropertyAction

import scala.collection.mutable.ArrayBuffer

object Utils {
  val TempDir = new File(AccessController.doPrivileged(new GetPropertyAction("java.io.tmpdir")))

  /**
    * List files in the directory recursively.
    *
    * @param file   Directory or file path.
    * @param filter The predicate used to test values.
    * @return An iterator which produces files that satisfy the predicate `filter`.
    */
  def listFiles(file: File, filter: File => Boolean = _ => true): Iterator[File] = {
    if (file.isDirectory) file.listFiles.iterator.filter(filter).flatMap(listFiles(_, filter))
    else Array(file).iterator
  }

  /**
    * Decompress archive files if exist.
    *
    * @param files Files which may contain archive files.
    * @return Decompressed files.
    */
  def normalizeFiles(files: Array[File]): Array[File] = {
    if (files == null || files.isEmpty) throw new IllegalArgumentException("Empty file array for normalization")
    val fileList = new ArrayBuffer[File]
    files.filter(_.isFile).foreach(f => f.getName match {
      case name if name.endsWith(".zip") =>
        decompressArchive(
          new ZipArchiveInputStream(new FileInputStream(f)),
          f.getParentFile,
          fileList)
        f.delete
      case name if name.endsWith(".tar") =>
        decompressArchive(
          new TarArchiveInputStream(new FileInputStream(f)),
          f.getParentFile,
          fileList)
        f.delete
      case name if name.endsWith(".tar.gz") =>
        decompressArchive(
          new TarArchiveInputStream(new GzipCompressorInputStream(new FileInputStream(f))),
          f.getParentFile,
          fileList)
        f.delete
      case _ => fileList += f
    })
    fileList.toArray
  }

  private def decompressArchive(ais: ArchiveInputStream, dest: File, files: ArrayBuffer[File]): Unit = {
    var entry = ais.getNextEntry
    while (entry != null) {
      if (!entry.isDirectory) {
        val curFile = new File(dest, entry.getName)
        val parentDir = curFile.getParentFile
        if (!parentDir.exists) parentDir.mkdirs
        IOUtils.copy(ais, new FileOutputStream(curFile))
        files += curFile
      }
      entry = ais.getNextEntry
    }
    ais.close()
  }
}
