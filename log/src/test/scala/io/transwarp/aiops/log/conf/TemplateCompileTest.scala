package io.transwarp.aiops.log.conf

import java.util

import org.junit.{Assert, Test}

import scala.util.{Success, Failure, Try}

case class Node(id: Int, pre: String, post: String, subNodes: Array[Node] = null)

object TestEnum extends Enumeration {
  type TestEnum = Value
  val A = Value("A")
  val B = Value("B")
  val C = Value("C")

}


class TemplateCompileTest {
  val traverseRes = new util.ArrayList[String]()

  @Test
  def test() = {
    Try(testRecursive(0)) match {
      case Success(q) => {}
      case Failure(f) => {
        println("failure " + f.getMessage)
      }
    }
  }

  private def testRecursive(level: Int): Int = {
    if (level == 10) {
      throw new RuntimeException("deep test")
    } else {
      testRecursive(level + 1)
    }
  }


  //
  //  @Test
  //  def testCombosCompile() = {
  //    LogConf.init
  //    val combosIter = LogConf.goalCombosMap.entrySet().iterator()
  //    var combos: GoalTempCombos = null
  //    var component: Component = null
  //    while (combosIter.hasNext) {
  //      val entry = combosIter.next
  //      component = entry.getKey
  //      combos = entry.getValue
  //      println()
  //      println(s"********************** component: ${component} **************************")
  //
  //
  //      val taskmapIter = combos.taskMap.entrySet().iterator()
  //      val taskEndMap = combos.taskEndMap.entrySet().iterator()
  //      val patternList = combos.patternList.iterator()
  //
  //      println("----- task map -----")
  //      while (taskmapIter.hasNext) {
  //        val entry = taskmapIter.next
  //        val value = entry.getValue
  //        println("[")
  //        println(s"taskGID: ${value.globalID} ; taskName: ${value.name} ")
  //        println(s"pre: ${value.pre}")
  //        println(s"post: ${value.post}")
  //        println("]")
  //        println()
  //      }
  //      println()
  //
  //      println("----- task end mark ----")
  //      while (taskEndMap.hasNext) {
  //        val entry = taskEndMap.next
  //        val key = entry.getKey
  //        val value = entry.getValue
  //
  //        println("[")
  //        println(s"taskGID: ${key}; taskName: ${combos.taskMap.get(key).name}")
  //        println()
  //        val currItemIter = value.iterator()
  //        while (currItemIter.hasNext) {
  //           val locator = currItemIter.next()
  //          println(s"pattern: ${locator.pattern}, from task[GID ${locator.taskTemp.globalID}, Name: ${locator.taskTemp.name}],"
  //            + s" mark ${locator.taskMark}")
  //        }
  //        println("]")
  //        println
  //      }
  //
  //      println("----- pattern list -----")
  //      var count = 0
  //      while (patternList.hasNext) {
  //         val locator = patternList.next
  //        println(s"pattern: ${locator.pattern}, from task[GID ${locator.taskTemp.globalID}, Name: ${locator.taskTemp.name}],"
  //          + s" mark ${locator.taskMark}")
  //        count += 1
  //      }
  //     println(s"count for pattern list is ${count}")
  //
  //    }
  //
  //  }

  private def constructTree(): Node = {
    /**
      * 1
      * 2                3
      * 4   5   6        7       8
      * 9  10
      */
    val leaf4 = Node(4, pre = "pre for node 4", post = "post for node 4")
    val leaf6 = Node(6, pre = "pre for node 6", post = "post for node 6")
    val leaf7 = Node(7, pre = "pre for node 7", post = "post for node 7")
    val leaf8 = Node(8, pre = "pre for node 8", post = "post for node 8")
    val leaf9 = Node(9, pre = "pre for node 9", post = "post for node 9")
    val leaf10 = Node(10, pre = "pre for node 10", post = "post for node 10")

    val subNodes5 = Array[Node](leaf9, leaf10)
    val node5 = Node(5, pre = "pre for node 5", post = "post for node 5", subNodes5)



    val subNodes2 = Array[Node](leaf4, node5, leaf6)
    val subNodes3 = Array[Node](leaf7, leaf8)

    val node2 = Node(2, pre = "pre for node 2", post = "post for node 2", subNodes = subNodes2)
    val node3 = Node(3, pre = "pre for node 3", post = "post for node 3", subNodes = subNodes3)


    val subNode1 = Array[Node](node2, node3)
    val root = Node(1, pre = "pre for node 1", post = "post for node 1", subNodes = subNode1)
    root
  }


  @Test
  def testTreeTraversal(): Unit = {
    val root = constructTree
    traverse(root)

    Assert.assertEquals("pre for node 1", traverseRes.get(0))
    Assert.assertEquals("pre for node 2", traverseRes.get(1))
    Assert.assertEquals("pre for node 4", traverseRes.get(2))
    Assert.assertEquals("post for node 4", traverseRes.get(3))
    Assert.assertEquals("pre for node 5", traverseRes.get(4))
    Assert.assertEquals("pre for node 9", traverseRes.get(5))
    Assert.assertEquals("post for node 9", traverseRes.get(6))
    Assert.assertEquals("pre for node 10", traverseRes.get(7))
    Assert.assertEquals("post for node 10", traverseRes.get(8))
    Assert.assertEquals("post for node 5", traverseRes.get(9))
    Assert.assertEquals("pre for node 6", traverseRes.get(10))
    Assert.assertEquals("post for node 6", traverseRes.get(11))
    Assert.assertEquals("post for node 2", traverseRes.get(12))
    Assert.assertEquals("pre for node 3", traverseRes.get(13))
    Assert.assertEquals("pre for node 7", traverseRes.get(14))
    Assert.assertEquals("post for node 7", traverseRes.get(15))
    Assert.assertEquals("pre for node 8", traverseRes.get(16))
    Assert.assertEquals("post for node 8", traverseRes.get(17))
    Assert.assertEquals("post for node 3", traverseRes.get(18))
    Assert.assertEquals("post for node 1", traverseRes.get(19))

    var index = 0
    while (index < traverseRes.size()) {
      println(traverseRes.get(index))
      index += 1
    }

  }


  private def traverse(root: Node): Unit = {
    if (root != null) {
      traverseRes.add(root.pre)
      if (root.subNodes != null) {
        var index = 0
        while (index < root.subNodes.length) {
          traverse(root.subNodes(index))
          index += 1
        }
      }
      traverseRes.add(root.post)
    }
  }


}
