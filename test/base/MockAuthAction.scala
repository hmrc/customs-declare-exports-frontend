/*
 * Copyright 2024 HM Revenue & Customs
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

package base

import base.ExportsTestData.{eori, _}
import config.AppConfig
import controllers.actions.{AuthActionImpl, EoriAllowList}
import models.requests.SessionHelper.declarationUuid
import models.requests.{JourneyRequest, VerifiedEmailRequest}
import models.{ExportsDeclaration, SignedInUser}
import org.mockito.ArgumentMatchers
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import play.api.mvc.{AnyContentAsEmpty, Request}
import play.api.test.FakeRequest
import tools.Stubs
import uk.gov.hmrc.auth.core._
import uk.gov.hmrc.auth.core.retrieve.v2.Retrievals._
import uk.gov.hmrc.auth.core.retrieve.~
import utils.FakeRequestCSRFSupport._

import scala.concurrent.Future

trait MockAuthAction extends MockitoSugar with Stubs with MetricsMocks with Injector with RequestBuilder {

  val mockAuthConnector: AuthConnector = mock[AuthConnector]
  val appConfig = instanceOf[AppConfig]

  val mockAuthAction =
    new AuthActionImpl(mockAuthConnector, new EoriAllowList(Seq.empty), stubMessagesControllerComponents(), metricsMock, appConfig)

  val exampleUser = newUser(eori, "external1")

  def unauthorizedUser(exceptionThrown: AuthorisationException): Unit =
    when(
      mockAuthConnector.authorise(
        any(),
        ArgumentMatchers.eq(
          credentials and email and externalId and internalId and affinityGroup and allEnrolments
            and agentCode and confidenceLevel and nino and saUtr and dateOfBirth and agentInformation and groupIdentifier and
            credentialRole and mdtpInformation and itmpName and itmpDateOfBirth and itmpAddress and credentialStrength and loginTimes
        )
      )(any(), any())
    ).thenReturn(Future.failed(exceptionThrown))

  // scalastyle:off
  def authorizedUser(user: SignedInUser = exampleUser): Unit =
    when(
      mockAuthConnector.authorise(
        any(),
        ArgumentMatchers.eq(
          credentials and email and externalId and internalId and affinityGroup and allEnrolments
            and agentCode and confidenceLevel and nino and saUtr and dateOfBirth and agentInformation and groupIdentifier and
            credentialRole and mdtpInformation and itmpName and itmpDateOfBirth and itmpAddress and credentialStrength and loginTimes
        )
      )(any(), any())
    ).thenReturn(
      Future.successful(
        new ~(
          new ~(
            new ~(
              new ~(
                new ~(
                  new ~(
                    new ~(
                      new ~(
                        new ~(
                          new ~(
                            new ~(
                              new ~(
                                new ~(
                                  new ~(
                                    new ~(
                                      new ~(
                                        new ~(
                                          new ~(new ~(user.identityData.credentials, user.identityData.email), user.identityData.externalId),
                                          user.identityData.internalId
                                        ),
                                        user.identityData.affinityGroup
                                      ),
                                      user.enrolments
                                    ),
                                    user.identityData.agentCode
                                  ),
                                  user.identityData.confidenceLevel.get
                                ),
                                user.identityData.nino
                              ),
                              user.identityData.saUtr
                            ),
                            user.identityData.dateOfBirth
                          ),
                          user.identityData.agentInformation.get
                        ),
                        nrsGroupIdentifierValue
                      ),
                      nrsCredentialRole
                    ),
                    Some(nrsMdtpInformation)
                  ),
                  Some(nrsItmpName)
                ),
                nrsDateOfBirth
              ),
              Some(nrsItmpAddress)
            ),
            nrsCredentialStrength
          ),
          nrsLoginTimes
        )
      )
    )

  def userWithoutEori(user: SignedInUser = newUser(eori, "external1")): Unit =
    when(
      mockAuthConnector.authorise(
        any(),
        ArgumentMatchers.eq(
          credentials and email and externalId and internalId and affinityGroup and allEnrolments
            and agentCode and confidenceLevel and nino and saUtr and dateOfBirth and agentInformation and groupIdentifier and
            credentialRole and mdtpInformation and itmpName and itmpDateOfBirth and itmpAddress and credentialStrength and loginTimes
        )
      )(any(), any())
    ).thenReturn(
      Future.successful(
        new ~(
          new ~(
            new ~(
              new ~(
                new ~(
                  new ~(
                    new ~(
                      new ~(
                        new ~(
                          new ~(
                            new ~(
                              new ~(
                                new ~(
                                  new ~(
                                    new ~(
                                      new ~(
                                        new ~(
                                          new ~(new ~(user.identityData.credentials, user.identityData.email), user.identityData.externalId),
                                          user.identityData.internalId
                                        ),
                                        user.identityData.affinityGroup
                                      ),
                                      Enrolments(Set())
                                    ),
                                    user.identityData.agentCode
                                  ),
                                  user.identityData.confidenceLevel.get
                                ),
                                user.identityData.nino
                              ),
                              user.identityData.saUtr
                            ),
                            user.identityData.dateOfBirth
                          ),
                          user.identityData.agentInformation.get
                        ),
                        nrsGroupIdentifierValue
                      ),
                      nrsCredentialRole
                    ),
                    Some(nrsMdtpInformation)
                  ),
                  Some(nrsItmpName)
                ),
                nrsDateOfBirth
              ),
              Some(nrsItmpAddress)
            ),
            nrsCredentialStrength
          ),
          nrsLoginTimes
        )
      )
    )

  def userWithoutExternalId(user: SignedInUser = newUser(eori, "external1")): Unit =
    when(
      mockAuthConnector.authorise(
        any(),
        ArgumentMatchers.eq(
          credentials and email and externalId and internalId and affinityGroup and allEnrolments
            and agentCode and confidenceLevel and nino and saUtr and dateOfBirth and agentInformation and groupIdentifier and
            credentialRole and mdtpInformation and itmpName and itmpDateOfBirth and itmpAddress and credentialStrength and loginTimes
        )
      )(any(), any())
    ).thenReturn(
      Future.successful(
        new ~(
          new ~(
            new ~(
              new ~(
                new ~(
                  new ~(
                    new ~(
                      new ~(
                        new ~(
                          new ~(
                            new ~(
                              new ~(
                                new ~(
                                  new ~(
                                    new ~(
                                      new ~(
                                        new ~(
                                          new ~(new ~(user.identityData.credentials, user.identityData.email), None),
                                          user.identityData.internalId
                                        ),
                                        user.identityData.affinityGroup
                                      ),
                                      user.enrolments
                                    ),
                                    user.identityData.agentCode
                                  ),
                                  user.identityData.confidenceLevel.get
                                ),
                                user.identityData.nino
                              ),
                              user.identityData.saUtr
                            ),
                            user.identityData.dateOfBirth
                          ),
                          user.identityData.agentInformation.get
                        ),
                        nrsGroupIdentifierValue
                      ),
                      nrsCredentialRole
                    ),
                    Some(nrsMdtpInformation)
                  ),
                  Some(nrsItmpName)
                ),
                nrsDateOfBirth
              ),
              Some(nrsItmpAddress)
            ),
            nrsCredentialStrength
          ),
          nrsLoginTimes
        )
      )
    )
  // scalastyle:on

  def getAuthenticatedRequest(declarationId: String = "declarationUuid"): VerifiedEmailRequest[AnyContentAsEmpty.type] =
    buildVerifiedEmailRequest(FakeRequest("GET", "").withSession(declarationUuid -> declarationId).withCSRFToken, exampleUser)

  def getAuthenticatedRequest(eori: String, declarationId: String): VerifiedEmailRequest[AnyContentAsEmpty.type] = {
    val user = newUser(eori, "external1")
    buildVerifiedEmailRequest(FakeRequest("GET", "").withSession(declarationUuid -> declarationId).withCSRFToken, user)
  }

  def getAuthenticatedRequest(declarationId: String, session: (String, String)*): VerifiedEmailRequest[AnyContentAsEmpty.type] =
    buildVerifiedEmailRequest(
      FakeRequest("GET", "").withSession(declarationUuid -> declarationId).withSession(session: _*).withCSRFToken,
      exampleUser
    )

  def getRequest: Request[AnyContentAsEmpty.type] =
    FakeRequest("GET", "").withSession(declarationUuid -> "declarationUuid").withCSRFToken

  def getJourneyRequest(declaration: ExportsDeclaration = aDeclaration()): JourneyRequest[AnyContentAsEmpty.type] =
    new JourneyRequest[AnyContentAsEmpty.type](getAuthenticatedRequest(), declaration)

  def getJourneyRequest(eori: String, declaration: ExportsDeclaration): JourneyRequest[AnyContentAsEmpty.type] =
    new JourneyRequest[AnyContentAsEmpty.type](getAuthenticatedRequest(eori, "declarationUuid"), declaration)

  def getJourneyRequest(declaration: ExportsDeclaration, session: (String, String)*): JourneyRequest[AnyContentAsEmpty.type] =
    new JourneyRequest[AnyContentAsEmpty.type](getAuthenticatedRequest(declaration.id, session: _*), declaration)

  def getRequestWithSession(session: (String, String)*): Request[AnyContentAsEmpty.type] =
    FakeRequest("GET", "")
      .withSession(declarationUuid -> "declarationUuid")
      .withSession(session: _*)
      .withCSRFToken

  def getRequest(data: (String, String)*): Request[AnyContentAsEmpty.type] =
    FakeRequest("GET", "")
      .withFlash(data: _*)
      .withSession(declarationUuid -> "declarationUuid")
      .withCSRFToken

  def getRequest(declarationId: Option[String]): Request[AnyContentAsEmpty.type] =
    declarationId match {
      case Some(declId) => FakeRequest("GET", "").withSession(declarationUuid -> declId).withCSRFToken
      case _            => FakeRequest("GET", "").withCSRFToken
    }
}
