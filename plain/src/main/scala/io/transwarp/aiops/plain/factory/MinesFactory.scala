package io.transwarp.aiops.plain.factory

object MinesFactory {
  def save: SaveFactory.type = SaveFactory

  def update: UpdateFactory.type = UpdateFactory

  def search: SearchFactory.type = SearchFactory

  def searchByPage: SearchByPageFactory.type = SearchByPageFactory

  def delete: DeleteFactory.type = DeleteFactory

  def stats: StatsFactory.type = StatsFactory

  def statistics: StatisticsFactory.type = StatisticsFactory

  def searchById: SearchByIdFactory.type = SearchByIdFactory

  def searchByCondition: SearchByConditionFactory.type = SearchByConditionFactory

  def esTruncate: TruncateFactory.type = TruncateFactory

  def esReload: ReloadFactory.type = ReloadFactory
}
