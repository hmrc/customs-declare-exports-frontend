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

import com.google.inject.Inject
import controllers.declaration.routes.SummaryController
import models.requests.JourneyRequest
import play.api.Logging
import play.api.mvc.{ActionRefiner, Result, Results}

import scala.concurrent.{ExecutionContext, Future}

class AmendmentDraftFilterAction @Inject() ()(implicit val exc: ExecutionContext) extends ActionRefiner[JourneyRequest, JourneyRequest] with Logging {

  type RefineResult[A] = Future[Either[Result, JourneyRequest[A]]]

  override protected def executionContext: ExecutionContext = exc

  override def refine[A](request: JourneyRequest[A]): RefineResult[A] =
    Future.successful(Right(request))

  def apply(): ActionRefiner[JourneyRequest, JourneyRequest] =
    new ActionRefiner[JourneyRequest, JourneyRequest] {
      override protected def executionContext: ExecutionContext = exc

      override protected def refine[A](request: JourneyRequest[A]): RefineResult[A] = refineOnAmendmentDraft(request)
    }

  private def refineOnAmendmentDraft[A](request: JourneyRequest[A]): RefineResult[A] =
    Future.successful {
      if (!request.cacheModel.isAmendmentDraft) Right(request)
      else {
        logger.warn(s"Redirection to summary for eori ${request.user.eori}, as amendment draft from ${request.headers.get("Raw-Request-URI")}")
        Left(Results.Redirect(SummaryController.displayPage))
      }
    }
}
