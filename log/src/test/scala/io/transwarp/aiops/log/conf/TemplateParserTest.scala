package io.transwarp.aiops.log.conf

import java.util
import java.util.regex.Pattern

import com.alibaba.fastjson.JSON
import com.alibaba.fastjson.serializer.SerializeFilter
import io.transwarp.aiops.Component
import org.apache.commons.io.IOUtils
import org.junit.{Assert, Test}

import scala.collection.mutable
import scala.collection.mutable.{ArrayBuffer, HashMap}
import scala.reflect.runtime.universe._
import scala.sys.process._


class LogStateBeans

object Comp extends Enumeration {
  type Comp = Value
  val UNKNOWN = Value("UNKNOWN")
  val TEST = Value("TEST")
  val INCEPTOR = Value("INCEPTOR")
  val OS = Value("OS")
  val MANAGER = Value("MANAGER")
  val HYPERBASE = Value("HYPERBASE")
  val SEARCH = Value("SEARCH")
  val HADOOP = Value("HADOOP")
  val SHIVA = Value("SHIVA")
  val SOPHON = Value("SOPHON")
  val TOS = Value("TOS")
  val WILDCARD = Value("WILDCARD")

}

class TemplateParserTest {
  val configPath = "log/inceptor.json"

  @Test
  def testConf = {
    val inceptorPath = "log/goalTemplate/inceptor.json"
    val inceptorTmpStr = IOUtils.toString(getClass.getClassLoader.getResourceAsStream(inceptorPath))
    println(s"str: ${inceptorTmpStr}")
  }

  @Test
  def testFull = {
//    trait TypedName {
//      val tt: TypeTag
//    }
//
//    object CacheField1 extends TypedName {
//      val tt =
//       type Out = HashMap[String, String]
////      type Out = mutable.HashEntry[String, String]
//    }
//
////    object CacheField2 extends TypedName {
////      type Out = Vector
////    }
//
//    val map = new mutable.HashMap[String, String]()
//    map += ("abc" -> "lala")
//
//    def func(typedName: TypedName, obj: Object): typedName.Out = {
//
//      obj.asInstanceOf[typedName.Out]
//    }
//
//
//    val a = func(CacheField1, map)
//    println(a.getClass)
//
////    val b = func(CacheField2)
////    b.iterator
  }


  @Test
  def testUnzip = {
    //    val command = if (pf.equals("zip")) {
    //      "unzip -q %s -d %s".format(zipFile.getAbsolutePath, tmpDir)
    //    } else {
    //      "tar -xf %s -C %s".format(zipFile.getAbsolutePath, tmpDir)
    //    }
    val command = "unzip -q /Users/hippo/Data/aiops_cp/20180606.zip -d /Users/hippo/Data/aiops_out"
    println(command)
    val res = command !

    if (res == 0) {
      //      listFiles(tmpDir).foreach(
      //        zf => {
      //          var is: InputStream = null
      //          try {
      //            is = new BufferedInputStream(new FileInputStream(zf))
      //            targetBuffer += ((zf.getName, is))
      //          } catch {
      //            case t: Throwable => {
      //              println(t.getStackTraceString)
      //            }
      //              if (is != null) {
      //                is.close()
      //              }
      //          }
      //        })
    } else {
      throw new RuntimeException("Uncompress commands execute failed ")
    }
  }


  @Test
  def testFilter = {
    val sample = Array(1, 2, 3, 4, 5, 6)
    val result = sample.clone
    sample(0) = 10
    println
    //    val res = sample.filter(_/2 == 0)
    //    println(res)
  }

  @Test
  def testMap = {

    def cast[A](a: Any, tt: TypeTag[A]): A = a.asInstanceOf[A]

    def trans(a: AnyRef): Option[AnyRef] = {
      Some(a)
    }

    //    class Ttag {
    //       var t: TypeTag[_] = null
    //    }
    class Ttag(val t: TypeTag[_])

    object MapTtag extends Ttag(typeTag[HashMap[String, String]])
    //    case class MapTtag extends Ttag {
    //       t = typeTag[HashMap[String, String]]
    //    }

    val tagMap = new HashMap[String, TypeTag[_]]
    tagMap += ("map" -> typeTag[mutable.HashMap[String, String]])


    val testMap = new HashMap[String, String]
    testMap += (("1" -> "haha"))
    val objMap = trans(testMap).get
    val t = MapTtag.t
    println(t.getClass)


    //    val res = cast(objMap, typeTag[HashMap[String, String]])
    //    val res = cast(objMap, MapTtag.t)
    //    println(


    //    trait DefaultValue {
    //      type A
    //      var value: A
    //    }


    object CacheTypeMap {

    }

    def transfer(input: AnyRef): Option[AnyRef] = {
      Some(input)
    }

    def test(map: HashMap[String, String]): HashMap[_, _] = {
      map
    }


  }


  @Test
  def testEnum = {
    println(Comp.MANAGER.toString)
  }

  @Test
  def testArrayList() = {
    val data = new Array[ArrayBuffer[Int]](3)
    data(0) = ArrayBuffer()
    data(1) = ArrayBuffer()
    data(2) = ArrayBuffer()

    data(0) ++= List(1, 2, 3)
    data(1) ++= List(4, 5, 6)
    data(2) ++= List(7, 8, 9)

    val flatData = data.flatten
    println
  }


  @Test
  def testMethod() = {
    def until(condition: => Boolean)(block: => Unit)(another: => Unit): Unit = {
      if (!condition) {
        block
        another
        until(condition)(block)(another)
      }
    }


    val untilFunc = (b: Double) => {
      var a = b
      until(a == 0) {
        a -= 1
        println(a)
      }(println("another one"))
    }

    untilFunc(10)

  }

  @Test
  def templateTest() = {
    val taskList = new util.ArrayList[TaskTemplate]
    taskList.add(new TaskTemplate(id = 0, name = "Parsing And Logical Plan", pre = "<PERFLOG method=compile>",
      occur = null, post = "</PERFLOG method=compile start=[0-9]+ end=[0-9]+ duration=[0-9]+>"))
    taskList.add(new TaskTemplate(id = 1, name = "Acquire Lock", pre = "<PERFLOG method=acquireReadWriteLocks>",
      occur = null, post = "</PERFLOG method=acquireReadWriteLocks start=[0-9]+ end=[0-9]+ duration=[0-9]+>"))

    val subTaskList = new util.ArrayList[TaskTemplate]
    subTaskList.add(new TaskTemplate(id = 0, name = "Run Job [execution]", pre = "Starting job: runJob",
      occur = null, post = "Job finished: runJob"))

    taskList.add(new TaskTemplate(id = 2, name = "Physical Plan", pre = "<PERFLOG method=Driver.execute>",
      occur = null, post = "</PERFLOG method=Driver.execute start=[0-9]+ end=[0-9]+ duration=[0-9]+>",
      taskList = subTaskList))

    val goal = new GoalTemplate(component = s"${Component.INCEPTOR}", id = 0, name = "run sql",
      pre = "<PERFLOG method=compile>", post = "</PERFLOG method=Driver.execute start=[0-9]+ end=[0-9]+ duration=[0-9]+>",
      taskList = taskList)

    println(JSON.toJSONString(goal, new Array[SerializeFilter](0)))
  }

  @Test
  def templateSerializeTest() = {
    val taskList = new util.ArrayList[TaskTemplate]
    taskList.add(new TaskTemplate(id = 0, name = "Parsing And Logical Plan", pre = "<PERFLOG method=compile>",
      occur = null, post = "<PERFLOG method=Driver.execute>"))
    taskList.add(new TaskTemplate(id = 1, name = "Physical Plan", pre = "<PERFLOG method=Driver.execute>",
      occur = null, post = "Starting job: runJob at"))
    taskList.add(new TaskTemplate(id = 2, name = "Run Job [execution]", pre = "Starting job: runJob",
      occur = null, post = "Job finished: runJob at"))
    taskList.add(new TaskTemplate(id = 3, name = "Post Execution", pre = "Job finished: runJob at",
      occur = null, post = "</PERFLOG method=Driver.execute start=[0-9]+ end=[0-9]+ duration=[0-9]+>"))

    val goal = new GoalTemplate(component = s"${Component.INCEPTOR}", id = 0, name = "run sql",
      pre = "<PERFLOG method=compile>", post = "</PERFLOG method=Driver.execute start=[0-9]+ end=[0-9]+ duration=[0-9]+>",
      taskList = taskList)

    println(JSON.toJSONString(goal, new Array[SerializeFilter](0)))

  }


  @Test
  def testRegex(): Unit = {
    val testString = "2018-04-10 08:41:53,098 INFO  ql.Driver: (PerfLogger.java:PerfLogEnd(137)) " +
      "[HiveServer2-Handler-Pool: Thread-1732(SessionHandle=1fcdf292-4179-4f17-9772-2e468c333915)] - </PERFLOG " +
      "method=Driver.execute start=123 end=1523364113098 duration=904>"

    val taskTemplate = new TaskTemplate(id = 2, name = "SQL Execution", pre = "Starting job: runJob at FileSinkOperator",
      occur = null, post = "</PERFLOG method=Driver.execute start=[0-9]+ end=[0-9]+ duration=[0-9]+>")

    val pattern = Pattern.compile(taskTemplate.post)

    val matcher = pattern.matcher(testString)
    Assert.assertTrue(matcher.find)
  }


  @Test
  def testTemplateParser() = {
    val inceptorTmpStr = IOUtils.toString(getClass.getClassLoader.getResourceAsStream("log/inceptor.json"))
    val goalTemplate = JSON.parseObject(inceptorTmpStr, classOf[GoalTemplate])
    println()
  }

  @Test
  def testLoadTemplate() = {

  }
}
