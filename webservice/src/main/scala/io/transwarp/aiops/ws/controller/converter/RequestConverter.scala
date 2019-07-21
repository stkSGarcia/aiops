//package io.transwarp.aiops.ws.controller.converter
//
//import io.transwarp.aiops.plain.state.{DeleteState, SaveState, SearchState, StatisticsState}
//import io.transwarp.aiops.ws.controller.request._
//
///**
//  * Created by hippo on 9/4/18.
//  */
//abstract class RequestConverter[T <: WebRequest, E](source: T) {
//  def convert: E
//
//}
//
//package object ImplicitPlainReqConverter {
//
//  implicit class PlainSaveReqConverter(source: PlainSaveReq) extends RequestConverter[PlainSaveReq, SaveState](source) {
//    override def convert: SaveState = {
//      new SaveState(source.userName, source.component, source.problemDetails, source.solution)
//    }
//  }
//
//  implicit class PlainUpdateReqConverter(source: PlainUpdateReq) extends RequestConverter[PlainUpdateReq, SaveState](source) {
//    override def convert: SaveState = {
//      throw new RuntimeException("Unsupported Method")
//    }
//
//    def convert2Delete: DeleteState = {
//      new DeleteState(source.id)
//    }
//
//    def convert2Save: SaveState = {
//      new SaveState(source.userName, source.component, source.problemDetails, source.solution)
//    }
//  }
//
//
//  implicit class PlainSearchReqConverter(source: PlainSearchReq) extends RequestConverter[PlainSearchReq, SearchState](source) {
//    override def convert: SearchState = {
//      new SearchState(source.query, source.pageId)
//    }
//  }
//
//}
