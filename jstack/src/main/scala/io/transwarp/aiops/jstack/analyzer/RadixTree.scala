package io.transwarp.aiops.jstack.analyzer

import io.transwarp.aiops.jstack.conf.JstackConf

import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer

private[jstack] class RadixTree[V] {
  private val root = new RadixTreeNode[V](Array())
  // Regex for splitting key
  private val splitPattern = "(?=[^0-9a-zA-Z]+)".r

  /**
    * Append a tuple of key and value.
    *
    * @param kv A tuple of key and value
    */
  def +=(kv: (String, V)): Unit = put(splitPattern split kv._1, kv._2, root)

  /**
    * ONLY for getting thread groups in jstack module.
    *
    * @return A map of thread groups
    */
  def getGroupMap: Map[String, ArrayBuffer[V]] = {
    val visitor: InternalVisitor[Map[String, ArrayBuffer[V]]] = new InternalVisitor[Map[String, ArrayBuffer[V]]] {
      val map = new mutable.HashMap[String, ArrayBuffer[V]]

      override def visit(node: RadixTreeNode[V], prefix: String): Boolean =
        if (prefix.length >= JstackConf.getConf.threadGroupNameMinLength
          && node.children.count(_.children.isEmpty) >= JstackConf.getConf.threadGroupMinSize) {
          val values = getAllValues(node)
          map.get(prefix) match {
            case Some(x) => x ++= values
            case None => map += prefix -> values
          }
          false
        } else true

      override def getResult: Map[String, ArrayBuffer[V]] = map.toMap

      private def getAllValues(node: RadixTreeNode[V]): ArrayBuffer[V] = node.value ++ node.children.flatMap(getAllValues)
    }
    visit(root, "", visitor)
    visitor.getResult
  }

  /**
    * Print this Radix tree.
    */
  def dump(): Unit = dump(root, "")

  private def put(key: Array[String], value: V, node: RadixTreeNode[V]): Unit = {
    val keyLen = key.length
    val nodeLen = node.prefix.length
    val prefixLen = (key zip node.prefix).takeWhile(Function.tupled(_ == _)).length

    if (prefixLen == nodeLen && prefixLen == keyLen) {
      node appendValue value
    } else if (prefixLen == 0 || (prefixLen < keyLen && prefixLen >= nodeLen)) {
      val leftoverKey = key drop prefixLen
      node.children.find(p => p.prefix.head == leftoverKey.head) match {
        case Some(x) => put(leftoverKey, value, x)
        case None => node.children += new RadixTreeNode[V](leftoverKey, value)
      }
    } else if (prefixLen < nodeLen) {
      // split node
      val newNode = new RadixTreeNode[V](node.prefix drop prefixLen, node.value)
      newNode.children ++= node.children

      node.prefix = node.prefix take prefixLen
      node.value.clear
      node.children.clear
      node.children += newNode

      if (prefixLen == keyLen) node appendValue value
      else node.children += new RadixTreeNode[V](key drop prefixLen, value)
    } else {
      node.children += new RadixTreeNode[V](key drop prefixLen, value)
    }
  }

  private def dump(node: RadixTreeNode[V], prefix: String): Unit = {
    if (node.prefix.isEmpty) println(s"${prefix}ROOT")
    else if (node.value.nonEmpty) println(s"$prefix${node.prefix.mkString} -> [${node.value.mkString(", ")}]")
    else println(s"$prefix${node.prefix.mkString}")

    val basePrefix = if (prefix endsWith "\\- ") prefix.substring(0, prefix.length - 3) + "   "
    else if (prefix endsWith "+- ") prefix.substring(0, prefix.length - 3) + "|  "
    else ""
    val childPrefix = basePrefix + "+- "

    if (node.children.nonEmpty) {
      node.children.init.foreach(dump(_, childPrefix))
      dump(node.children.last, basePrefix + "\\- ")
    }
  }

  private def visit(node: RadixTreeNode[V], prefix: String, visitor: InternalVisitor[_]): Unit = {
    val shouldContinue = visitor.visit(node, prefix)
    if (shouldContinue) node.children.foreach(f => visit(f, prefix + f.prefix.mkString, visitor))
  }

  /**
    * An internal trait for traversing the Radix tree.
    *
    * @tparam R Result type
    */
  private trait InternalVisitor[R] {
    /**
      * Actions when visiting the node.
      *
      * @param node   The node visited
      * @param prefix The prefix of the node
      * @return Whether to traverse its children
      */
    def visit(node: RadixTreeNode[V], prefix: String): Boolean

    /**
      * Get the result after traversing.
      *
      * @return
      */
    def getResult: R
  }

}

private[jstack] class RadixTreeNode[V](var prefix: Array[String]) {
  val children = new ArrayBuffer[RadixTreeNode[V]]
  var value = new ArrayBuffer[V]

  def this(prefix: Array[String], value: V) {
    this(prefix)
    this.value += value
  }

  def this(prefix: Array[String], value: ArrayBuffer[V]) {
    this(prefix)
    this.value.clear
    this.value ++= value
  }

  def appendValue(value: V): Unit = this.value += value
}
