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

package base

import models.SignedInUser
import org.mockito.ArgumentMatchers
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatest.mockito.MockitoSugar
import uk.gov.hmrc.auth.core.{AffinityGroup, AuthConnector, Enrolment, Enrolments}
import uk.gov.hmrc.auth.core.retrieve.Retrievals._
import uk.gov.hmrc.auth.core.retrieve.{Credentials, Name, ~}

import scala.concurrent.Future

trait MockAuthAction extends MockitoSugar {
  lazy val mockAuthConnector: AuthConnector = mock[AuthConnector]

  def authorizedUser(user: SignedInUser = newUser("12345","external1")): Unit =
    when(
      mockAuthConnector.authorise(
        any(),
        ArgumentMatchers.eq(credentials and name and email and externalId and internalId and affinityGroup and allEnrolments))
      (any(), any())
    ).thenReturn(
      Future.successful(new ~(new ~(new ~(new ~(new ~(new ~(user.credentials, user.name), user.email),
        Some(user.externalId)), user.internalId), user.affinityGroup), user.enrolments))
    )

  def userWithoutEori(user: SignedInUser = newUser("12345","external1")): Unit =
    when(
      mockAuthConnector.authorise(
        any(),
        ArgumentMatchers.eq(credentials and name and email and externalId and internalId and affinityGroup and allEnrolments))
      (any(), any())
    ).thenReturn(
      Future.successful(new ~(new ~(new ~(new ~(new ~(new ~(user.credentials, user.name), user.email),
        Some(user.externalId)), user.internalId), user.affinityGroup), Enrolments(Set())))
    )

  def userWithoutExternalId(user: SignedInUser = newUser("12345","external1")): Unit =
    when(
      mockAuthConnector.authorise(
        any(),
        ArgumentMatchers.eq(credentials and name and email and externalId and internalId and affinityGroup and allEnrolments))
      (any(), any())
    ).thenReturn(
      Future.successful(new ~(new ~(new ~(new ~(new ~(new ~(user.credentials, user.name), user.email),
        None), user.internalId), user.affinityGroup), user.enrolments))
    )

  def newUser(eori: String, externalId: String): SignedInUser = SignedInUser(
    Credentials("2345235235","GovernmentGateway"),
    Name(Some("Aldo"),Some("Rain")),
    Some("amina@hmrc.co.uk"),
    eori,
    externalId,
    Some("Int-ba17b467-90f3-42b6-9570-73be7b78eb2b"),
    Some(AffinityGroup.Individual),
    Enrolments(Set(
      Enrolment("HMRC-CUS-ORG").withIdentifier("EORINumber", eori)
    ))
  )
}
