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

package controllers.navigation

import base.{JourneyTypeTestRunner, MockExportCacheService, RequestBuilder, UnitWithMocksSpec}
import controllers.declaration.routes._
import controllers.helpers._
import controllers.routes.RejectedNotificationsController
import forms.declaration.AdditionalInformationSummary
import mock.FeatureFlagMocks
import models.requests.ExportsSessionKeys.{declarationId, errorFixModeSessionKey}
import models.requests.JourneyRequest
import models.responses.FlashKeys
import models.{DeclarationType, ExportsDeclaration, SignedInUser}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, when}
import org.scalatest.concurrent.ScalaFutures
import play.api.mvc._
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.TariffApiService
import services.TariffApiService.{CommodityCodeNotFound, SupplementaryUnitsNotRequired}
import services.cache.ExportsDeclarationBuilder

import java.time.LocalDate
import scala.concurrent.{ExecutionContext, Future}

class NavigatorSpec
    extends UnitWithMocksSpec with ExportsDeclarationBuilder with JourneyTypeTestRunner with MockExportCacheService with RequestBuilder
    with ScalaFutures with FeatureFlagMocks {

  private val url = "url"
  private val call: Call = Call("GET", url)
  private val tariffApiService = mock[TariffApiService]
  private val inlandOrBorderHelper = mock[InlandOrBorderHelper]
  private val supervisingCustomsOfficeHelper = mock[SupervisingCustomsOfficeHelper]

  private val navigator = new Navigator(tariffApiService, inlandOrBorderHelper, supervisingCustomsOfficeHelper)

  override def afterEach(): Unit = {
    reset(inlandOrBorderHelper, supervisingCustomsOfficeHelper, tariffApiService)
    super.afterEach()
  }

  private def decoratedRequest(sourceRequest: FakeRequest[AnyContent])(implicit declaration: ExportsDeclaration) =
    new JourneyRequest[AnyContent](buildVerifiedEmailRequest(sourceRequest, mock[SignedInUser]), declaration)

  private def requestWithFormAction(action: Option[FormAction], inErrorFixMode: Boolean = false): FakeRequest[AnyContentAsFormUrlEncoded] = {
    val session =
      if (inErrorFixMode) List(declarationId -> existingDeclarationId, errorFixModeSessionKey -> "true")
      else List(declarationId -> existingDeclarationId)

    FakeRequest("GET", "uri")
      .withFormUrlEncodedBody(action.getOrElse("other-field").toString -> "")
      .withSession(session: _*)
  }

  "Continue To" should {
    val updatedDate = LocalDate.of(2020, 1, 1)

    implicit val declaration = aDeclaration(withUpdateDate(updatedDate))

    "go to the URL provided" when {
      "Save And Continue" in {
        val result = navigator.continueTo(call)(decoratedRequest(requestWithFormAction(Some(SaveAndContinue))))

        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(url)
      }

      "Add" in {
        val result = navigator.continueTo(call)(decoratedRequest(requestWithFormAction(Some(Add))))

        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(url)
      }

      "Remove" in {
        val result = navigator.continueTo(call)(decoratedRequest(requestWithFormAction(Some(Remove(Seq.empty)))))

        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(url)
      }

      "Unknown Action" in {
        val result = navigator.continueTo(call)(decoratedRequest(requestWithFormAction(Some(Unknown))))

        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(url)
      }

      "Error-fix flag is passed in error-fix mode" in {
        val result = navigator.continueTo(call)(decoratedRequest(requestWithFormAction(Some(SaveAndContinue))))

        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(url)
      }

      "Add in error-fix mode with error-fix flag passed" in {
        val result = navigator.continueTo(call)(decoratedRequest(requestWithFormAction(Some(Add))))

        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(url)
      }

      "Remove in error-fix mode with error-fix flag passed" in {
        val result = navigator.continueTo(call)(decoratedRequest(requestWithFormAction(Some(Remove(Seq.empty)))))

        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(url)
      }
    }

    "Go to the summary page when Save and return to summary form action" in {
      val result = navigator.continueTo(call)(decoratedRequest(requestWithFormAction(Some(SaveAndReturnToSummary))))

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(SummaryController.displayPage().url)
    }
  }

  "Navigator" should {

    val request = FakeRequest("GET", "uri")
      .withSession(declarationId -> existingDeclarationId)

    "redirect to RejectedNotificationsController.displayPage" when {

      val parentDeclarationId = "1234"
      implicit val declaration = aDeclaration(withParentDeclarationId(parentDeclarationId))

      "Save and return to errors is clicked when in error-fix mode and parentDeclarationId in request" in {
        val request = requestWithFormAction(Some(SaveAndReturnToErrors), true)
        val result = navigator.continueTo(call)(decoratedRequest(request))
        redirectLocation(result) mustBe Some(RejectedNotificationsController.displayPage(parentDeclarationId).url)
      }

      "Save and continue is clicked with mode ErrorFix and parentDeclarationId in request" in {
        val request = requestWithFormAction(Some(SaveAndContinue))
        val result = navigator.continueTo(call)(decoratedRequest(request))
        redirectLocation(result) mustBe Some(url)
      }
    }

    "redirect to the url provided" when {

      "continueTo method is invoked when in error-fix mode and form action SaveAndReturnToErrors and" when {
        "parentDeclarationId is None" in {
          val request = requestWithFormAction(Some(SaveAndReturnToErrors), true)
          val result = navigator.continueTo(call)(decoratedRequest(request)(aDeclaration()))
          result.header.headers.get("Location") mustBe Some(url)
          result.newFlash mustBe Some(Flash(Map.empty))
        }
      }

      "continueTo method is invoked when in error-fix mode and" when {
        "parentDeclarationId is None" in {
          val result = navigator.continueTo(call)(decoratedRequest(request)(aDeclaration()))
          result.header.headers.get("Location") mustBe Some(url)
          result.newFlash mustBe None
        }
      }
    }

    "preserve any Flash Data" when {
      "continueTo method is invoked when in error-fix mode" in {
        val flash = Map(FlashKeys.fieldName -> "Some name", FlashKeys.errorMessage -> "Some message")
        val request = requestWithFormAction(Some(Unknown), true).withFlash(flash.toList: _*)
        val result = navigator.continueTo(call)(decoratedRequest(request)(aDeclaration()))
        result.newFlash mustBe Some(Flash(flash))
      }
    }
  }

  "Navigator.backLinkForAdditionalInformation" should {

    implicit val ec: ExecutionContext = ExecutionContext.global

    val itemId = "itemId"

    onJourney(DeclarationType.STANDARD, DeclarationType.SUPPLEMENTARY) { implicit request =>
      "return a Call instance for CommodityMeasureController" when {
        "the response from Tariff API does not include supplementary units" in {
          when(tariffApiService.retrieveCommodityInfoIfAny(any(), any()))
            .thenReturn(Future.successful(Left(SupplementaryUnitsNotRequired)))

          val url = navigator.backLinkForAdditionalInformation(AdditionalInformationSummary, itemId).futureValue.url

          url mustBe CommodityMeasureController.displayPage(itemId).url
        }
      }

      "return a Call instance for SupplementaryUnitsController" when {
        "the response from Tariff API does not include supplementary units" in {
          when(tariffApiService.retrieveCommodityInfoIfAny(any(), any()))
            .thenReturn(Future.successful(Left(CommodityCodeNotFound)))

          val url = navigator.backLinkForAdditionalInformation(AdditionalInformationSummary, itemId).futureValue.url

          url mustBe SupplementaryUnitsController.displayPage(itemId).url
        }
      }
    }

    onClearance { implicit request =>
      "return a Call instance for CommodityMeasureController" in {
        val url = navigator.backLinkForAdditionalInformation(AdditionalInformationSummary, itemId).futureValue.url

        url mustBe CommodityMeasureController.displayPage(itemId).url
      }
    }

    onJourney(DeclarationType.SIMPLIFIED, DeclarationType.OCCASIONAL) { implicit request =>
      "return a Call instance for PackageInformationSummaryController" in {
        val url = navigator.backLinkForAdditionalInformation(AdditionalInformationSummary, itemId).futureValue.url

        url mustBe PackageInformationSummaryController.displayPage(itemId).url
      }
    }
  }

  private implicit def result2future: Result => Future[Result] = Future.successful
}
