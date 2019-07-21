package io.transwarp.aiops.log.pattern

import java.util

import io.transwarp.aiops.log.message.LogEntity
import io.transwarp.aiops.log.parser.Level
import org.junit.{Assert, Test}

import scala.collection.JavaConversions._

class InceptorPatternTest {


  @Test
  def testSessionClose(): Unit = {
    val trueList = new util.ArrayList[LogEntity]

    trueList.add(new LogEntity(level = Level.INFO, content = "2018-04-18 08:33:12,138 INFO  thrift.ThriftCLIService: (ThriftCLIService.java:CloseSession(494)) [HiveServer2-Handler-Pool: Thread-3299()] - Closed a session: SessionHandle [778008cb-af33-476d-abf2-51422527990e]"))
    trueList.add(new LogEntity(level = Level.INFO, content = "Closed a session: SessionHandle [778008cb-af33-7990e]"))
    trueList.add(new LogEntity(level = Level.INFO, content = " asdfasdfasdfad Closed a session: SessionHandle [aa]"))


    val falseList = new util.ArrayList[LogEntity]
    falseList.add(new LogEntity(level = Level.INFO, content = "asdfasdfed a session: SessionHandle [778008cb-af33-7990e]"))
    falseList.add(new LogEntity(level = Level.INFO, content = "Closed a session: SessionHandle [778008*cb-af33-7990e"))
    falseList.add(new LogEntity(level = Level.INFO, content = "Closed a session: SessionHandle[778008cb-af33-7990e]"))

    // verify trueList
    val matcher0 = InceptorPattern.sessionClosePattern.matcher(trueList.get(0).content)
    Assert.assertTrue(matcher0.find)
    Assert.assertEquals("Closed a session: SessionHandle [778008cb-af33-476d-abf2-51422527990e]", matcher0.group(0))


    val matcher1 = InceptorPattern.sessionClosePattern.matcher(trueList.get(1).content)
    Assert.assertTrue(matcher1.find)
    Assert.assertEquals("Closed a session: SessionHandle [778008cb-af33-7990e]", matcher1.group(0))


    val matcher2 = InceptorPattern.sessionClosePattern.matcher(trueList.get(2).content)
    Assert.assertTrue(matcher2.find)
    Assert.assertEquals("Closed a session: SessionHandle [aa]", matcher2.group(0))

    // verify falseList
    falseList.foreach((item) => {
      val matcher = InceptorPattern.sessionClosePattern.matcher(item.content)
      Assert.assertFalse(matcher.find)
    })

  }

}
