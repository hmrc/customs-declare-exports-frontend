package services.mapping

import services.cache.ExportsCacheModel

trait Builder[T] {

  def buildThenAdd(model: ExportsCacheModel, t: T): Unit

}
