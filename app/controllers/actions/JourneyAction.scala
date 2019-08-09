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

package controllers.actions

import com.google.inject.Inject
import controllers.declaration.SessionIdAware
import models.requests.{AuthenticatedRequest, JourneyRequest}
import play.api.Logger
import play.api.mvc.Results.Conflict
import play.api.mvc.{ActionRefiner, Result}
import services.cache.ExportsCacheService
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.HeaderCarrierConverter

import scala.concurrent.{ExecutionContext, Future}

class JourneyAction @Inject()(cacheService: ExportsCacheService)(
  implicit override val executionContext: ExecutionContext
) extends ActionRefiner[AuthenticatedRequest, JourneyRequest] with SessionIdAware {

  private val logger = Logger(this.getClass())

  override def refine[A](request: AuthenticatedRequest[A]): Future[Either[Result, JourneyRequest[A]]] = {
    implicit val hc: HeaderCarrier =
      HeaderCarrierConverter.fromHeadersAndSession(request.headers, Some(request.session))

    cacheService.get(request.sessionId).map {
      case Some(cacheModel) => Right(JourneyRequest(request, cacheModel))
      case _                =>
        // $COVERAGE-OFF$Trivial
        logger.error(s"Could not obtain journey type for ${request.sessionId}")
        // $COVERAGE-ON
        Left(Conflict("Could not obtain information about journey type"))
    }
  }
}
