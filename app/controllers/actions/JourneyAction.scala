/*
 * Copyright 2021 HM Revenue & Customs
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

import scala.concurrent.{ExecutionContext, Future}

import com.google.inject.Inject
import controllers.routes.RootController
import models.DeclarationType.DeclarationType
import models.requests.{AuthenticatedRequest, JourneyRequest}
import play.api.Logging
import play.api.mvc.{ActionRefiner, Result, Results}
import services.cache.ExportsCacheService
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.http.HeaderCarrierConverter

class JourneyAction @Inject()(cacheService: ExportsCacheService)(implicit val exc: ExecutionContext)
    extends ActionRefiner[AuthenticatedRequest, JourneyRequest] with Logging {

  override protected def executionContext: ExecutionContext = exc

  private def refiner[A](request: AuthenticatedRequest[A], types: Seq[DeclarationType]): Future[Either[Result, JourneyRequest[A]]] = {
    implicit val hc: HeaderCarrier = HeaderCarrierConverter.fromRequestAndSession(request, request.session)
    request.declarationId match {
      case Some(id) =>
        cacheService.get(id).map {
          case Some(declaration) if types.isEmpty || types.contains(declaration.`type`) =>
            Right(new JourneyRequest(request, declaration))

          case _ =>
            logger.warn(s"Could not retrieve from cache, for eori ${request.user.eori}, the declaration with id $id")
            Left(Results.Redirect(RootController.displayPage()))
        }
      case None =>
        logger.warn(s"Could not obtain the declaration's id for eori ${request.user.eori}")
        Future.successful(Left(Results.Redirect(RootController.displayPage())))
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
