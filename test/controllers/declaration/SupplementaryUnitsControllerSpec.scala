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

package controllers.declaration

import base.ControllerSpec
import forms.declaration.commodityMeasure.SupplementaryUnits
import forms.declaration.commodityMeasure.SupplementaryUnits.{hasSupplementaryUnits, supplementaryUnits}
import models.DeclarationType.{CLEARANCE, OCCASIONAL, SIMPLIFIED, STANDARD, SUPPLEMENTARY}
import models.Mode
import models.declaration.{CommodityMeasure, ExportItem}
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, verify, when}
import play.api.data.Form
import play.api.libs.json.Json
import play.api.mvc.{AnyContentAsEmpty, Request}
import play.api.test.Helpers._
import play.twirl.api.HtmlFormat
import views.html.declaration.commodityMeasure.supplementary_units

class SupplementaryUnitsControllerSpec extends ControllerSpec {

  private val supplementaryUnitsPage = mock[supplementary_units]

  private val controller = new SupplementaryUnitsController(
    mockAuthAction,
    mockJourneyAction,
    mockExportsCacheService,
    navigator,
    stubMessagesControllerComponents(),
    supplementaryUnitsPage
  )(ec)

  override protected def beforeEach(): Unit = {
    super.beforeEach()
    authorizedUser()
    when(supplementaryUnitsPage.apply(any(), any(), any())(any(), any())).thenReturn(HtmlFormat.empty)
  }

  override protected def afterEach(): Unit = {
    super.afterEach()
    reset(supplementaryUnitsPage)
  }

  def theResponseForm: Form[SupplementaryUnits] = {
    val captor = ArgumentCaptor.forClass(classOf[Form[SupplementaryUnits]])
    verify(supplementaryUnitsPage).apply(any(), any(), captor.capture())(any(), any())
    captor.getValue
  }

  override def getFormForDisplayRequest(request: Request[AnyContentAsEmpty.type]): Form[_] = {
    withNewCaching(aDeclaration())
    await(controller.displayPage(Mode.Normal, "itemId")(request))
    theResponseForm
  }

  "Supplementary Units controller" should {

    "return 200 (OK) on displayPage" when {
      onJourney(STANDARD, SUPPLEMENTARY) { request =>
        "display page method is invoked and commodity measure cache is empty" in {
          withNewCaching(request.cacheModel)

          val result = controller.displayPage(Mode.Normal, "itemId")(getRequest())

          status(result) must be(OK)
          theResponseForm.value mustBe empty
        }

        "display page method is invoked and commodity measure cache is not empty" in {
          val commodityMeasure = CommodityMeasure(Some("100"), Some(false), Some("1000"), Some("500"))
          val item = ExportItem("itemId", commodityMeasure = Some(commodityMeasure))
          withNewCaching(aDeclaration(withType(request.declarationType), withItem(item)))

          val result = controller.displayPage(Mode.Normal, "itemId")(getRequest())

          status(result) must be(OK)
          theResponseForm.value mustBe Some(SupplementaryUnits(Some("100")))
        }
      }
    }

    "return 400 (BAD_REQUEST) on submitPage" when {
      onJourney(STANDARD, SUPPLEMENTARY) { request =>
        "form is incorrect" in {
          withNewCaching(aDeclaration(withType(request.declarationType)))

          val incorrectForm = Json.obj(hasSupplementaryUnits -> "Yes", supplementaryUnits -> "abcd")

          val result = controller.submitPage(Mode.Normal, "itemId")(postRequest(incorrectForm))

          status(result) must be(BAD_REQUEST)
          verify(supplementaryUnitsPage).apply(any(), any(), any())(any(), any())
        }
      }
    }

    "return 303 (SEE_OTHER) on submitPage" when {
      onJourney(STANDARD, SUPPLEMENTARY) { request =>
        "information provided by user are correct" in {
          withNewCaching(request.cacheModel)

          val correctForm = Json.obj(hasSupplementaryUnits -> "Yes", supplementaryUnits -> "100")

          val result = controller.submitPage(Mode.Normal, "itemId")(postRequest(correctForm))

          await(result) mustBe aRedirectToTheNextPage
          thePageNavigatedTo mustBe routes.AdditionalInformationRequiredController.displayPage(Mode.Normal, "itemId")
        }
      }
    }

    "redirect to the Choice page at '/'" when {
      onJourney(CLEARANCE, OCCASIONAL, SIMPLIFIED) { request =>
        "the journey is not valid for displayPage" in {
          withNewCaching(request.cacheModel)

          val response = controller.displayPage(Mode.Normal, "itemId").apply(getRequest())

          status(response) must be(SEE_OTHER)
          redirectLocation(response) mustBe Some(controllers.routes.RootController.displayPage().url)
        }

        "the journey is not valid for submitPage" in {
          withNewCaching(request.cacheModel)

          val response = controller.submitPage(Mode.Normal, "itemId").apply(getRequest())

          status(response) must be(SEE_OTHER)
          redirectLocation(response) mustBe Some(controllers.routes.RootController.displayPage().url)
        }
      }
    }
  }
}
