import java.io.{File, FileInputStream, FileOutputStream}

import org.apache.commons.compress.archivers.zip.{ZipArchiveEntry, ZipArchiveInputStream}
import org.apache.commons.compress.utils.IOUtils
import org.junit.{Assert, Test}

import scala.collection.mutable

class TestUnzip {



  @Test
  def testOption: Unit = {
    val map = new mutable.HashMap[String, String]
    map += "1" -> "haha"

    def customPrint(input: String): Unit = {
     println(s"value is ${input}")
    }

    println("get 1")
    map.get("1").fold(println("empty"))(customPrint(_))

    println("get 2")
    map.get("2").fold(println("empty"))(customPrint(_))
  }


  @Test
  def testMultiThread = {
    val lockArray = Array(1, 2, 3)
    val t0 = new Thread(new Runnable {
      override def run = {
        lockArray.synchronized {
          Thread.sleep(60000)
        }
      }
    })
    t0.start
    Thread.sleep(5000)
    val t1 = new Thread(new Runnable {
      override def run = {
//        lockArray.synchronized {
          lockArray(0) = -1
//        }
      }
    })
    t1.start
//    while (t0.isAlive || t1.isAlive) {
      println(s"t0 ${t0.getName} status ${t0.getState}")
      println(s"t1 ${t1.getName} status ${t1.getState}")
      println(s"array first element ${lockArray(0)}")
//    }
    t0.join
    t1.join
  }


  @Test
  def testDecompress() = {
    //   val zipFile = new File("/Users/hippo/tmp/testzip")
    val outdir = "/Users/hippo/tmp/testzipOut"
    val zipInputStream = new ZipArchiveInputStream(new FileInputStream(new File("/Users/hippo/tmp/testzip/inceptor_small.zip")))
    var entry: ZipArchiveEntry = zipInputStream.getNextZipEntry
    var totalSize: Long = 0

    while (entry != null) {
      if (!entry.isDirectory) {
        totalSize += entry.getSize
      }
      entry = zipInputStream.getNextZipEntry
    }

    zipInputStream.close()

    val is = new ZipArchiveInputStream(new FileInputStream(new File("/Users/hippo/tmp/testzip/inceptor_small.zip")))
    entry = is.getNextZipEntry
    var iterSize: Long = 0

    while (entry != null) {
      if (!entry.isDirectory) {
        val curFile = new File(outdir, entry.getName)
        val parentDir = curFile.getParentFile
        if (!parentDir.exists) {
          if (!parentDir.mkdir) {
            throw new RuntimeException("can't create parent dir")
          }
        }
        IOUtils.copy(is, new FileOutputStream(curFile))
        iterSize += entry.getSize
        println(s"unzip progress " + f"${100 * (iterSize / totalSize.toFloat)}%1.2f" + "%")
      }
      entry = is.getNextZipEntry
    }

    println("decompress completed")
  }

}
