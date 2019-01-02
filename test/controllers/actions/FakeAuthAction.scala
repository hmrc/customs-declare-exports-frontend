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

import models.requests.AuthenticatedRequest
import models.{IdentityData, SignedInUser}
import play.api.mvc.{Request, Result}
import uk.gov.hmrc.auth.core._
import uk.gov.hmrc.auth.core.retrieve.Credentials

import scala.concurrent.Future

object FakeAuthAction extends AuthAction {

  val defaultUser = newUser("0771123680108", "Ext-1234-5678")

  def newUser(eori: String, externalId: String): SignedInUser =
    SignedInUser(
      eori,
      Enrolments(
        Set(
          Enrolment("IR-SA", List(EnrolmentIdentifier("UTR", "111111111")), "Activated", None),
          Enrolment("IR-CT", List(EnrolmentIdentifier("UTR", "222222222")), "Activated", None),
          Enrolment("HMRC-CUS-ORG", List(EnrolmentIdentifier("EORINumber", eori)), "Activated", None)
        )
      ),
      IdentityData(
        Some("Int-1234-5678"),
        Some(externalId),
        agentCode = None,
        credentials = Some(Credentials("2345235235", "GovernmentGateway")),
        confidenceLevel = None,
        nino = None,
        saUtr = None,
        name = None,
        dateOfBirth = None,
        email = Some("amina@hmrc.co.uk")
      )
    )

  override def invokeBlock[A](request: Request[A], block: (AuthenticatedRequest[A]) => Future[Result]): Future[Result] =
    block(AuthenticatedRequest(request, defaultUser))
}

class TestAuthAction extends AuthAction {
  override def invokeBlock[A](request: Request[A], block: (AuthenticatedRequest[A]) => Future[Result]): Future[Result] =
    block(AuthenticatedRequest(request, FakeAuthAction.defaultUser))
}
