/*
 * Copyright 2018 HM Revenue & Customs
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

import com.google.inject.{ImplementedBy, Inject}
import play.api.mvc.{ActionBuilder, ActionFunction, Request, Result}
import play.api.mvc.Results._
import uk.gov.hmrc.auth.core.{NoActiveSession, _}
import config.FrontendAppConfig
import controllers.routes
import models.SignedInUser
import models.requests.AuthenticatedRequest
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.HeaderCarrierConverter
import uk.gov.hmrc.auth.core._
import uk.gov.hmrc.auth.core.retrieve.~
import uk.gov.hmrc.auth.core.retrieve.Retrievals._

import scala.concurrent.{ExecutionContext, Future}

class AuthActionImpl @Inject()(override val authConnector: AuthConnector, config: FrontendAppConfig)
                              (implicit ec: ExecutionContext) extends AuthAction with AuthorisedFunctions {

  override def invokeBlock[A](request: Request[A], block: (AuthenticatedRequest[A]) => Future[Result]): Future[Result] = {
    implicit val hc: HeaderCarrier = HeaderCarrierConverter.fromHeadersAndSession(request.headers, Some(request.session))

    authorised().retrieve(credentials and name and email and externalId and internalId  and affinityGroup and allEnrolments) {
      case credentials ~ name ~ email ~ externalId ~ internalId ~ affinityGroup ~ allEnrolments =>
        val eori = allEnrolments.getEnrolment("HMRC-CUS-ORG").flatMap(_.getIdentifier("EORINumber"))
        // TODO add correct eori validation not only isEmpty
        if (eori.isEmpty) {
          throw InsufficientEnrolments()
        }
        if (externalId.isEmpty) {
          throw NoExternalId()
        }
        val cdsLoggedInUser =
          SignedInUser(credentials, name, email, eori.get.value, externalId.get, internalId, affinityGroup, allEnrolments)

        block(AuthenticatedRequest(request, cdsLoggedInUser))
    } recover {
      case _: NoActiveSession =>
        Redirect(config.loginUrl, Map("continue" -> Seq(config.loginContinueUrl)))
      case _: InsufficientEnrolments =>
        //TODO put in a redirect here to the Get an EORI page!
        Redirect(routes.UnauthorisedController.onPageLoad)
      case _ =>
        Redirect(routes.UnauthorisedController.onPageLoad)
    }
  }
}

@ImplementedBy(classOf[AuthActionImpl])
trait AuthAction extends ActionBuilder[AuthenticatedRequest] with ActionFunction[Request, AuthenticatedRequest]

case class NoExternalId() extends NoActiveSession("No externalId was found")