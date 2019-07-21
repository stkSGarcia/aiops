package io.transwarp.aiops.plain.conf

import com.fasterxml.jackson.annotation.{JsonIgnoreProperties, JsonProperty}
import io.transwarp.aiops.ConfBean

@JsonIgnoreProperties(ignoreUnknown = true)
class EsConfBean extends ConfBean {
  @JsonProperty("host") var host: String = _
  @JsonProperty("port") var port: Int = _
  @JsonProperty("mines_local_data_dir") var minesLocalDataDir: String = _
  @JsonProperty("mines_data_dir") var minesDataDir: String = _
  @JsonProperty("mines_save_local_enable") var saveLocal: Boolean = _
  @JsonProperty("cluster_name") var clusterName: String = _
  @JsonProperty("data_index") var dataIndex: String = _
  @JsonProperty("query_index") var queryIndex: String = _
  @JsonProperty("type_name") var typeName: String = _
  @JsonProperty("search_page_size") var searchPageSize: Int = _
  @JsonProperty("bulk_size") var bulkSize: Int = _
  @JsonProperty("bucket_size") var bucketSize: Int = _
  @JsonProperty("full_txt_search_boost") var fullTxtSearchBoost: Int = _
  @JsonProperty("highlight_pre_tag") var highlightPreTag: String = _
  @JsonProperty("highlight_post_tag") var highlightPostTag: String = _
  @JsonProperty("top_answer_num") var topAnswerNum: Int = _
  @JsonProperty("top_inputer_num") var topInputerNum: Int = _
  @JsonProperty("short_query_threshold") var shortQueryThreshold: Int = _
  @JsonProperty("normal_query_threshold") var normalQueryThreshold: String = _
  @JsonProperty("exception_query_threshold") var exceptionQueryThreshold: String = _
  @JsonProperty("setting") var setting: Any = _
  @JsonProperty("data_mapping") var data_mapping: Any = _
  @JsonProperty("query_mapping") var query_mapping: Any = _
}
