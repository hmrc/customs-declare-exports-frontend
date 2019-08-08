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
import models.requests.{AuthenticatedRequest, JourneyRequest}
import play.api.mvc.AnyContent
import services.cache.ExportsCacheService

import scala.concurrent.{ExecutionContext, Future}

trait ModelCacheable {
  def exportsCacheService: ExportsCacheService

  protected def getAndUpdateExportCacheModel(
    sessionId: String,
    update: ExportsDeclaration => Future[Option[ExportsDeclaration]]
  )(implicit ec: ExecutionContext): Future[Option[ExportsDeclaration]] =
    exportsCacheService.get(sessionId).flatMap {
      case Some(model) => update(model)
      case _           => Future.successful(None)
    }
}

trait SessionIdAware {
  def journeySessionId(implicit request: JourneyRequest[_]) =
    request.authenticatedRequest.session.data("sessionId")

  def authenticatedSessionId(implicit request: AuthenticatedRequest[AnyContent]) =
    request.session.data("sessionId")
}
