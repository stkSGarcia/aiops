package io.transwarp.aiops.log.layer.atom

import java.util

import io.transwarp.aiops.log.message.LogEntity
import io.transwarp.aiops.log.parser.Level
import io.transwarp.aiops.log.pattern.InceptorPattern
import org.junit.{Assert, Test}

import scala.collection.JavaConversions._

class InceptorEntitySOTest {


  @Test
  def testInceptorRegex() = {
    val trueList = new util.ArrayList[LogEntity]
    trueList.add(LogEntity(level = Level.INFO, content = "2018-04-10 08:41:50,828 INFO  ql.Driver: (PerfLogger.java:PerfLogBegin(110)) " +
      "[HiveServer2-Handler-Pool: Thread-1732(SessionHandle=1fcdf292-4179-4f17-9772-2e468c333915)] - <PERFLOG method=Driver.run>"))

    trueList.add(LogEntity(level = Level.INFO, content = "2018-04-10 08:41:52,242 INFO  leviathan.TimedEventTracker: (Logging.scala:logInfo(59)) " +
      "[HiveServer2-Handler-Pool: Thread-1732(SessionHandle=1fcdf292-4179-4f17-9772-2e468c333915)] - [Leviathan]" +
      "[2768]RegularId: 19d5ab1a78ffbfba Extra Info: Session: 1fcdf292-4179-4f17-9772-2e468c333915 JobNo: 0 SQL: select * from tmptable_orc limit 100"))

    trueList.add(LogEntity(level = Level.UNKNOWN, content = "(SessionHandle=1fcdf292-4179-4f17-9772-2e468c333915)(SessionHandle=)" +
      "(SessionHandle=2222222)"))


    val falseList = new util.ArrayList[LogEntity]
    falseList.add(LogEntity(level = Level.INFO, content = "2018-04-10 08:41:52,287 INFO  scheduler.DAGScheduler: (Logging.scala:logInfo(59))[sparkDriver-akka.actor.default-dispatcher-15()] - Parents of final stage: List()"))
    falseList.add(LogEntity(level = Level.INFO, content = "2018-04-10 08:41:52,487 INFO  scheduler.StatsReportListener: (Logging.scala:logInfo(59)) [SparkListenerBus()] -   185.0 ms  185.0 ms  185.0 ms  185.0 ms  192.0 ms  192.0 ms  192.0 ms  192.0 ms  192.0 ms"))
    falseList.add(LogEntity(level = Level.INFO, content = "2018-04-10 08:41:50,828 INFO  ql.Driver: (PerfLogger.java:PerfLogBegin(110)) " +
      "[HiveServer2-Handler-Pool: Thread-1732(SessionHandle=1fcdf292-4179-4f17-9772-2e468]c333915)] - <PERFLOG " +
      "method=Driver.run>"))
    falseList.add(LogEntity(level = Level.UNKNOWN, content = "(SessionHandle)=1fcdf292-4179-4f17-9772-2e468c333915)"))
    falseList.add(LogEntity(level = Level.UNKNOWN, content = "SessionHandle=1fcdf292-4179-4f17-9772-2e468c333915)"))
    falseList.add(LogEntity(level = Level.UNKNOWN, content = "(SessionHandle=1fcdf292-4179-4f17-9772*2e468c:333915)"))
    falseList.add(LogEntity(level = Level.UNKNOWN, content = "(SessionHandle=1fcdf292-4179-4f17-9772-2e468c333915"))

    val pattern = InceptorPattern.sessionPattern


    trueList.foreach {
      (item) => {
        Assert.assertTrue(pattern.matcher(item.content).find())
      }
    }

    falseList.foreach {
      (item) => {
        Assert.assertFalse(pattern.matcher(item.content).find())
      }
    }

    // Test filterEntityWithSession method
    val inceptorSubjectObj = new InceptorEntitySO
    val method = inceptorSubjectObj.getClass.getDeclaredMethod("filterEntityWithSession", (new LogEntity).getClass)
    method.setAccessible(true)

    // check filter result of true list
    val filterRes0 = method.invoke(inceptorSubjectObj, trueList.get(0))
      .asInstanceOf[(Boolean, String)]
    Assert.assertTrue(filterRes0._1)
    Assert.assertEquals("1fcdf292-4179-4f17-9772-2e468c333915", filterRes0._2)

    val filterRes1 = method.invoke(inceptorSubjectObj, trueList.get(1)).asInstanceOf[(Boolean, String)]
    Assert.assertTrue(filterRes1._1)
    Assert.assertEquals("1fcdf292-4179-4f17-9772-2e468c333915", filterRes1._2)


    val filterRes2 = method.invoke(inceptorSubjectObj, trueList.get(2)).asInstanceOf[(Boolean, String)]
    Assert.assertFalse(filterRes2._1)

    // check filter result of false list
    falseList.foreach {
      (item) => {
        val filterRes = method.invoke(inceptorSubjectObj, item).asInstanceOf[(Boolean, String)]
        Assert.assertFalse(filterRes._1)
      }
    }

  }
}
