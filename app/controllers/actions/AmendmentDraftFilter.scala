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

import models.requests.JourneyRequest
import play.api.mvc.Results.Redirect
import play.api.mvc.{Action, ActionRefiner, AnyContent, Call, Result}

import scala.concurrent.{ExecutionContext, Future}

trait AmendmentDraftFilter {

  val nextPage: JourneyRequest[_] => Call

  val displayPage: Action[AnyContent]
  val submitForm: Action[AnyContent]

  type RefineResult[A] = Future[Either[Result, JourneyRequest[A]]]

  def nextPageIfAmendmentDraft(implicit exc: ExecutionContext): ActionRefiner[JourneyRequest, JourneyRequest] =
    new ActionRefiner[JourneyRequest, JourneyRequest] {
      override protected def executionContext: ExecutionContext = exc

      override protected def refine[A](request: JourneyRequest[A]): RefineResult[A] =
        Future.successful {
          if (request.cacheModel.isAmendmentDraft) Left(Redirect(nextPage(request))) else Right(request)
        }
    }
}
