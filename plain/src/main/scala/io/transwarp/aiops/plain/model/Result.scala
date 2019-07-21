package io.transwarp.aiops.plain.model

import scala.collection.mutable

trait Result

class SearchResult extends Result {
  var suggestions: Array[PlainDoc] = _
}

class SearchByPageResult extends SearchResult {
  var page: Int = _
  var total: Long = _
  var size: Int = _
}

class StatisticsResult extends Result {
  var totalDocs: Long = _
  var compStats: mutable.HashMap[String, Long] = _
  var personStats: Array[(String, Long)] = _
  var totalQueries: Long = _
  var intervalQueries: Array[(Long, Long)] = _
  var topAnswers: Array[PlainDoc] = _
  var topQueries: Array[String] = _
}

class StatsResult extends Result {
  // history statistics
  var docAmount: Int = _
  var docPerPerson: Array[(String, Int)] = _
  //  var docPerPerson: Map[String, Int] = _
  var docPerComp: Array[(String, Int)] = _
  //  var docPerComp: Map[String, Int] = _

  // new day statistics
  var docPerDay: Int = _
  //  var docPerPersonPerDay: Map[String, Int] = _
  var docPerPersonPerDay: Array[(String, Int)] = _

  // es meta stat
  var shardNum: Int = _
}
