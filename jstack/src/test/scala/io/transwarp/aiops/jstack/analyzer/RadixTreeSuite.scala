package io.transwarp.aiops.jstack.analyzer

import org.scalatest.FunSuite

class RadixTreeSuite extends FunSuite {
  test("Build Radix tree") {
    val data = Array(
      "abc-def-123",
      "abc-def-123-xyz",
      "abc-def",
      "abc-def-456",
      "abc-ghi-123",
      "abc-ghi-456",
      "123-abc",
      "123-abc-123",
      "abc-def-123-xyz",
      "abc-def-456"
    )
    val radixTrie = new RadixTree[String]
    data.foreach(f => radixTrie += f -> f)
    radixTrie.dump()
  }

  test("Test thread group") {
    val data = Array(
      "qtp1848848801-111",
      "qtp1848848801-112",

      "qtp404429505-222",
      "qtp404429505-223",

      "qtp761879999-333",
      "qtp761879999-334",

      "I/O dispatcher 11",
      "I/O dispatcher 12",
      "I/O dispatcher 13",

      "HiveServer2-Handler-Pool: Thread-28114",
      "HiveServer2-Handler-Pool: Thread-28115",
      "HiveServer2-Handler-Pool: Thread-28116",
      "HiveServer2-Handler-Pool: Thread-430-SendThread(hadooptest1:2181)",
      "HiveServer2-Handler-Pool: Thread-430-EventThread",

      "HiveServer2-Handler-Pool: Thread-433-SendThread(hadooptest3:2181)",
      "HiveServer2-Handler-Pool: Thread-433-SendThread(hadooptest2:2181)",
      "HiveServer2-Handler-Pool: Thread-433-SendThread(hadooptest1:2181)",

      "pool-23-thread-4",
      "pool-23-thread-5",
      "pool-23-thread-6",

      "pool-26-thread-1",
      "pool-26-thread-2",
      "pool-26-thread-3",

      "Gang worker#0 (Parallel GC Threads)",
      "Gang worker#0 (Parallel CMS Threads)",
      "Gang worker#0 (Pbrallel CMS Threads)",
      "Gang worker#0 (Parallec GC Threads)",
      "Gang worker#1 (Parallel GC Threads)",
      "Gang worker#2 (Parallel GC Threads)",

      "New boss #6",
      "New worker #5",
      "New #4",
      "New 3",
      "New I/O worker #4",
      "New I/O worker #2",
      "New I/O worker #1"
    )
    val radixTrie = new RadixTree[String]
    data.foreach(f => radixTrie += f -> f)
    println("=====Tree Dump=====")
    radixTrie.dump()
    println("=====Thread Groups=====")
    radixTrie.getGroupMap.foreach { case (key, value) =>
      println(s"$key ->")
      value.foreach(f => println(s"\t$f"))
    }
  }
}
