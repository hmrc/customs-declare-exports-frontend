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

package base

import base.ExportsTestData._
import controllers.actions.AuthActionImpl
import models.SignedInUser
import models.requests.AuthenticatedRequest
import org.mockito.ArgumentMatchers
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatest.mockito.MockitoSugar
import play.api.mvc.{AnyContentAsEmpty, Request}
import play.api.test.FakeRequest
import uk.gov.hmrc.auth.core._
import uk.gov.hmrc.auth.core.retrieve.v2.Retrievals._
import uk.gov.hmrc.auth.core.retrieve.~
import unit.tools.Stubs

import scala.concurrent.Future

trait MockAuthAction extends MockitoSugar with Stubs {

  val mockAuthConnector: AuthConnector = mock[AuthConnector]

  val mockAuthAction = new AuthActionImpl(mockAuthConnector, stubMessagesControllerComponents())

  val exampleUser = newUser("12345", "external1")

  def authorizedUser(user: SignedInUser = exampleUser): Unit =
    when(
      mockAuthConnector.authorise(
        any(),
        ArgumentMatchers.eq(
          credentials and name and email and externalId and internalId and affinityGroup and allEnrolments
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
                                          new ~(
                                            new ~(
                                              new ~(user.identityData.credentials, user.identityData.name),
                                              user.identityData.email
                                            ),
                                            user.identityData.externalId
                                          ),
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

  def userWithoutEori(user: SignedInUser = newUser("12345", "external1")): Unit =
    when(
      mockAuthConnector.authorise(
        any(),
        ArgumentMatchers.eq(
          credentials and name and email and externalId and internalId and affinityGroup and allEnrolments
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
                                          new ~(
                                            new ~(
                                              new ~(user.identityData.credentials, user.identityData.name),
                                              user.identityData.email
                                            ),
                                            user.identityData.externalId
                                          ),
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

  def userWithoutExternalId(user: SignedInUser = newUser("12345", "external1")): Unit =
    when(
      mockAuthConnector.authorise(
        any(),
        ArgumentMatchers.eq(
          credentials and name and email and externalId and internalId and affinityGroup and allEnrolments
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
                                          new ~(
                                            new ~(
                                              new ~(user.identityData.credentials, user.identityData.name),
                                              user.identityData.email
                                            ),
                                            None
                                          ),
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

  def getAuthenticatedRequest(sessionId: String = "sessionId"): AuthenticatedRequest[AnyContentAsEmpty.type] = {
    import utils.FakeRequestCSRFSupport._
    AuthenticatedRequest(FakeRequest("GET", "").withSession(("sessionId", sessionId)).withCSRFToken, exampleUser)
  }

  def getRequest(): Request[AnyContentAsEmpty.type] = {
    import utils.FakeRequestCSRFSupport._
    FakeRequest("GET", "").withSession(("sessionId", "sessionId")).withCSRFToken
  }

  def getRequest(uri: String, sessionId: String): Request[AnyContentAsEmpty.type] = {
    import utils.FakeRequestCSRFSupport._
    FakeRequest("GET", uri).withSession(("sessionId", sessionId)).withCSRFToken
  }

}
