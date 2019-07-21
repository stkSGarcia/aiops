package io.transwarp.aiops.ws.service

import io.transwarp.aiops.ws.controller.response._

trait PlainService {
  def save(userName: String,
           component: String,
           problemDetails: String,
           solution: String): RestResponse.NullType

  def update(id: String,
             userName: String,
             component: String,
             problemDetails: String,
             solution: String): RestResponse.NullType

  def search(query: String): RestResponse[PlainSearchRes]

  def searchByPage(query: String, page: Int, size: Int): RestResponse[PlainSearchByPageRes]

  def searchById(id: String): RestResponse[PlainSearchRes]

  def searchByCondition(startTime: Long,
                        endTime: Long,
                        component: String,
                        page: Int,
                        size: Int): RestResponse[PlainSearchByPageRes]

  def delete(id: String): RestResponse.NullType

  def truncate: RestResponse.NullType

  def reload: RestResponse.NullType

  def stats: RestResponse[PlainStatsRes]

  def statistics(startTime: Long, endTime: Long): RestResponse[PlainStatisticsRes]
}
