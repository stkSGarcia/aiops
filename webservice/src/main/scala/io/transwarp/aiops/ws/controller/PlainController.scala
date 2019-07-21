package io.transwarp.aiops.ws.controller

import io.swagger.annotations.{Api, ApiOperation, ApiParam}
import io.transwarp.aiops.ws.controller.request._
import io.transwarp.aiops.ws.controller.response._
import io.transwarp.aiops.ws.service.PlainService
import javax.annotation.Resource
import org.springframework.web.bind.annotation._

@Api(value = "plain", description = "知识库相关操作")
@RestController
@RequestMapping(path = Array(PathV1.PLAIN))
class PlainController {
  @Resource
  private var plainService: PlainService = _

  @ApiOperation(value = "问题录入", notes = "录入新的问题到知识库")
  @RequestMapping(method = Array(RequestMethod.PUT))
  def saveDoc(@ApiParam(value = "问题及解决方案详细描述", required = true)
              @RequestBody req: PlainSaveReq): RestResponse.NullType = {
    plainService.save(req.userName, req.component, req.problem, req.solution)
  }

  @ApiOperation(value = "问题更新", notes = "更新问题到知识库")
  @RequestMapping(method = Array(RequestMethod.PATCH))
  def updateDoc(@ApiParam(value = "问题及解决方案详细描述", required = true)
                @RequestBody req: PlainUpdateReq): RestResponse.NullType = {
    plainService.update(req.id, req.userName, req.component, req.problem, req.solution)
  }

  @ApiOperation(value = "问题查询", notes = "查询问题")
  @RequestMapping(method = Array(RequestMethod.POST))
  def searchDoc(@ApiParam(value = "问题描述", required = true)
                @RequestBody req: PlainSearchReq): RestResponse[PlainSearchRes] = {
    plainService.search(req.query)
  }

  @ApiOperation(value = "[内部]问题查询（分页）", notes = "分页查询问题")
  @RequestMapping(value = Array("/page"), method = Array(RequestMethod.POST))
  def searchDocByPage(@ApiParam(value = "问题描述", required = true)
                      @RequestBody req: PlainSearchByPageReq): RestResponse[PlainSearchByPageRes] = {
    plainService.searchByPage(req.query, req.page, req.size)
  }

  @ApiOperation(value = "问题删除", notes = "删除指定问题")
  @RequestMapping(method = Array(RequestMethod.DELETE))
  def delete(@ApiParam(value = "待删除问题ID", required = true)
             @RequestParam id: String): RestResponse.NullType = {
    plainService.delete(id)
  }

  @ApiOperation(value = "[内部]问题ID查询", notes = "查询问题ID")
  @RequestMapping(value = Array("/id"), method = Array(RequestMethod.GET))
  def searchDocById(@ApiParam(value = "问题ID", required = true)
                    @RequestParam id: String): RestResponse[PlainSearchRes] = {
    plainService.searchById(id)
  }

  @ApiOperation(value = "[内部]条件查询", notes = "通过条件查询问题")
  @RequestMapping(value = Array("/condition"), method = Array(RequestMethod.POST))
  def searchDocById(@ApiParam(value = "问题条件", required = true)
                    @RequestBody req: PlainSearchByConditionReq): RestResponse[PlainSearchByPageRes] = {
    plainService.searchByCondition(req.startTime, req.endTime, req.component, req.page, req.size)
  }

  @ApiOperation(value = "[内部]ES数据删除", notes = "[内部]ES数据删除")
  @RequestMapping(value = Array("/es"), method = Array(RequestMethod.DELETE))
  def truncate: RestResponse.NullType = {
    plainService.truncate
  }

  @ApiOperation(value = "[内部]ES数据重导", notes = "[内部]ES数据删除并重新导入")
  @RequestMapping(value = Array("/es"), method = Array(RequestMethod.PUT))
  def reload: RestResponse.NullType = {
    plainService.reload
  }

  @ApiOperation(value = "[内部]知识库统计信息(临时)", notes = "[内部]获取知识库统计信息")
  @RequestMapping(value = Array("/stats"), method = Array(RequestMethod.GET))
  def stats: RestResponse[PlainStatsRes] = {
    plainService.stats
  }

  @ApiOperation(value = "[内部]知识库统计信息(仪表盘)", notes = "[内部]获取知识库统计信息")
  @RequestMapping(value = Array("/statistics"), method = Array(RequestMethod.GET))
  def statistics(@ApiParam(value = "开始时间", required = true)
                 @RequestParam startTime: Long,
                 @ApiParam(value = "结束时间", required = true)
                 @RequestParam endTime: Long): RestResponse[PlainStatisticsRes] = {
    plainService.statistics(startTime, endTime)
  }
}
