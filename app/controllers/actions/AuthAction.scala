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

import com.google.inject.{ImplementedBy, Inject}
import models.requests.AuthenticatedRequest
import models.{IdentityData, SignedInUser}
import play.api.Logger
import play.api.mvc._
import uk.gov.hmrc.auth.core.retrieve.v2.Retrievals.{agentCode, _}
import uk.gov.hmrc.auth.core.retrieve.~
import uk.gov.hmrc.auth.core.{NoActiveSession, _}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.HeaderCarrierConverter

import scala.concurrent.{ExecutionContext, Future}

class AuthActionImpl @Inject()(override val authConnector: AuthConnector, mcc: MessagesControllerComponents) extends AuthAction with AuthorisedFunctions {

  implicit override val executionContext: ExecutionContext = mcc.executionContext
  override val parser: BodyParser[AnyContent] = mcc.parsers.defaultBodyParser
  private val logger = Logger(classOf[AuthActionImpl])

  override def invokeBlock[A](
    request: Request[A],
    block: AuthenticatedRequest[A] => Future[Result]
  ): Future[Result] = {
    implicit val hc: HeaderCarrier =
      HeaderCarrierConverter.fromHeadersAndSession(request.headers, Some(request.session))

    authorised(Enrolment("HMRC-CUS-ORG"))
      .retrieve(
        credentials and name and email and externalId and internalId and affinityGroup and allEnrolments and
          agentCode and confidenceLevel and nino and saUtr and dateOfBirth and agentInformation and groupIdentifier and
          credentialRole and mdtpInformation and itmpName and itmpDateOfBirth and itmpAddress and credentialStrength and loginTimes
      ) {
        case credentials ~ name ~ email ~ externalId ~ internalId ~ affinityGroup ~ allEnrolments ~ agentCode ~
              confidenceLevel ~ authNino ~ saUtr ~ dateOfBirth ~ agentInformation ~ groupIdentifier ~
              credentialRole ~ mdtpInformation ~ itmpName ~ itmpDateOfBirth ~ itmpAddress ~ credentialStrength ~ loginTimes =>
          val eori = getEoriFromEnrolments(allEnrolments)

          validateEnrollments(eori, externalId)

          val identityData = IdentityData(
            internalId,
            externalId,
            agentCode,
            credentials,
            Some(confidenceLevel),
            authNino,
            saUtr,
            name,
            dateOfBirth,
            email,
            Some(agentInformation),
            groupIdentifier,
            credentialRole.map(res => res.toJson.toString()),
            mdtpInformation,
            itmpName,
            itmpDateOfBirth,
            itmpAddress,
            affinityGroup,
            credentialStrength,
            Some(loginTimes)
          )

          val cdsLoggedInUser = SignedInUser(eori.get.value, allEnrolments, identityData)

          block(AuthenticatedRequest(request, cdsLoggedInUser))
      }
  }

  private def getEoriFromEnrolments(enrolments: Enrolments): Option[EnrolmentIdentifier] =
    enrolments.getEnrolment("HMRC-CUS-ORG").flatMap(_.getIdentifier("EORINumber"))

  private def validateEnrollments(eori: Option[EnrolmentIdentifier], externalId: Option[String]): Unit = {
    if (eori.isEmpty) {
      logger.error("User doesn't have eori")
      throw InsufficientEnrolments()
    }

    if (externalId.isEmpty) {
      logger.error("User doesn't have external Id")
      throw NoExternalId()
    }
  }
}

@ImplementedBy(classOf[AuthActionImpl])
trait AuthAction extends ActionBuilder[AuthenticatedRequest, AnyContent] with ActionFunction[Request, AuthenticatedRequest]

case class NoExternalId() extends NoActiveSession("No externalId was found")
