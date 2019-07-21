package io.transwarp.aiops.plain.factory

import io.transwarp.aiops.plain.conf.EsConf
import io.transwarp.aiops.plain.model._
import io.transwarp.aiops.plain.service._

object DeleteFactory extends GeneralFactory[DocIdState] {
  addProcedure(new EsDeleteProcedure)
  addProcedure(new FileSystemDeleteProcedure)
  if (EsConf.getConf.saveLocal) addProcedure(new LocalFileDeleteProcedure)
}

object SaveFactory extends GeneralFactory[SaveState] {
  addProcedure(new EsSaveProcedure)
  addProcedure(new FileSystemSaveProcedure)
  if (EsConf.getConf.saveLocal) addProcedure(new LocalFileSaveProcedure)
}

object UpdateFactory extends GeneralFactory[SaveState] {
  addProcedure(new EsSaveProcedure)
  addProcedure(new FileSystemDeleteProcedure)
  addProcedure(new FileSystemSaveProcedure)
  if (EsConf.getConf.saveLocal) {
    addProcedure(new LocalFileDeleteProcedure)
    addProcedure(new LocalFileSaveProcedure)
  }
}

object SearchFactory extends GeneralFactory[SearchState] {
  addProcedure(new EsSearchProcedure)
  addProcedure(new EsQuerySaveProcedure)
}

object SearchByPageFactory extends GeneralFactory[SearchByPageState] {
  addProcedure(new EsSearchByPageProcedure)
  addProcedure(new EsQuerySaveProcedure)
}

object SearchByIdFactory extends GeneralFactory[SearchByIdState] {
  addProcedure(new EsSearchByIdProcedure)
}

object SearchByConditionFactory extends GeneralFactory[SearchByConditionState] {
  addProcedure(new EsSearchByConditionProcedure)
}

object StatisticsFactory extends GeneralFactory[StatisticsState] {
  addProcedure(new EsDocStatisticProcedure)
  addProcedure(new EsQueryStatisticsProcedure)
}

object ReloadFactory extends GeneralFactory[State] {
  addProcedure(new EsTruncateProcedure)
  addProcedure(new EsLoadAllProcedure)
}

object TruncateFactory extends GeneralFactory[State] {
  addProcedure(new EsTruncateProcedure)
}

@Deprecated
object StatsFactory extends GeneralFactory[StatsState] {
  // addProcedure(new EsDataStatsProcedure)
  // addProcedure(new EsMetaStatsProcedure)
  // addProcedure(new FileStatsProcedure)
}
