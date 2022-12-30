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

package controllers.declaration

import scala.concurrent.Future

import base.ControllerSpec
import controllers.declaration.routes.AdditionalInformationRequiredController
import controllers.routes.RootController
import forms.declaration.commodityMeasure.SupplementaryUnits
import forms.declaration.commodityMeasure.SupplementaryUnits.{hasSupplementaryUnits, supplementaryUnits}
import models.DeclarationType.{CLEARANCE, OCCASIONAL, SIMPLIFIED, STANDARD, SUPPLEMENTARY}
import models.declaration.CommodityMeasure
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, verify, when}
import play.api.data.Form
import play.api.libs.json.Json
import play.api.mvc.{AnyContentAsEmpty, Request}
import play.api.test.Helpers._
import play.twirl.api.HtmlFormat
import services.TariffApiService.{CommodityCodeNotFound, SupplementaryUnitsNotRequired}
import services.{CommodityInfo, TariffApiService}
import views.html.declaration.commodityMeasure.{supplementary_units, supplementary_units_yes_no}

class SupplementaryUnitsControllerSpec extends ControllerSpec {

  private val supplementaryUnitsPage = mock[supplementary_units]
  private val supplementaryUnitsYesNoPage = mock[supplementary_units_yes_no]

  private val tariffApiService = mock[TariffApiService]

  private val controller = new SupplementaryUnitsController(
    mockAuthAction,
    mockJourneyAction,
    mockExportsCacheService,
    tariffApiService,
    navigator,
    stubMessagesControllerComponents(),
    supplementaryUnitsPage,
    supplementaryUnitsYesNoPage
  )(ec)

  override protected def beforeEach(): Unit = {
    super.beforeEach()
    authorizedUser()
    when(tariffApiService.retrieveCommodityInfoIfAny(any(), any())).thenReturn(Future.successful(Left(CommodityCodeNotFound)))
    when(supplementaryUnitsPage.apply(any(), any(), any())(any(), any())).thenReturn(HtmlFormat.empty)
    when(supplementaryUnitsYesNoPage.apply(any(), any())(any(), any())).thenReturn(HtmlFormat.empty)
  }

  override protected def afterEach(): Unit = {
    super.afterEach()
    reset(supplementaryUnitsPage, supplementaryUnitsYesNoPage, tariffApiService)
  }

  def responseMandatoryForm: Form[SupplementaryUnits] = {
    val captor = ArgumentCaptor.forClass(classOf[Form[SupplementaryUnits]])
    verify(supplementaryUnitsPage).apply(any(), captor.capture(), any())(any(), any())
    captor.getValue
  }

  def responseYesNoForm: Form[SupplementaryUnits] = {
    val captor = ArgumentCaptor.forClass(classOf[Form[SupplementaryUnits]])
    verify(supplementaryUnitsYesNoPage).apply(any(), captor.capture())(any(), any())
    captor.getValue
  }

  override def getFormForDisplayRequest(request: Request[AnyContentAsEmpty.type]): Form[_] = {
    withNewCaching(aDeclaration())
    await(controller.displayPage("itemId")(request))
    responseYesNoForm
  }

  private val commodityInfo = CommodityInfo("2208303000", "description", "units")

  "SupplementaryUnitsController.displayPage" when {

    onJourney(STANDARD, SUPPLEMENTARY) { request =>
      "the given commodity code was not found by calling the Tariff API" should {

        "display an empty supplementary_units_yes_no Page" when {
          "no supplementary units are cached yet" in {
            withNewCaching(request.cacheModel)

            val result = controller.displayPage("itemdId")(getRequest())

            status(result) must be(OK)
            responseYesNoForm.value mustBe empty
          }
        }

        "display a filled-in supplementary_units_yes_no Page" when {
          "supplementary units have already been cached" in {
            val commodityMeasure = CommodityMeasure(Some("100"), Some(false), Some("1000"), Some("500"))
            val item = anItem(withCommodityMeasure(commodityMeasure))
            withNewCaching(aDeclarationAfter(request.cacheModel, withItems(item)))

            val result = controller.displayPage(item.id)(getRequest())

            status(result) must be(OK)
            responseYesNoForm.value mustBe Some(SupplementaryUnits(Some("100")))
          }
        }
      }

      "the cached commodity code does require supplementary units" should {

        "display an empty supplementary_units Page" when {
          "no supplementary units are cached yet" in {
            when(tariffApiService.retrieveCommodityInfoIfAny(any(), any())).thenReturn(Future.successful(Right(commodityInfo)))

            withNewCaching(request.cacheModel)

            val result = controller.displayPage("itemId")(getRequest())

            status(result) must be(OK)
            responseMandatoryForm.value mustBe empty
          }
        }

        "return a mandatoryForm with submission errors" when {
          "the cached commodity code does require supplementary units" in {
            when(tariffApiService.retrieveCommodityInfoIfAny(any(), any())).thenReturn(Future.successful(Right(commodityInfo)))

            withNewCaching(aDeclaration())
            await(controller.displayPage("itemId")(getRequestWithSubmissionErrors))
            responseMandatoryForm.errors mustBe Seq(submissionFormError)
          }
        }

        "display a filled-in supplementary_units Page" when {
          "supplementary units have already been cached" in {
            when(tariffApiService.retrieveCommodityInfoIfAny(any(), any())).thenReturn(Future.successful(Right(commodityInfo)))

            val commodityMeasure = CommodityMeasure(Some("100"), Some(false), Some("1000"), Some("500"))
            val item = anItem(withCommodityMeasure(commodityMeasure))
            withNewCaching(aDeclarationAfter(request.cacheModel, withItems(item)))

            val result = controller.displayPage(item.id)(getRequest())

            status(result) must be(OK)
            responseMandatoryForm.value mustBe Some(SupplementaryUnits(Some("100")))
          }
        }
      }

      "the cached commodity code does NOT require supplementary units" should {
        "redirect to the page at '/is-additional-information-required'" in {
          when(tariffApiService.retrieveCommodityInfoIfAny(any(), any())).thenReturn(Future.successful(Left(SupplementaryUnitsNotRequired)))

          withNewCaching(request.cacheModel)

          val response = controller.displayPage("itemId").apply(getRequest())

          await(response) mustBe aRedirectToTheNextPage
          thePageNavigatedTo mustBe AdditionalInformationRequiredController.displayPage("itemId")
        }
      }
    }

    onJourney(CLEARANCE, OCCASIONAL, SIMPLIFIED) { request =>
      "redirect to the Choice page at '/'" in {
        withNewCaching(request.cacheModel)

        val response = controller.displayPage("itemId").apply(getRequest())

        status(response) must be(SEE_OTHER)
        redirectLocation(response) mustBe Some(RootController.displayPage.url)
      }
    }
  }

  "SupplementaryUnitsController.submitPage" when {

    onJourney(STANDARD, SUPPLEMENTARY) { request =>
      "the given commodity code was not found by calling the Tariff API" should {

        "return 303 (SEE_OTHER)" when {
          "information provided by the user are correct" in {
            withNewCaching(request.cacheModel)

            val correctForm = Json.obj(hasSupplementaryUnits -> "Yes", supplementaryUnits -> "100")

            val result = controller.submitPage("itemId")(postRequest(correctForm))

            await(result) mustBe aRedirectToTheNextPage
            thePageNavigatedTo mustBe AdditionalInformationRequiredController.displayPage("itemId")
          }
        }

        "return 400 (BAD_REQUEST)" when {
          "information provided by the user are NOT correct" in {
            withNewCaching(request.cacheModel)

            val incorrectForm = Json.obj(hasSupplementaryUnits -> "Yes", supplementaryUnits -> "abcd")

            val result = controller.submitPage("itemId")(postRequest(incorrectForm))

            status(result) must be(BAD_REQUEST)
            verify(supplementaryUnitsYesNoPage).apply(any(), any())(any(), any())
          }
        }
      }

      "the cached commodity code does require supplementary units" should {

        "return 303 (SEE_OTHER)" when {
          "information provided by the user are correct" in {
            when(tariffApiService.retrieveCommodityInfoIfAny(any(), any())).thenReturn(Future.successful(Right(commodityInfo)))

            withNewCaching(request.cacheModel)

            val correctForm = Json.obj(supplementaryUnits -> "100")

            val result = controller.submitPage("itemId")(postRequest(correctForm))

            await(result) mustBe aRedirectToTheNextPage
            thePageNavigatedTo mustBe AdditionalInformationRequiredController.displayPage("itemId")
          }
        }

        "return 400 (BAD_REQUEST)" when {
          "information provided by the user are NOT correct" in {
            when(tariffApiService.retrieveCommodityInfoIfAny(any(), any())).thenReturn(Future.successful(Right(commodityInfo)))

            withNewCaching(request.cacheModel)

            val incorrectForm = Json.obj(supplementaryUnits -> "abcd")

            val result = controller.submitPage("itemId")(postRequest(incorrectForm))

            status(result) must be(BAD_REQUEST)
            verify(supplementaryUnitsPage).apply(any(), any(), any())(any(), any())
          }
        }
      }
    }

    onJourney(CLEARANCE, OCCASIONAL, SIMPLIFIED) { request =>
      "redirect to the Choice page at '/'" in {
        withNewCaching(request.cacheModel)

        val response = controller.submitPage("itemId").apply(getRequest())

        status(response) must be(SEE_OTHER)
        redirectLocation(response) mustBe Some(RootController.displayPage.url)
      }
    }
  }
}
