package io.transwarp.aiops.plain.model

object ResStatus extends Enumeration {
  type ResStatus = Value
  val SUCCESS = Value("ok")
  val FAILURE = Value("failed")
  val DEFAULT = SUCCESS
}

class State {
  var status: ResStatus.ResStatus = ResStatus.DEFAULT
  var errMsg: String = _
}

class DocIdState extends State {
  var id: String = _
}

class SaveState extends DocIdState {
  var timestamp: String = _
  var userName: String = _
  var component: String = _
  var problem: String = _
  var solution: String = _
}

class SearchState extends State {
  val result = new SearchResult
  var query: String = _
  var timestamp: Long = _
}

class SearchByPageState extends SearchState {
  override val result = new SearchByPageResult
  var page: Int = _
  var size: Int = _
}

class SearchByIdState extends DocIdState {
  val result = new SearchResult
}

class SearchByConditionState(val startTime: Long,
                             val endTime: Long,
                             val component: String,
                             val page: Int,
                             val size: Int) extends State {
  val result = new SearchByPageResult
}

class StatisticsState(val startTime: Long, val endTime: Long) extends State {
  val result = new StatisticsResult
}

@Deprecated
class StatsState extends State {
  val result = new StatsResult
}
