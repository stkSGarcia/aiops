package io.transwarp.aiops.plain.dao

import com.alibaba.fastjson.JSON
import io.transwarp.aiops.plain.conf.EsConf
import io.transwarp.aiops.plain.dao.DaoRes.DaoRes
import io.transwarp.aiops.plain.model._
import org.elasticsearch.common.settings.Settings
import org.elasticsearch.common.xcontent.XContentFactory
import org.elasticsearch.index.query.QueryBuilders._
import org.elasticsearch.index.query.{MultiMatchQueryBuilder, Operator, QueryBuilders}
import org.elasticsearch.rest.RestStatus
import org.elasticsearch.search.aggregations.AggregationBuilders
import org.elasticsearch.search.aggregations.bucket.global.Global
import org.elasticsearch.search.aggregations.bucket.histogram.{DateHistogramInterval, Histogram}
import org.elasticsearch.search.aggregations.bucket.terms.{StringTerms, Terms}
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder
import org.elasticsearch.search.sort.SortOrder
import org.joda.time.DateTime

import scala.collection.JavaConversions._
import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer
import scala.util.{Failure, Success, Try}

object PlainDocDao extends IPlainDocDao {
  implicit def boolean2DaoRes(status: Boolean): DaoRes.DaoRes = if (status) DaoRes.Success else DaoRes.Failure

  override def save(doc: PlainDoc): DaoRes = {
    val docInfo = XContentFactory.jsonBuilder
      .startObject
      .field(PlainDocField.TIMESTAMP, doc.timestamp)
      .field(PlainDocField.USERNAME, doc.userName)
      .field(PlainDocField.COMPONENT, doc.component)
      .field(PlainDocField.PROBLEM, doc.problem)
      .field(PlainDocField.SOLUTION, doc.solution)
      .endObject
    val response = EsClient.getThreadLocal
      .prepareIndex(EsConf.getConf.dataIndex, EsConf.getConf.typeName, doc.id)
      .setSource(docInfo).get
    response.status == RestStatus.CREATED || response.status == RestStatus.OK
  }

  override def bulkSave(map: mutable.Map[String, String]): DaoRes = {
    val client = EsClient.getThreadLocal
    val bulkRequest = client.prepareBulk()

    map.foreach { case (id, source) =>
      bulkRequest.add(client.prepareIndex(EsConf.getConf.dataIndex, EsConf.getConf.typeName, id).setSource(source))
    }
    val bulkRes = bulkRequest.get
    !bulkRes.hasFailures
  }

  override def deleteById(id: String): DaoRes = {
    val deleteRes = EsClient.getThreadLocal.prepareDelete(EsConf.getConf.dataIndex, EsConf.getConf.typeName, id).get
    deleteRes.status match {
      case RestStatus.OK => DaoRes.Success
      case _ => DaoRes.Failure
    }
  }

  override def deleteIndex(idx: String): DaoRes = {
    Try(EsClient.getThreadLocal.admin.indices.prepareDelete(idx).get) match {
      case Success(deleteRes) => deleteRes.isAcknowledged
      case Failure(_) => false
    }
  }

  override def fullTxtCompoundPageSearch(normalQuery: String, exceptionQuery: String, from: Int, size: Int): DaoSearchRes = {
    val client = EsClient.getThreadLocal
    val highlightBuilder = (new HighlightBuilder)
      .preTags(EsConf.getConf.highlightPreTag)
      .postTags(EsConf.getConf.highlightPostTag)
      .field(PlainDocField.COMPONENT)
      .field(PlainDocField.PROBLEM)
      .field(PlainDocField.SOLUTION)
      .numOfFragments(0)

    def effectiveQuery(query: String): Boolean = {
      query != null && query.length != 0 && query != ""
    }

    assert(effectiveQuery(normalQuery) || effectiveQuery(exceptionQuery))

    // val shortQueryThreshold = 10
    // val normalQB = if (effectiveQuery(normalQuery)) {
    //   if (effectiveQuery(exceptionQuery) || normalQuery.length <= shortQueryThreshold) {
    //     QueryBuilders.multiMatchQuery(normalQuery)
    //       .field(PlainDoc.Id)
    //       .field(PlainDoc.Problem, EsConf.fullTxtSearchBoost)
    //       .field(PlainDoc.Component, EsConf.fullTxtSearchBoost)
    //       .field(PlainDoc.Solution)
    //       .`type`(MultiMatchQueryBuilder.Type.CROSS_FIELDS)
    //   } else {
    //     QueryBuilders.multiMatchQuery(normalQuery)
    //       .field(PlainDoc.Id)
    //       .field(PlainDoc.Problem, EsConf.fullTxtSearchBoost)
    //       .field(PlainDoc.Component, EsConf.fullTxtSearchBoost)
    //       .field(PlainDoc.Solution)
    //       .`type`(MultiMatchQueryBuilder.Type.CROSS_FIELDS)
    //       .minimumShouldMatch("65%")
    //   }
    // } else {
    //   null
    // }

    val qb = QueryBuilders.boolQuery

    if (effectiveQuery(normalQuery)) {
      qb.should(multiMatchQuery(normalQuery)
        .field(PlainDocField.COMPONENT, 1)
        .field(PlainDocField.PROBLEM, 4)
        .field(PlainDocField.SOLUTION, 2)
        .`type`(MultiMatchQueryBuilder.Type.CROSS_FIELDS)
        .minimumShouldMatch(if (normalQuery.length <= EsConf.getConf.shortQueryThreshold) EsConf.getConf.normalQueryThreshold
        else "100%")
      )
    }

    if (effectiveQuery(exceptionQuery)) {
      qb.must(commonTermsQuery(PlainDocField.PROBLEM, exceptionQuery)
        .lowFreqMinimumShouldMatch(EsConf.getConf.exceptionQueryThreshold)
        .lowFreqOperator(Operator.AND))
    }

    // val exceptionQB = if (effectiveQuery(exceptionQuery)) {
    //   QueryBuilders.matchQuery(PlainDoc.Problem, exceptionQuery)
    //     .minimumShouldMatch("100%")
    // } else null

    // val qb = QueryBuilders.boolQuery()
    //
    // if (exceptionQB != null) {
    //   qb.must(exceptionQB)
    // }
    // if (normalQB != null) {
    //   qb.should(normalQB)
    // }

    val searchReqBuilder = client.prepareSearch(EsConf.getConf.dataIndex).setTypes(EsConf.getConf.typeName).setQuery(qb)
      .highlighter(highlightBuilder).setFrom(from).setSize(size)

    val searchRes = searchReqBuilder.get
    val searchHits = searchRes.getHits

    if (searchRes.status == RestStatus.OK) {
      var docArray = new Array[PlainDoc](size)
      val hits = searchHits.getHits
      val docNum = searchHits.totalHits

      hits.zipWithIndex.foreach {
        case (hit, index) =>
          val msb = JSON.parseObject(hit.getSourceAsString, classOf[Doc])
          val docItem = new PlainDoc(hit.getId, msb.timestamp, msb.userName, msb.component, msb.problemDetails, msb.solution)
          hit.getHighlightFields.foreach { case (name, field) =>
            docItem.setField(name, field.getFragments()(0).toString)
          }
          docArray(index) = docItem
      }

      if (hits.length < size) {
        docArray = docArray.filter(_ != null)
      }
      DaoSearchRes(DaoRes.Success, docArray, docNum)
    } else {
      DaoSearchRes(DaoRes.Failure, null, 0)
    }
  }

  @Deprecated
  override def fullTxtPageSearch(query: String, from: Int, size: Int): DaoSearchRes = {
    val client = EsClient.getThreadLocal
    val highlightBuilder = (new HighlightBuilder)
      .preTags(EsConf.getConf.highlightPreTag)
      .postTags(EsConf.getConf.highlightPostTag)
      .field(PlainDocField.PROBLEM)
      .field(PlainDocField.COMPONENT)
      .field(PlainDocField.SOLUTION)
      .numOfFragments(0)

    assert(query != null && query.length != 0)
    val qb = multiMatchQuery(query)
      .field(PlainDocField.ID)
      .field(PlainDocField.PROBLEM, EsConf.getConf.fullTxtSearchBoost)
      .field(PlainDocField.COMPONENT, EsConf.getConf.fullTxtSearchBoost)
      .field(PlainDocField.SOLUTION)
      .`type`(MultiMatchQueryBuilder.Type.CROSS_FIELDS)
      .minimumShouldMatch("80%")

    val searchReqBuilder = client.prepareSearch(EsConf.getConf.dataIndex).setTypes(EsConf.getConf.typeName).setQuery(qb)
      .highlighter(highlightBuilder).setFrom(from).setSize(size)

    val searchRes = searchReqBuilder.get
    val searchHits = searchRes.getHits

    if (searchRes.status == RestStatus.OK) {
      var docArray = new Array[PlainDoc](size)
      val hits = searchHits.getHits
      val docNum = searchHits.totalHits

      hits.zipWithIndex.foreach {
        case (hit, index) =>
          val msb = JSON.parseObject(hit.getSourceAsString, classOf[Doc])
          val docItem = new PlainDoc(hit.getId, msb.timestamp, msb.userName, msb.component, msb.problemDetails, msb.solution)
          hit.getHighlightFields.foreach { case (name, field) =>
            docItem.setField(name, field.getFragments()(0).toString)
          }
          docArray(index) = docItem
      }

      if (hits.length < size) {
        docArray = docArray.filter(_ != null)
      }
      DaoSearchRes(DaoRes.Success, docArray, docNum)
    } else {
      DaoSearchRes(DaoRes.Failure, null, 0)
    }
  }

  override def scanPageSearch(from: Int, size: Int): DaoSearchRes = {
    val client = EsClient.getThreadLocal
    val highlightBuilder = (new HighlightBuilder)
      .preTags(EsConf.getConf.highlightPreTag)
      .postTags(EsConf.getConf.highlightPostTag)
      .field(PlainDocField.PROBLEM)
      .field(PlainDocField.COMPONENT)
      .field(PlainDocField.SOLUTION)
      .numOfFragments(0)

    val searchReqBuilder = client.prepareSearch(EsConf.getConf.dataIndex).setTypes(EsConf.getConf.typeName).setQuery(QueryBuilders
      .matchAllQuery()).highlighter(highlightBuilder).setFrom(from).setSize(size)

    val searchRes = searchReqBuilder.get
    val searchHits = searchRes.getHits

    if (searchRes.status == RestStatus.OK) {
      val hits = searchHits.getHits
      val docNum = searchHits.totalHits

      val docArray = hits.map(hit => {
        val msb = JSON.parseObject(hit.getSourceAsString, classOf[Doc])
        if (msb != null) {
          val docItem = new PlainDoc(hit.getId, msb.timestamp, msb.userName, msb.component, msb.problemDetails, msb.solution)
          hit.getHighlightFields.foreach { case (name, field) =>
            docItem.setField(name, field.getFragments()(0).toString)
          }
          docItem
        } else null
      })

      DaoSearchRes(DaoRes.Success, docArray, docNum)
    } else {
      DaoSearchRes(DaoRes.Failure, null, 0)
    }
  }

  override def createIndex(idx: String, mapping: String): DaoRes = {
    val createIndexRes = EsClient.getThreadLocal.admin.indices.prepareCreate(idx)
      .setSettings(Settings.builder.loadFromSource(EsConf.getJsonString(EsConf.getConf.setting)))
      .addMapping(EsConf.getConf.typeName, mapping)
      .get
    createIndexRes.isAcknowledged
  }

  @Deprecated
  override def getMeta(): DaoMetaRes = {
    val client = EsClient.getThreadLocal
    val statRes = client.admin().indices().prepareStats("es_data").get
    DaoMetaRes(DaoRes.Success, statRes.getPrimaries.getDocs.getCount.toInt, statRes.getShards.length)
  }

  @Deprecated
  override def aggDocByPerson(): DaoAggRes = {
    val client = EsClient.getThreadLocal
    val aggGlobalName = "global_by_person"
    val aggTermName = "by_person"
    val searchRes = client.prepareSearch(EsConf.getConf.dataIndex).setTypes(EsConf.getConf.typeName)
      .addAggregation(AggregationBuilders.global(aggGlobalName).subAggregation(
        AggregationBuilders.terms(aggTermName).field(PlainDocField.USERNAME).order(Terms.Order.count(false))
          .size(EsConf.getConf.bucketSize))
      ).get

    if (searchRes.status == RestStatus.OK) {
      val buckets = searchRes.getAggregations.get(aggGlobalName).asInstanceOf[Global].getAggregations.get(aggTermName)
        .asInstanceOf[StringTerms].getBuckets
      val aggBuffer = new ArrayBuffer[(String, Int)]

      def updateBuffer(bucket: Terms.Bucket): Unit = {
        aggBuffer += ((bucket.getKeyAsString, bucket.getDocCount.toInt))
      }

      buckets.foreach(updateBuffer)
      DaoAggRes(DaoRes.Success, aggBuffer)
    }
    else {
      DaoAggRes(DaoRes.Failure, null)
    }
  }

  @Deprecated
  override def aggDocByComp(): DaoAggRes = {
    // val client = EsClient.getThreadLocal
    // val CompSet = PlainDoc.CompSet
    // val filterAggName = "docByComp"
    // val keyFilters = new Array[KeyedFilter](CompSet.length)
    // CompSet.zipWithIndex.foreach {
    //   case (name, id) => {
    //     keyFilters(id) = new FiltersAggregator.KeyedFilter(name, QueryBuilders.matchQuery("component", name))
    //   }
    // }

    // val searchRes = client.prepareSearch(EsConf.dataIndex).setTypes(EsConf.typeName)
    //   .addAggregation(AggregationBuilders.filters(filterAggName, keyFilters: _*)).get

    // if (searchRes.status == RestStatus.OK) {
    //   val buffer = new ArrayBuffer[(String, Int)]
    //   val test: Filters = searchRes.getAggregations.get(filterAggName)

    //   val buckets = test.getBuckets

    //   def updateBuffer(item: Filters.Bucket): Unit = {
    //     buffer += ((item.getKeyAsString, item.getDocCount.toInt))
    //   }

    //   buckets.foreach(updateBuffer)
    //   DaoAggRes(DaoRes.Success, buffer.sortWith((x, y) => x._2 > y._2))
    // } else {
    //   DaoAggRes(DaoRes.Failure, null)
    // }
    DaoAggRes(DaoRes.Failure, null)
  }

  override def docStatistics(startTime: Long, endTime: Long): DaoDocStatsRes = {
    val compAggName = "comp"
    val personAggName = "person"

    val client = EsClient.getThreadLocal
    val compAgg = AggregationBuilders
      .terms(compAggName)
      .field(PlainDocField.COMPONENT_KEYWORD)
      .size(EsConf.getConf.bucketSize)
    val personAgg = AggregationBuilders
      .terms(personAggName)
      .field(PlainDocField.USERNAME)
      .size(EsConf.getConf.topInputerNum)
      .order(Terms.Order.count(false))
    val response = if (startTime != -1 || endTime != -1) {
      val qb = boolQuery.filter(rangeQuery(PlainDocField.TIMESTAMP).gte(startTime).lte(endTime))
      client
        .prepareSearch(EsConf.getConf.dataIndex)
        .setTypes(EsConf.getConf.typeName)
        .setSize(0)
        .setQuery(qb)
        .addAggregation(compAgg)
        .addAggregation(personAgg)
        .get
    } else {
      client
        .prepareSearch(EsConf.getConf.dataIndex)
        .setTypes(EsConf.getConf.typeName)
        .setSize(0)
        .addAggregation(compAgg)
        .addAggregation(personAgg)
        .get
    }
    if (response.status == RestStatus.OK) {
      // document statistics grouped by component
      val compStats = new mutable.HashMap[String, Long]()
      response.getAggregations.get(compAggName).asInstanceOf[StringTerms].getBuckets
        .foreach(entry => {
          val components = entry.getKeyAsString.split(",")
          val count = entry.getDocCount
          components.foreach(comp => {
            // FIXME need standard
            val compString = comp.trim
            val component = if (compString.startsWith("comp_")) {
              compString.substring(5).toUpperCase // erase "comp_"
            } else {
              compString.toUpperCase
            }
            if (compStats.containsKey(component))
              compStats += component -> (compStats(component) + count)
            else
              compStats += component -> count
          })
        })
      // number of documents
      val totalDocs = response.getHits.totalHits
      // document statistics grouped by person
      val personStats = response.getAggregations.get(personAggName).asInstanceOf[StringTerms].getBuckets
        .map(entry => (entry.getKeyAsString, entry.getDocCount)).toArray

      DaoDocStatsRes(DaoRes.Success, totalDocs, compStats, personStats)
    } else DaoDocStatsRes(DaoRes.Failure, -1, null, null)
  }

  override def queryStatistics(startTime: Long, endTime: Long): DaoQueryStatsRes = {
    val idAggName = "ids"
    val dateAggName = "intervals"

    val client = EsClient.getThreadLocal
    val idAgg = AggregationBuilders
      .terms(idAggName)
      .field(QueryDocField.DOCID)
      .size(EsConf.getConf.bucketSize)
    val timeAgg = AggregationBuilders
      .dateHistogram(dateAggName)
      .field(QueryDocField.TIMESTAMP)
      .dateHistogramInterval(
        if (startTime == -1 || endTime == -1) DateHistogramInterval.DAY
        else if (endTime - startTime <= 86400000) DateHistogramInterval.HOUR
        else DateHistogramInterval.DAY
      )
    val response = if (startTime != -1 || endTime != -1) {
      val qb = boolQuery
        .filter(rangeQuery(QueryDocField.TIMESTAMP).gte(startTime).lte(endTime))
      client
        .prepareSearch(EsConf.getConf.queryIndex)
        .setTypes(EsConf.getConf.typeName)
        .setQuery(qb)
        .addAggregation(idAgg)
        .addAggregation(timeAgg)
        .get
    } else {
      client
        .prepareSearch(EsConf.getConf.queryIndex)
        .setTypes(EsConf.getConf.typeName)
        .addAggregation(idAgg)
        .addAggregation(timeAgg)
        .get
    }
    if (response.status == RestStatus.OK) {
      // top answers
      val idMap = mutable.HashMap[String, Long]()
      response.getAggregations.get(idAggName).asInstanceOf[StringTerms].getBuckets
        .foreach(entry => {
          val idString = entry.getKeyAsString.trim
          if (idString != "") {
            val ids = idString.split(",")
            val count = entry.getDocCount
            ids.foreach(id => {
              if (idMap.containsKey(id)) idMap += id -> (idMap(id) + count)
              else idMap += id -> count
            })

          }
        })
      val topIds = idMap.toArray.sortBy(_._2).reverse.map(_._1)
      val topAnswers = new ArrayBuffer[PlainDoc]()
      val length = topIds.length
      var answerNum = 0
      var index = 0
      while (answerNum < EsConf.getConf.topAnswerNum && index < length) {
        assert(topIds(index) != "")
        val searchRes = searchById(topIds(index))
        if (searchRes.status == DaoRes.Success) {
          topAnswers += searchRes.result.head
          answerNum += 1
        }
        index += 1
      }
      // time intervals
      val totalQueries = response.getHits.totalHits
      val intervalQueries = response.getAggregations.get(dateAggName).asInstanceOf[Histogram].getBuckets
        .map(entry => {
          val interval = entry.getKey.asInstanceOf[DateTime].getMillis
          val count = entry.getDocCount
          (interval, count)
        }).toArray
      DaoQueryStatsRes(DaoRes.Success, totalQueries, intervalQueries, topAnswers.toArray, null)
    } else DaoQueryStatsRes(DaoRes.Failure, -1, null, null, null)
  }

  override def searchById(id: String): DaoSearchRes = {
    val client = EsClient.getThreadLocal
    val searchRes = client
      .prepareSearch(EsConf.getConf.dataIndex)
      .setTypes(EsConf.getConf.typeName)
      .setQuery(termQuery(PlainDocField.ID, id))
      .setFrom(0)
      .setSize(1)
      .get
    if (searchRes.status == RestStatus.OK) {
      val searchHits = searchRes.getHits
      val hits = searchHits.getHits
      val docNum = searchHits.totalHits

      if (hits.nonEmpty) {
        var docArray = new ArrayBuffer[PlainDoc]()
        hits.foreach(hit => {
          val msb = JSON.parseObject(hit.getSourceAsString, classOf[Doc])
          docArray += new PlainDoc(hit.getId, msb.timestamp, msb.userName, msb.component, msb.problemDetails, msb.solution)
        })
        DaoSearchRes(DaoRes.Success, docArray.toArray, docNum)
      } else DaoSearchRes(DaoRes.Failure, null, 0)
    } else DaoSearchRes(DaoRes.Failure, null, 0)
  }

  override def querySave(timestamp: Long, query: String, searchResult: SearchResult): DaoRes = {
    val topDoc = searchResult.suggestions.slice(0, EsConf.getConf.topAnswerNum)
    val docId = topDoc.map(_.id).mkString(",")
    val client = EsClient.getThreadLocal
    val response = client
      .prepareIndex(EsConf.getConf.queryIndex, EsConf.getConf.typeName)
      .setSource(XContentFactory.jsonBuilder
        .startObject
        .field(QueryDocField.TIMESTAMP, timestamp)
        .field(QueryDocField.DOCID, docId)
        .field(QueryDocField.QUERY, query)
        .endObject)
      .get
    response.status == RestStatus.OK || response.status == RestStatus.CREATED
  }

  override def searchByCondition(startTime: Long, endTime: Long, component: String, page: Int, size: Int): DaoSearchRes = {
    val client = EsClient.getThreadLocal
    val qb = if (startTime != -1 && endTime != -1) {
      boolQuery.filter(rangeQuery(PlainDocField.TIMESTAMP).gte(startTime).lte(endTime))
        .filter(matchQuery(PlainDocField.COMPONENT, component))
    } else {
      boolQuery.filter(matchQuery(PlainDocField.COMPONENT, component))
    }
    val response = client
      .prepareSearch(EsConf.getConf.dataIndex)
      .setTypes(EsConf.getConf.typeName)
      .setQuery(qb)
      .addSort(PlainDocField.TIMESTAMP, SortOrder.DESC)
      .setFrom(page)
      .setSize(size)
      .get

    if (response.status == RestStatus.OK) {
      val searchHits = response.getHits
      val hits = searchHits.getHits
      val docNum = searchHits.totalHits

      val docArray = hits.map(hit => {
        val msb = JSON.parseObject(hit.getSourceAsString, classOf[Doc])
        new PlainDoc(hit.getId, msb.timestamp, msb.userName, msb.component, msb.problemDetails, msb.solution)
      })

      DaoSearchRes(DaoRes.Success, docArray, docNum)
    } else DaoSearchRes(DaoRes.Failure, null, -1)
  }
}
