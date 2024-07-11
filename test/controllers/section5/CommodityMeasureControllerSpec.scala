/*
 * Copyright 2023 HM Revenue & Customs
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

package controllers.section5

import base.{AuditedControllerSpec, ControllerSpec}
import controllers.general.routes.RootController
import controllers.section5.routes.{AdditionalInformationRequiredController, SupplementaryUnitsController}
import forms.section5.commodityMeasure.CommodityMeasure
import models.DeclarationType.{CLEARANCE, OCCASIONAL, SIMPLIFIED, STANDARD, SUPPLEMENTARY}
import models.declaration.{CommodityMeasure => CM, ExportItem}
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, verify, when}
import play.api.data.Form
import play.api.libs.json.Json
import play.api.mvc.{AnyContentAsEmpty, Request}
import play.api.test.Helpers._
import play.twirl.api.HtmlFormat
import views.html.section5.commodityMeasure.commodity_measure

class CommodityMeasureControllerSpec extends ControllerSpec with AuditedControllerSpec {

  private val format = Json.format[CommodityMeasure]

  private val commodityMeasurePage = mock[commodity_measure]

  private val controller =
    new CommodityMeasureController(mockAuthAction, mockJourneyAction, mockExportsCacheService, navigator, mcc, commodityMeasurePage)(ec, auditService)

  override protected def beforeEach(): Unit = {
    super.beforeEach()
    authorizedUser()
    when(commodityMeasurePage.apply(any(), any())(any(), any())).thenReturn(HtmlFormat.empty)
  }

  override protected def afterEach(): Unit = {
    super.afterEach()
    reset(commodityMeasurePage)
  }

  def theResponseForm: Form[CommodityMeasure] = {
    val captor = ArgumentCaptor.forClass(classOf[Form[CommodityMeasure]])
    verify(commodityMeasurePage).apply(any(), captor.capture())(any(), any())
    captor.getValue
  }

  override def getFormForDisplayRequest(request: Request[AnyContentAsEmpty.type]): Form[_] = {
    withNewCaching(aDeclaration())
    await(controller.displayPage("itemId")(request))
    theResponseForm
  }

  "Commodity Measure controller" should {

    "return 200 (OK) on displayOutcomePage" when {
      onJourney(STANDARD, SUPPLEMENTARY, CLEARANCE) { request =>
        "display page method is invoked and commodity measure cache is empty" in {
          withNewCaching(request.cacheModel)

          val result = controller.displayPage("itemId")(getRequest())

          status(result) must be(OK)
          theResponseForm.value mustBe empty
        }

        "display page method is invoked and commodity measure cache is not empty" in {
          val item = ExportItem("itemId", commodityMeasure = Some(CM(None, None, Some("1000"), Some("500"))))
          withNewCaching(aDeclaration(withType(request.declarationType), withItem(item)))

          val result = controller.displayPage("itemId")(getRequest())

          status(result) must be(OK)
          theResponseForm.value mustBe Some(CommodityMeasure(Some("1000"), Some("500")))
        }
      }
    }

    "return 400 (BAD_REQUEST) on submitPage" when {
      onJourney(STANDARD, SUPPLEMENTARY, CLEARANCE) { request =>
        "form is incorrect" in {
          withNewCaching(aDeclaration(withType(request.declarationType)))

          val incorrectForm = Json.toJson(CommodityMeasure(Some("0"), None))(format)

          val result = controller.submitPage("itemId")(postRequest(incorrectForm))

          status(result) must be(BAD_REQUEST)
          verify(commodityMeasurePage).apply(any(), any())(any(), any())
          verifyNoAudit()
        }
      }
    }

    "return 303 (SEE_OTHER) on submitPage" when {

      val correctForm = Json.toJson(CommodityMeasure(Some("1000"), Some("500")))(format)

      onJourney(STANDARD, SUPPLEMENTARY) { request =>
        "information provided by user are correct" in {
          withNewCaching(request.cacheModel)

          val result = controller.submitPage("itemId")(postRequest(correctForm))

          await(result) mustBe aRedirectToTheNextPage
          thePageNavigatedTo mustBe SupplementaryUnitsController.displayPage("itemId")
          verifyAudit()
        }
      }

      onClearance { request =>
        "information provided by user are correct" in {
          withNewCaching(request.cacheModel)

          val result = controller.submitPage("itemId")(postRequest(correctForm))

          await(result) mustBe aRedirectToTheNextPage
          thePageNavigatedTo mustBe AdditionalInformationRequiredController.displayPage("itemId")
          verifyAudit()
        }
      }
    }

    "redirect to the Choice page at '/'" when {
      onJourney(OCCASIONAL, SIMPLIFIED) { request =>
        "the journey is not valid for displayOutcomePage" in {
          withNewCaching(request.cacheModel)

          val response = controller.displayPage("itemId").apply(getRequest())

          status(response) must be(SEE_OTHER)
          redirectLocation(response) mustBe Some(RootController.displayPage.url)
          verifyNoAudit()
        }

        "the journey is not valid for submitPage" in {
          withNewCaching(request.cacheModel)

          val response = controller.submitPage("itemId").apply(getRequest())

          status(response) must be(SEE_OTHER)
          redirectLocation(response) mustBe Some(RootController.displayPage.url)
          verifyNoAudit()
        }
      }
    }
  }
}
