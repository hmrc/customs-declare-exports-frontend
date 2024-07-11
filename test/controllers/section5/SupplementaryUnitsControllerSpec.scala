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
import controllers.section5.routes.AdditionalInformationRequiredController
import forms.section5.commodityMeasure.SupplementaryUnits
import forms.section5.commodityMeasure.SupplementaryUnits.{hasSupplementaryUnits, supplementaryUnits}
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
import views.html.section5.commodityMeasure.supplementary_units_yes_no

class SupplementaryUnitsControllerSpec extends ControllerSpec with AuditedControllerSpec {

  private val supplementaryUnitsYesNoPage = mock[supplementary_units_yes_no]

  private val controller =
    new SupplementaryUnitsController(mockAuthAction, mockJourneyAction, mockExportsCacheService, navigator, mcc, supplementaryUnitsYesNoPage)(
      ec,
      auditService
    )

  override protected def beforeEach(): Unit = {
    super.beforeEach()
    authorizedUser()
    when(supplementaryUnitsYesNoPage.apply(any(), any())(any(), any())).thenReturn(HtmlFormat.empty)
  }

  override protected def afterEach(): Unit = {
    super.afterEach()
    reset(supplementaryUnitsYesNoPage, auditService)
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

  "SupplementaryUnitsController.displayOutcomePage" should {

    onJourney(STANDARD, SUPPLEMENTARY) { request =>
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

    onJourney(CLEARANCE, OCCASIONAL, SIMPLIFIED) { request =>
      "redirect to the Choice page at '/'" in {
        withNewCaching(request.cacheModel)

        val response = controller.displayPage("itemId").apply(getRequest())

        status(response) must be(SEE_OTHER)
        redirectLocation(response) mustBe Some(RootController.displayPage.url)
      }
    }
  }

  "SupplementaryUnitsController.submitPage" should {

    onJourney(STANDARD, SUPPLEMENTARY) { request =>
      "return 303 (SEE_OTHER)" when {
        "information provided by the user are correct" in {
          withNewCaching(request.cacheModel)

          val correctForm = Json.obj(hasSupplementaryUnits -> "Yes", supplementaryUnits -> "100")

          val result = controller.submitPage("itemId")(postRequest(correctForm))

          await(result) mustBe aRedirectToTheNextPage
          thePageNavigatedTo mustBe AdditionalInformationRequiredController.displayPage("itemId")
          verifyAudit()
        }
      }

      "return 400 (BAD_REQUEST)" when {
        "information provided by the user are NOT correct" in {
          withNewCaching(request.cacheModel)

          val incorrectForm = Json.obj(hasSupplementaryUnits -> "Yes", supplementaryUnits -> "abcd")

          val result = controller.submitPage("itemId")(postRequest(incorrectForm))

          status(result) must be(BAD_REQUEST)
          verify(supplementaryUnitsYesNoPage).apply(any(), any())(any(), any())
          verifyNoAudit()
        }
      }
    }

    onJourney(CLEARANCE, OCCASIONAL, SIMPLIFIED) { request =>
      "redirect to the Choice page at '/'" in {
        withNewCaching(request.cacheModel)

        val response = controller.submitPage("itemId").apply(getRequest())

        status(response) must be(SEE_OTHER)
        redirectLocation(response) mustBe Some(RootController.displayPage.url)
        verifyNoAudit()
      }
    }
  }
}
