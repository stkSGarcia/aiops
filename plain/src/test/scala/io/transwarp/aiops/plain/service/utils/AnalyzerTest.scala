package io.transwarp.aiops.plain.service.utils

import org.junit.{Before, Test}

import scala.collection.mutable.ArrayBuffer

class AnalyzerTest {
  private val data = new ArrayBuffer[String]

  @Before
  def initData(): Unit = {
    data +=
      """在启动 Transwarp HBase 的 regionserver 时，报 license is null 的错误。但是集群 license 服务正常，license 也没有过期。
java.lang.NoSuchMethodError: org.apache.spark.rdd.ShuffledRDD$.$lessinit$greater$default$7()Z
 at io.transwarp.graphsearch.bulkload.BulkloadBase$.shuffleAndMergeSortRecords(BulkloadBase.scala:149)
 at io.transwarp.graphsearch.bulkload.BulkloadBase.operatorExecute(BulkloadBase.scala:95)
 at io.transwarp.graph.GraphOperator.preUdtfExecute(GraphOperator.scala:54)
 at io.transwarp.inceptor.execution.UDTFOperator.preprocessRdd(UDTFOperator.scala:106)
 at io.transwarp.inceptor.execution.UnaryOperator.execute(Operator.scala:617)
 at io.transwarp.inceptor.execution.Operator$$anonfun$executeParents$1.apply(Operator.scala:315)
 at io.transwarp.inceptor.execution.Operator$$anonfun$executeParents$1.apply(Operator.scala:315)
 at scala.collection.TraversableLike$$anonfun$map$1.apply(TraversableLike.scala:244)
则说明该端口已经和内外网都绑定到了地址 0.0.0.0，访问应该没有问题。如果没有
java.net.ConnectException: Connection refused: no further information
at sun.nio.ch.SocketChannelImpl.checkConnect(Native Method)
at sun.nio.ch.SocketChannelImpl.finishConnect(Unknown Source)
at org.apache.hadoop.net.SocketIOWithTimeout.connect(SocketIOWithTimeout.java:206)
at org.apache.hadoop.net.NetUtils.connect(NetUtils.java:529)
at org.apache.hadoop.net.NetUtils.connect(NetUtils.java:493)
Transwarp HBase集群内部每个节点都有双网卡，其中内网IP可以直接访问集群，但是使用外网IP无法访问，拒绝连接。"""
    data +=
      """则说明该端口已经和内外网都绑定到了地址 0.0.0.0，访问应该没有问题。如果没有
java.net.ConnectException: Connection refused: no further information
java.net.ConnectException(IOException): Connection refused: no further information
at sun.nio.ch.SocketChannelImpl.checkConnect(Native Method)
Caused by: java.util.lang
Caused ?? by: java.util.lang"""
    data += "Exception"
  }

  @Test
  def analyze(): Unit = {
    data.foreach(input => {
      println("====================")
      val tokenizer = new Tokenizer(input)
      val analyzer = new Analyzer(tokenizer)
      println("-----Normal-----")
      println(analyzer.normalString)
      println("-----Exception-----")
      println(analyzer.exceptionString)
    })
  }
}
