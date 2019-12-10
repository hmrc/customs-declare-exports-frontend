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
import models.DeclarationType.DeclarationType
import models.requests.{AuthenticatedRequest, JourneyRequest}
import play.api.Logger
import play.api.mvc.{ActionRefiner, Result, Results}
import services.cache.ExportsCacheService
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.HeaderCarrierConverter

import scala.concurrent.{ExecutionContext, Future}

class JourneyAction @Inject()(cacheService: ExportsCacheService)(implicit val exc: ExecutionContext)
    extends ActionRefiner[AuthenticatedRequest, JourneyRequest] {

  private val logger = Logger(this.getClass)

  override protected def executionContext: ExecutionContext = exc

  private def refiner[A](request: AuthenticatedRequest[A], types: Seq[DeclarationType]): Future[Either[Result, JourneyRequest[A]]] = {
    implicit val hc: HeaderCarrier = HeaderCarrierConverter.fromHeadersAndSession(request.headers, Some(request.session))
    request.declarationId match {
      case Some(id) =>
        cacheService.get(id).map {
          case Some(declaration) if types.isEmpty || types.contains(declaration.`type`) =>
            Right(new JourneyRequest(request, declaration))
          case _ =>
            Left(Results.Redirect(controllers.routes.StartController.displayStartPage()))
        }
      case None =>
        logger.warn(s"Could not obtain journey type for declaration ${request.declarationId}")
        Future.successful(Left(Results.Redirect(controllers.routes.StartController.displayStartPage())))
    }
  }
  override def refine[A](request: AuthenticatedRequest[A]): Future[Either[Result, JourneyRequest[A]]] =
    refiner(request, Seq.empty[DeclarationType])

  def apply(type1: DeclarationType, others: DeclarationType*): ActionRefiner[AuthenticatedRequest, JourneyRequest] = apply(others.toSeq.+:(type1))

  def apply(types: Seq[DeclarationType]): ActionRefiner[AuthenticatedRequest, JourneyRequest] =
    new ActionRefiner[AuthenticatedRequest, JourneyRequest] {
      override protected def refine[A](request: AuthenticatedRequest[A]): Future[Either[Result, JourneyRequest[A]]] = refiner(request, types)
      override protected def executionContext: ExecutionContext = exc
    }
}
