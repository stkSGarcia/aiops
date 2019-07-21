package io.transwarp.aiops.sar

import java.io._

object SarTools {

  val SplitSymbol = ","

  var inputDir: File = _
  var outputFile: File = _
  var confFile: File = _

  private def help(): Unit = {
    System.err.println("io.transwarp.aiops.sar.SarTools <confFile> <inputDir> <outputFile>")
    System.exit(255)
  }

  def checkArgsValid(): Unit = {
    if (!confFile.isFile) {
      System.err.println("conf file %s must be exists".format(confFile.getAbsolutePath))
      System.exit(255)
    }

    if (!inputDir.isDirectory) {
      System.err.println("input dir %s must be exists".format(inputDir.getAbsolutePath))
      System.exit(255)
    }
  }

  private def skippedFile(f: File): Boolean = {
    val name = f.getName
    name.equals(".") || name.equals("..") || name.endsWith("zip") || name.endsWith("tar") || name.endsWith("tar.gz")
  }

  def findSarAndWork(input: File, format: String, writer: PrintWriter): Unit = {
    if (input.isFile) {
      if (!skippedFile(input)) {
        println("Parse %s".format(input.getAbsolutePath))
        val sar = new Sar(input)
        sar.outputSars(format.split(SplitSymbol), SplitSymbol).foreach(line => {
          writer.println(line)
        })
      }
    } else if (input.isDirectory) {
      input.listFiles().foreach(f => findSarAndWork(f, format, writer))
    }
  }

  def main(args: Array[String]): Unit = {
    if (args.contains("-h") || args.contains("--help") || args.length != 3) {
      help()
    }

    confFile = new File(args(0))
    inputDir = new File(args(1))
    outputFile = new File(args(2))

    checkArgsValid()

    val format = {
      var br: BufferedReader = null
      try {
        br = new BufferedReader(new FileReader(confFile))
        br.readLine()
      } finally {
        if (br != null) {
          br.close()
        }
      }
    }

    var pw: PrintWriter = null
    try {
      pw = new PrintWriter(new FileWriter(outputFile))
      findSarAndWork(inputDir, format, pw)
    } finally {
      if (pw != null) {
        pw.close()
      }
    }
  }
}
