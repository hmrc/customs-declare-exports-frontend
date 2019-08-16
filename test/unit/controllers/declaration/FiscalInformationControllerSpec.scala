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

package unit.controllers.declaration

import controllers.declaration.FiscalInformationController
import forms.Choice.AllowedChoiceValues.SupplementaryDec
import forms.declaration.{ExporterDetails, FiscalInformation}
import forms.declaration.FiscalInformation.AllowedFiscalInformationAnswers._
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, times, verify, when}
import org.scalatest.OptionValues
import play.api.data.Form
import play.api.libs.json.Json
import play.api.test.Helpers._
import play.twirl.api.HtmlFormat
import unit.base.ControllerSpec
import views.html.declaration.fiscal_information

class FiscalInformationControllerSpec extends ControllerSpec with OptionValues {

  val mockFiscalInformationPage = mock[fiscal_information]

  val controller = new FiscalInformationController(
    mockAuthAction,
    mockJourneyAction,
    mockExportsCacheService,
    stubMessagesControllerComponents(),
    mockFiscalInformationPage
  )(ec)

  override protected def beforeEach(): Unit = {
    super.beforeEach()
    authorizedUser()
    withNewCaching(aDeclaration(withChoice(SupplementaryDec)))
    when(mockFiscalInformationPage.apply(any(), any())(any(), any())).thenReturn(HtmlFormat.empty)
  }

  override protected def afterEach(): Unit = {
    super.afterEach()
    reset(mockFiscalInformationPage)
  }

  val itemId = "itemId"

  def checkViewInteractions(noOfInvocations: Int = 1): Unit =
    verify(mockFiscalInformationPage, times(noOfInvocations)).apply(any(), any())(any(), any())

  def theResponseForm: Form[FiscalInformation] = {
    val captor = ArgumentCaptor.forClass(classOf[Form[FiscalInformation]])
    verify(mockFiscalInformationPage).apply(any(), captor.capture())(any(), any())
    captor.getValue
  }

  "Fiscal Information controller" should {

    "return 200 (OK)" when {

      "display page method is invoked and cache is empty" in {

        val result = controller.displayPage(itemId)(getRequest())

        status(result) mustBe OK
        checkViewInteractions()

        theResponseForm.value mustBe empty
      }

      "display page method is invoked and cache contains data" in {
        val item = anItem( withFiscalInformation(FiscalInformation(yes)))
        withNewCaching(aDeclaration(withItems(item)))

        val result = controller.displayPage(item.id)(getRequest())

        status(result) mustBe OK
        checkViewInteractions()

        theResponseForm.value.value.onwardSupplyRelief mustBe yes
      }
    }

    "return 400 (BAD_REQUEST)" when {

      "form is incorrect" in {

        val incorrectForm = Json.toJson(FiscalInformation("IncorrectValue"))

        val result = controller.saveFiscalInformation(itemId)(postRequest(incorrectForm))

        status(result) mustBe BAD_REQUEST
        checkViewInteractions()
      }
    }

    "return 303 (SEE_OTHER)" when {

      "user answer yes" in {

        val correctForm = Json.toJson(FiscalInformation("Yes"))

        val result = controller.saveFiscalInformation(itemId)(postRequest(correctForm))

        status(result) mustBe SEE_OTHER
        checkViewInteractions(0)
      }

      "user answer no" in {

        val correctForm = Json.toJson(FiscalInformation("No"))

        val result = controller.saveFiscalInformation(itemId)(postRequest(correctForm))

        status(result) mustBe SEE_OTHER
        checkViewInteractions(0)
      }
    }
  }
}
