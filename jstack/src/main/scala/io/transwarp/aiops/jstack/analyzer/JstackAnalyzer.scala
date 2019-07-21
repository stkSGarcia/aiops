package io.transwarp.aiops.jstack.analyzer

import java.io.File

import io.transwarp.aiops.Utils
import io.transwarp.aiops.jstack.parser.EntryParser
import io.transwarp.aiops.jstack.structure.{JstackFile, JstackResponse}

class JstackAnalyzer {
  def analyze(files: Array[File]): JstackResponse = {
    val normalizedFiles = Utils.normalizeFiles(files)
    val fileInfo = normalizedFiles.map(f => analyzeFile(EntryParser.fromFile(f), f.getName))
    val totalInfo = if (fileInfo.length > 1) totalAccum(fileInfo).toJstackFileInfo else null
    JstackResponse(fileInfo.map(_.toJstackFileInfo), totalInfo)
  }

  private def analyzeFile(entryParser: EntryParser, fileName: String): JstackFile = {
    val fileInfo = new JstackFile
    fileInfo.timestamp = entryParser.timestamp
    fileInfo.fileName = fileName
    entryParser.iterator.foreach(fileInfo += _)
    fileInfo
  }

  private def totalAccum(fileInfo: Array[JstackFile]): JstackFile = fileInfo.fold(new JstackFile)(_ merge _)
}
