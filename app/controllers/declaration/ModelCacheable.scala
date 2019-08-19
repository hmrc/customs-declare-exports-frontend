/*
 * Copyright 2019 HM Revenue & Customs
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
import services.cache.ExportsCacheService
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.{ExecutionContext, Future}

trait ModelCacheable {
  def exportsCacheService: ExportsCacheService

  @deprecated("Please use updateExportCacheModel", since = "2019-08-07")
  protected def getAndUpdateExportsDeclaration(
    sessionId: String,
    update: ExportsDeclaration => Future[Option[ExportsDeclaration]]
  )(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Option[ExportsDeclaration]] =
    exportsCacheService.get(sessionId).flatMap {
      case Some(model) => update(model)
      case _           => Future.successful(None)
    }

  protected def updateExportsDeclarationSyncDirect(
    update: ExportsDeclaration => ExportsDeclaration
  )(implicit hc: HeaderCarrier, ec: ExecutionContext, request: JourneyRequest[_]): Future[Option[ExportsDeclaration]] =
    exportsCacheService.update(update(request.cacheModel))

  protected def updateExportsDeclarationSync(
    update: ExportsDeclaration => Option[ExportsDeclaration]
  )(implicit hc: HeaderCarrier, ex: ExecutionContext, request: JourneyRequest[_]): Future[Option[ExportsDeclaration]] =
    update(request.cacheModel)
      .map(model => exportsCacheService.update(model))
      .getOrElse(Future.successful(None))

  protected def updateExportsDeclaration(
    update: ExportsDeclaration => Future[Option[ExportsDeclaration]]
  )(implicit hc: HeaderCarrier, ec: ExecutionContext, request: JourneyRequest[_]): Future[Option[ExportsDeclaration]] =
    update(request.cacheModel).flatMap { updatedModel =>
      updatedModel
        .map(model => exportsCacheService.update(model))
        .getOrElse(Future.successful(None))
    }
}
