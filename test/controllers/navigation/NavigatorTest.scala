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

package controllers.navigation

import java.time.{LocalDate, ZoneOffset}
import java.util.concurrent.TimeUnit

import config.AppConfig
import controllers.util._
import models.SignedInUser
import models.requests.{AuthenticatedRequest, ExportsSessionKeys, JourneyRequest}
import models.responses.FlashKeys
import org.mockito.ArgumentMatchers.any
import org.mockito.BDDMockito._
import org.mockito.Mockito.{verify, verifyZeroInteractions}
import org.scalatest.{Matchers, WordSpec}
import org.scalatestplus.mockito.MockitoSugar
import play.api.mvc.{AnyContent, Call, Result}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.audit.AuditService
import services.cache.ExportsDeclarationBuilder
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.Future
import scala.concurrent.duration.FiniteDuration

class NavigatorTest extends WordSpec with Matchers with MockitoSugar with ExportsDeclarationBuilder {

  private val call = Call("GET", "url")
  private val config = mock[AppConfig]
  private val auditService = mock[AuditService]
  private val hc: HeaderCarrier = mock[HeaderCarrier]
  private val navigator = new Navigator(config, auditService)

  "Continue To" should {
    val updatedDate = LocalDate.of(2020, 1, 1)
    val expiryDate = LocalDate.of(2020, 1, 1).plusDays(10)

    val declaration = aDeclaration(withUpdateDate(updatedDate))
    def authenticatedRequest(action: Option[FormAction]) = new AuthenticatedRequest[AnyContent](
      FakeRequest("GET", "uri")
        .withFormUrlEncodedBody(action.getOrElse("other-field").toString -> "")
        .withSession(ExportsSessionKeys.declarationId -> "declarationId"),
      mock[SignedInUser]
    )
    def request(action: Option[FormAction]): JourneyRequest[AnyContent] =
      new JourneyRequest[AnyContent](authenticatedRequest(action), declaration)

    "Go to Save as Draft" in {
      given(config.draftTimeToLive).willReturn(FiniteDuration(10, TimeUnit.DAYS))

      val result = navigator.continueTo(call)(request(Some(SaveAndReturn)), hc)

      status(result) shouldBe SEE_OTHER
      redirectLocation(result) shouldBe Some(
        controllers.declaration.routes.ConfirmationController.displayDraftConfirmation().url
      )
      flash(result).get(FlashKeys.expiryDate) shouldBe Some(
        expiryDate.atStartOfDay(ZoneOffset.UTC).toInstant.toEpochMilli.toString
      )
      session(result).get(ExportsSessionKeys.declarationId) shouldBe None

      verify(auditService).auditAllPagesUserInput(any())(any())
    }

    "Go to URL provided" when {
      "Save And Continue" in {
        val result = navigator.continueTo(call)(request(Some(SaveAndContinue)), hc)

        status(result) shouldBe SEE_OTHER
        redirectLocation(result) shouldBe Some("url")
        verifyZeroInteractions(auditService)
      }

      "Add" in {
        val result = navigator.continueTo(call)(request(Some(Add)), hc)

        status(result) shouldBe SEE_OTHER
        redirectLocation(result) shouldBe Some("url")
        verifyZeroInteractions(auditService)
      }

      "Remove" in {
        val result = navigator.continueTo(call)(request(Some(Remove(Seq.empty))), hc)

        status(result) shouldBe SEE_OTHER
        redirectLocation(result) shouldBe Some("url")
        verifyZeroInteractions(auditService)
      }

      "Unknown Action" in {
        val result = navigator.continueTo(call)(request(Some(Unknown)), hc)

        status(result) shouldBe SEE_OTHER
        redirectLocation(result) shouldBe Some("url")
        verifyZeroInteractions(auditService)
      }
    }
  }

  private implicit def result2future: Result => Future[Result] = Future.successful

}
