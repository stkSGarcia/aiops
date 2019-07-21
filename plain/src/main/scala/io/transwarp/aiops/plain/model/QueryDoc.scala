package io.transwarp.aiops.plain.model

case class QueryDoc(timestamp: String, docId: String, query: String)

object QueryDocField {
  val TIMESTAMP: String = "timestamp"
  val DOCID: String = "docId"
  val QUERY: String = "query"
}
