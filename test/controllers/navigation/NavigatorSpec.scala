/*
 * Copyright 2021 HM Revenue & Customs
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
import scala.concurrent.Future
import scala.concurrent.duration.FiniteDuration
import base.{MockExportCacheService, RequestBuilder, UnitWithMocksSpec}
import config.AppConfig
import controllers.helpers._
import forms.declaration.carrier.CarrierDetails
import models.requests.{ExportsSessionKeys, JourneyRequest}
import models.responses.FlashKeys
import models.{ExportsDeclaration, Mode, SignedInUser}
import org.mockito.ArgumentMatchers.any
import org.mockito.BDDMockito._
import org.mockito.Mockito.{verify, verifyNoInteractions}
import org.mockito.{ArgumentMatchers, Mockito}
import org.scalatest.BeforeAndAfterEach
import play.api.mvc.{AnyContent, Call, Result}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.audit.{AuditService, AuditTypes}
import services.cache.ExportsDeclarationBuilder
import uk.gov.hmrc.http.HeaderCarrier

class NavigatorSpec extends UnitWithMocksSpec with MockExportCacheService with BeforeAndAfterEach with ExportsDeclarationBuilder with RequestBuilder {

  private val mode = Mode.Normal
  private val call: Mode => Call = _ => Call("GET", "url")
  private val config = mock[AppConfig]
  private val auditService = mock[AuditService]
  private val hc: HeaderCarrier = mock[HeaderCarrier]
  private val navigator = new Navigator(config, auditService)

  override def beforeEach(): Unit = {
    super.beforeEach()
    Mockito.reset(config, auditService, hc)
  }

  override def afterEach(): Unit = {
    Mockito.reset(config, auditService, hc)
    super.afterEach()
  }

  private def decoratedRequest(sourceRequest: FakeRequest[AnyContent])(implicit declaration: ExportsDeclaration) =
    new JourneyRequest[AnyContent](buildVerifiedEmailRequest(sourceRequest, mock[SignedInUser]), declaration)

  "Continue To" should {
    val updatedDate = LocalDate.of(2020, 1, 1)
    val expiryDate = LocalDate.of(2020, 1, 1).plusDays(10)

    implicit val declaration = aDeclaration(withUpdateDate(updatedDate))

    def request(action: Option[FormAction]) =
      FakeRequest("GET", "uri")
        .withFormUrlEncodedBody(action.getOrElse("other-field").toString -> "")
        .withSession(ExportsSessionKeys.declarationId -> existingDeclarationId)

    "Go to Save as Draft" in {
      given(config.draftTimeToLive).willReturn(FiniteDuration(10, TimeUnit.DAYS))

      val result = navigator.continueTo(mode, call(_))(decoratedRequest(request(Some(SaveAndReturn))), hc)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(controllers.declaration.routes.ConfirmationController.displayDraftConfirmation().url)
      flash(result).get(FlashKeys.expiryDate) mustBe Some(expiryDate.atStartOfDay(ZoneOffset.UTC).toInstant.toEpochMilli.toString)
      session(result).get(ExportsSessionKeys.declarationId) mustBe None

      verify(auditService).auditAllPagesUserInput(ArgumentMatchers.eq(AuditTypes.SaveAndReturnSubmission), any())(any())
    }

    "Go to URL provided" when {
      "Save And Continue" in {
        val result = navigator.continueTo(mode, call(_))(decoratedRequest(request(Some(SaveAndContinue))), hc)

        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some("url")
        verifyNoInteractions(auditService)
      }

      "Add" in {
        val result = navigator.continueTo(mode, call(_))(decoratedRequest(request(Some(Add))), hc)

        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some("url")
        verifyNoInteractions(auditService)
      }

      "Remove" in {
        val result = navigator.continueTo(mode, call(_))(decoratedRequest(request(Some(Remove(Seq.empty)))), hc)

        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some("url")
        verifyNoInteractions(auditService)
      }

      "Unknown Action" in {
        val result = navigator.continueTo(mode, call(_))(decoratedRequest(request(Some(Unknown))), hc)

        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some("url")
        verifyNoInteractions(auditService)
      }
    }
  }

  "Navigator" should {

    val request = FakeRequest("GET", "uri")
      .withSession(ExportsSessionKeys.declarationId -> existingDeclarationId)

    "redirect to RejectedNotificationsController.displayPage" when {

      val sourceId = "1234"
      implicit val declaration = aDeclaration(withSourceId(sourceId))

      "continueTo method is invoked with mode ErrorFix and sourceId in request" in {

        val result = navigator.continueTo(Mode.ErrorFix, call)(decoratedRequest(request), hc)

        redirectLocation(result) mustBe Some(controllers.routes.RejectedNotificationsController.displayPage(sourceId).url)
      }

      "backLink method is invoked with mode ErrorFix and sourceId in request" in {

        val result = Navigator.backLink(CarrierDetails, Mode.ErrorFix)(decoratedRequest(request))

        result mustBe controllers.routes.RejectedNotificationsController.displayPage(sourceId)
      }

      "backLink method for items is invoked with mode ErrorFix and sourceId in request" in {

        val result = Navigator.backLink(CarrierDetails, Mode.ErrorFix, ItemId("123456"))(decoratedRequest(request))

        result mustBe controllers.routes.RejectedNotificationsController.displayPage(sourceId)
      }
    }

    "redirect to SubmissionsController.displayListOfSubmissions" when {

      implicit val declaration = aDeclaration(withoutSourceId())

      "continueTo method is invoked with mode ErrorFix but without sourceId in request" in {

        val result = navigator.continueTo(Mode.ErrorFix, call)(decoratedRequest(request), hc)

        redirectLocation(result) mustBe Some(controllers.routes.SubmissionsController.displayListOfSubmissions().url)
      }

      "backLink method is invoked with mode ErrorFix but without sourceId in request" in {

        val result = Navigator.backLink(CarrierDetails, Mode.ErrorFix)(decoratedRequest(request))

        result mustBe controllers.routes.SubmissionsController.displayListOfSubmissions()
      }

      "backLink method for items is invoked with mode ErrorFix but without sourceId in request" in {

        val result = Navigator.backLink(CarrierDetails, Mode.ErrorFix, ItemId("123456"))(decoratedRequest(request))

        result mustBe controllers.routes.SubmissionsController.displayListOfSubmissions()
      }
    }
  }

  private implicit def result2future: Result => Future[Result] = Future.successful
}
