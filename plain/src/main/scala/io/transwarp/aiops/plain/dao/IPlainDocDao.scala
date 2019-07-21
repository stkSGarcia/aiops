package io.transwarp.aiops.plain.dao

import io.transwarp.aiops.plain.dao.DaoRes.DaoRes
import io.transwarp.aiops.plain.model.{PlainDoc, SearchResult}

import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer

object DaoRes extends Enumeration {
  type DaoRes = Value
  val Success = Value(0)
  val Failure = Value(1)
}

case class DaoSearchRes(status: DaoRes, result: Array[PlainDoc], total: Long)

case class DaoMetaRes(status: DaoRes, docAmount: Int, shardNum: Int)

case class DaoAggRes(status: DaoRes, aggBuffer: ArrayBuffer[(String, Int)])

case class DaoDocStatsRes(status: DaoRes,
                          totalDocs: Long,
                          compStats: mutable.HashMap[String, Long],
                          personStats: Array[(String, Long)])

case class DaoQueryStatsRes(status: DaoRes,
                            totalQueries: Long,
                            intervalQueries: Array[(Long, Long)],
                            topAnswers: Array[PlainDoc],
                            topQueries: Array[String])

trait IPlainDocDao {
  def save(doc: PlainDoc): DaoRes

  /**
    * Save multiple items at a time
    *
    * @param map each item is made up of id and doc content(in json form)
    *
    */
  def bulkSave(map: mutable.Map[String, String]): DaoRes

  def createIndex(idx: String, mapping: String): DaoRes

  def deleteById(id: String): DaoRes

  def deleteIndex(id: String): DaoRes

  def searchById(id: String): DaoSearchRes

  def searchByCondition(startTime: Long, endTime: Long, component: String, page: Int, size: Int): DaoSearchRes

  /**
    * Seach component, problem and solution to match the query
    *
    * @param query search query
    * @param from  page offset
    * @param size  page size
    * @return search status and search results
    *
    */
  def fullTxtPageSearch(query: String, from: Int, size: Int): DaoSearchRes

  def fullTxtCompoundPageSearch(normalQuery: String, exceptionQuery: String, from: Int, size: Int): DaoSearchRes

  def scanPageSearch(from: Int, size: Int): DaoSearchRes

  def getMeta(): DaoMetaRes

  def aggDocByPerson(): DaoAggRes

  def aggDocByComp(): DaoAggRes

  def docStatistics(startTime: Long, endTime: Long): DaoDocStatsRes

  def queryStatistics(startTime: Long, endTime: Long): DaoQueryStatsRes

  def querySave(timestamp: Long, query: String, searchResult: SearchResult): DaoRes

  //TODO: implement detailed search
  //  def fullTxtSearch(query: String): RestStatus
  //  def searchByUserName(usrName: String): RestStatus
  //
  //  def searchById(id: String): RestStatus
}
