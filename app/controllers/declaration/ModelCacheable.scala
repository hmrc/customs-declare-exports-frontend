/*
 * Copyright 2020 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package controllers.declaration

import models.ExportsDeclaration
import models.requests.JourneyRequest
import play.api.mvc.AnyContent
import services.cache.ExportsCacheService
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.{ExecutionContext, Future}

trait ModelCacheable {
  def exportsCacheService: ExportsCacheService

  protected def updateExportsDeclarationSyncDirect(
    update: ExportsDeclaration => ExportsDeclaration
  )(implicit hc: HeaderCarrier, ec: ExecutionContext, request: JourneyRequest[_]): Future[Option[ExportsDeclaration]] =
    exportsCacheService.update(update(request.cacheModel))

  protected def updateExportsDeclarationSync(
    update: ExportsDeclaration => Option[ExportsDeclaration]
  )(implicit hc: HeaderCarrier, ec: ExecutionContext, request: JourneyRequest[_]): Future[Option[ExportsDeclaration]] =
    update(request.cacheModel)
      .map(model => exportsCacheService.update(model))
      .getOrElse(Future.successful(None))

}
