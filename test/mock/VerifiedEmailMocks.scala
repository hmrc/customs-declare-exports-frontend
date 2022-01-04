/*
 * Copyright 2022 HM Revenue & Customs
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

package mock

import controllers.actions.VerifiedEmailAction
import models.requests.{AuthenticatedRequest, VerifiedEmailRequest}
import play.api.mvc.Result

import scala.concurrent.{ExecutionContext, Future}

trait VerifiedEmailMocks {
  val mockVerifiedEmailAction =
    new VerifiedEmailAction() {
      implicit val executionContext: ExecutionContext = ExecutionContext.global

      override protected def refine[A](request: AuthenticatedRequest[A]): Future[Either[Result, VerifiedEmailRequest[A]]] =
        Future.successful(Right(VerifiedEmailRequest(request, "example@example.com")))
    }
}
