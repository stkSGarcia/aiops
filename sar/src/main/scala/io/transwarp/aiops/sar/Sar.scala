package io.transwarp.aiops.sar

import java.io._

import scala.collection.JavaConversions._

import java.util.{HashMap => JavaHashMap}

import scala.collection.mutable

class SarKey(val label: String, val key: String) {
  if (key == null || key.isEmpty) {
    throw new Exception("Key can't be empty")
  }
  override def hashCode(): Int = {
    label.hashCode * 31 + key.hashCode
  }

  override def equals(o: Any): Boolean = {
    o match {
      case k: SarKey => label.equals(k.label) && key.equals(k.key)
      case _ => false
    }
  }
  override def toString(): String = {
    "%s.%s".format(label, key)
  }
}

object SarKey {
  def apply(label: String, key: String) = new SarKey(label, key)
  def apply(name: String) = {
    val tokens = name.split("\\.")
    new SarKey(tokens(0), tokens(1))
  }
}

class SarElement() {
  val map = new JavaHashMap[SarKey, String]()

  def addElement(elem: SarElement): Unit = {
    elem.map.foreach(kv => map.put(kv._1, kv._2))
  }

  def getSarValue(sarKeyStr: String): String = {
    val res = map.get(SarKey(sarKeyStr))
//    if (res == null) {
//      System.err.println("Can't found sar key %s".format(sarKeyStr))
//    }
    res
  }
}


/*
 * Linux 3.10.0-327.el7.x86_64 (leviathan4)        02/23/2018      _x86_64_        (24 CPU)
 * 12:00:01 AM     CPU      %usr     %nice      %sys   %iowait    %steal      %irq     %soft    %guest    %gnice     %idle
 * 12:10:01 AM     all      3.95      0.00      0.48      0.66      0.00      0.00      0.01      0.00      0.00     94.90
 */

class Sar(inputStream: InputStream) {

  def this(file: File) = {
    this(new FileInputStream(file))
  }

  val sarWithTimelines = new JavaHashMap[String, SarElement]()
  var is: InputStream = inputStream

  private def removeElement[T](a: Array[T], i: Int): mutable.ArraySeq[T] = {
    a.zipWithIndex.filter(e => e._2 != i).map(e => e._1)
  }

  private def init(): Unit = {

    var head = new Array[String](0)

    var lastIsEmptyLine = true
    var hasLabel = false
    var dataStartPosition = 1

    def formatLine(line: String, lastSarElement: SarElement): (String, SarElement) = {
      if (line.startsWith("Linux") || line.startsWith("#")) {
        null
      } else if (line.isEmpty) {
        lastIsEmptyLine = true
        null
      } else {
        var token = line.split(" ").filter(!_.isEmpty)
        if (token.length > 2) {
          if (token(1).equalsIgnoreCase("AM") || token(1).equalsIgnoreCase("PM")) {
            token(0) = token(0) + token(1)
            token = removeElement(token, 1).toArray
          }
          if (lastIsEmptyLine) {
            lastIsEmptyLine = false
            if (token(1).toUpperCase.equals(token(1))) {
              hasLabel = true
              dataStartPosition = 2
            } else {
              hasLabel = false
              dataStartPosition = 1
            }
            head = token
            null
          } else {
            val timestamp = "%s".format(token(0))
            val sarElement = new SarElement()
            val label = if (!hasLabel) {
              ""
            } else {
              token(1)
            }
            var i = dataStartPosition
            while (i < token.length) {
              if (i < head.length) {
                val sarKey = SarKey(label, head(i))
                sarElement.map.put(sarKey, token(i))
                val sarDeltaKey = SarKey(label, head(i) + "-")
                if (lastSarElement != null) {
                  val lastSarValue = lastSarElement.map.get(sarKey)
                  try {
                    val lv = lastSarValue.toDouble
                    val cv = token(i).toDouble
                    sarElement.map.put(sarDeltaKey, (lv - cv).toString)
                  } catch {
                    case t: Throwable => // Don't put delta value, report error
                  }
                } else {
                  sarElement.map.put(sarDeltaKey, "0")
                }
              }
              i += 1
            }
            (timestamp, sarElement)
          }
        } else {
//          println(line)
          null
        }
      }
    }

    var lastElement: SarElement = null
    var reader: BufferedReader = null
    try {
      reader = new BufferedReader(new InputStreamReader(is))
      var line = reader.readLine()
      if (line != null && line.startsWith("Linux")) {
        while (line != null) {
          line = line.trim
          val res = formatLine(line, lastElement)
          if (res != null) {
            lastElement = res._2
            val c = sarWithTimelines.get(res._1)
            if (c == null) {
              sarWithTimelines.put(res._1, res._2)
            } else {
              c.addElement(res._2)
            }
          } else {
            lastElement = null
          }
          line = reader.readLine()
        }
      }
    } catch {
      case t: Throwable => t.printStackTrace()
    } finally {
      if (reader != null) {
        reader.close()
      }
    }
  }
  init()

  def outputSars(format: Array[String],
                 splitSymbal: String = ","): Seq[String] = {
    sarWithTimelines.map {
      case (timestamp: String, sar: SarElement) => {
        format.map(key => {
          val keyTokens = key.split("\\.")
          val v = sar.map.get(SarKey(keyTokens(0), keyTokens(1)))
          if (v == null) {
            throw new Exception("Can't find value for key %s".format(key))
          }
          v
        }).mkString(splitSymbal)
      }
    }.toSeq
  }
}

