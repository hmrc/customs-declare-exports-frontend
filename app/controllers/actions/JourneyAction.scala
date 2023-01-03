/*
 * Copyright 2023 HM Revenue & Customs
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
import forms.declaration.additionaldeclarationtype.AdditionalDeclarationType.AdditionalDeclarationType
import models.DeclarationType.DeclarationType
import models.ExportsDeclaration
import models.requests.{AuthenticatedRequest, JourneyRequest}
import play.api.Logging
import play.api.mvc.{ActionRefiner, Result, Results}
import services.cache.ExportsCacheService
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.http.HeaderCarrierConverter

class JourneyAction @Inject() (cacheService: ExportsCacheService)(implicit val exc: ExecutionContext)
    extends ActionRefiner[AuthenticatedRequest, JourneyRequest] with Logging {

  type RefineResult[A] = Future[Either[Result, JourneyRequest[A]]]

  override protected def executionContext: ExecutionContext = exc

  private def refineOnDeclarationTypes[A](request: AuthenticatedRequest[A], types: Seq[DeclarationType]): RefineResult[A] =
    request.declarationId match {
      case Some(id) => verifyDeclaration(id, request, (declaration: ExportsDeclaration) => types.isEmpty || types.contains(declaration.`type`))
      case None     => Future.successful(redirectToRoot(s"Could not obtain the declaration's id for eori ${request.user.eori}"))
    }

  private def refineOnAdditionalTypes[A](request: AuthenticatedRequest[A], additionalTypes: Seq[AdditionalDeclarationType]): RefineResult[A] =
    request.declarationId match {
      case Some(id) => verifyDeclaration(id, request, additionalTypes.isEmpty || _.additionalDeclarationType.exists(additionalTypes.contains))
      case None     => Future.successful(redirectToRoot(s"Could not obtain the declaration's id for eori ${request.user.eori}"))
    }

  override def refine[A](request: AuthenticatedRequest[A]): RefineResult[A] =
    refineOnDeclarationTypes(request, Seq.empty[DeclarationType])

  def apply(type1: DeclarationType, others: DeclarationType*): ActionRefiner[AuthenticatedRequest, JourneyRequest] =
    apply(others.toSeq.+:(type1))

  def apply(types: Seq[DeclarationType]): ActionRefiner[AuthenticatedRequest, JourneyRequest] =
    new ActionRefiner[AuthenticatedRequest, JourneyRequest] {
      override protected def executionContext: ExecutionContext = exc

      override protected def refine[A](request: AuthenticatedRequest[A]): RefineResult[A] = refineOnDeclarationTypes(request, types)
    }

  def onAdditionalTypes(additionalTypes: Seq[AdditionalDeclarationType]): ActionRefiner[AuthenticatedRequest, JourneyRequest] =
    new ActionRefiner[AuthenticatedRequest, JourneyRequest] {
      override protected def executionContext: ExecutionContext = exc

      override protected def refine[A](request: AuthenticatedRequest[A]): RefineResult[A] =
        refineOnAdditionalTypes(request, additionalTypes)
    }

  private def redirectToRoot[A](message: String): Either[Result, JourneyRequest[A]] = {
    logger.warn(message)
    Left(Results.Redirect(RootController.displayPage))
  }

  private def verifyDeclaration[A](id: String, request: AuthenticatedRequest[A], onCondition: ExportsDeclaration => Boolean): RefineResult[A] = {
    implicit val hc: HeaderCarrier = HeaderCarrierConverter.fromRequestAndSession(request, request.session)

    cacheService.get(id).map {
      case Some(declaration) =>
        if (onCondition(declaration)) Right(new JourneyRequest(request, declaration))
        else {
          val types = s"${declaration.`type`}${declaration.additionalDeclarationType.fold("")(adt => s",$adt")}"
          redirectToRoot(s"Redirection to start for eori ${request.user.eori}, as types($types) of declaration($id) are not accepted")
        }

      case _ => redirectToRoot(s"Could not retrieve from cache, for eori ${request.user.eori}, the declaration with id $id")
    }
  }
}
