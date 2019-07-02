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

import models.requests.{AuthenticatedRequest, JourneyRequest}
import play.api.mvc.AnyContent
import services.cache.{ExportsCacheModel, ExportsCacheService}

import scala.concurrent.{ExecutionContext, Future}

trait ModelCacheable {
  val cacheService: ExportsCacheService

  /*
  Compilation warning
    match may not be exhaustive.
    [warn] It would fail on the following input: Left(_)
   */
  protected def getAndUpdateExportCacheModel(
    sessionId: String,
    update: ExportsCacheModel => Future[Either[String, ExportsCacheModel]]
  )(implicit ec: ExecutionContext): Future[Either[String, ExportsCacheModel]] =
    cacheService.get(sessionId).flatMap {
      case Right(model) => update(model)
      case Left(_)      => Future.successful(Left("Unable to retrieve model from cache"))
    }

}

trait SessionIdAware {
  def journeySessionId(implicit request: JourneyRequest[_]) =
    request.authenticatedRequest.session.data("sessionId")

  def authenticatedSessionId(implicit request: AuthenticatedRequest[AnyContent]) =
    request.session.data("sessionId")
}
