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

import base.ControllerSpec
import forms.common.YesNoAnswer
import forms.declaration.AdditionalInformation
import mock.ErrorHandlerMocks
import models.DeclarationType._
import models.declaration.AdditionalInformationData
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, times, verify, when}
import play.api.data.Form
import play.api.libs.json.Json
import play.api.mvc.{AnyContentAsEmpty, Request}
import play.api.test.Helpers._
import play.twirl.api.HtmlFormat
import views.html.declaration.additionalInformation.additional_information

import scala.concurrent.Future

class AdditionalInformationControllerSpec extends ControllerSpec with ErrorHandlerMocks {

  private val mockSummaryPage = mock[additional_information]

  val controller = new AdditionalInformationController(
    mockAuthAction,
    mockJourneyAction,
    mockExportsCacheService,
    navigator,
    stubMessagesControllerComponents(),
    mockSummaryPage
  )

  val itemId = "itemId"

  override protected def beforeEach(): Unit = {
    super.beforeEach()
    authorizedUser()
    withNewCaching(aDeclaration())

    when(mockSummaryPage.apply(any(), any(), any(), any(), any())(any(), any())).thenReturn(HtmlFormat.empty)
    when(navigator.backLinkForAdditionalInformation(any(), any(), any())(any(), any()))
      .thenReturn(Future.successful(routes.CommodityMeasureController.displayPage(itemId)))
  }

  override protected def afterEach(): Unit = {
    super.afterEach()
    reset(mockSummaryPage)
  }

  def theResponseForm: Form[YesNoAnswer] = {
    val formCaptor = ArgumentCaptor.forClass(classOf[Form[YesNoAnswer]])
    verify(mockSummaryPage).apply(any(), any(), formCaptor.capture(), any(), any())(any(), any())
    formCaptor.getValue
  }

  override def getFormForDisplayRequest(request: Request[AnyContentAsEmpty.type]): Form[_] = {
    val item = anItem(withAdditionalInformation(additionalInformation))
    withNewCaching(aDeclaration(withItems(item)))
    await(controller.displayPage(item.id)(request))
    theResponseForm
  }

  private def verifyPageInvoked(numberOfTimes: Int = 1): HtmlFormat.Appendable =
    verify(mockSummaryPage, times(numberOfTimes)).apply(any(), any(), any(), any(), any())(any(), any())

  private val additionalInformation = AdditionalInformation("54321", "Some description")

  "AdditionalInformation controller" should {

    "return 200 (OK)" when {

      "display page method is invoked with data in cache" in {

        val item = anItem(withAdditionalInformation(additionalInformation))
        withNewCaching(aDeclaration(withItems(item)))

        val result = controller.displayPage(item.id)(getRequest())

        status(result) mustBe OK
        verifyPageInvoked()
      }
    }

    "return 400 (BAD_REQUEST)" when {

      "user provide wrong action" in {

        val requestBody = Json.obj("yesNo" -> "invalid")
        val result = controller.submitForm(itemId)(postRequest(requestBody))

        status(result) mustBe BAD_REQUEST
        verifyPageInvoked()
      }
    }

    "return 303 (SEE_OTHER)" when {

      "user has not answered 'do you need to add additional information'" in {

        val result = controller.displayPage(itemId)(getRequest())

        await(result) mustBe aRedirectToTheNextPage
        thePageNavigatedTo mustBe routes.AdditionalInformationRequiredController.displayPage(itemId)
      }

      "no additional information items in the cache" in {
        val item = anItem(withItemId(itemId), withAdditionalInformationData(AdditionalInformationData(Seq.empty)))
        withNewCaching(aDeclaration(withItems(item)))

        val result = controller.displayPage(itemId)(getRequest())

        await(result) mustBe aRedirectToTheNextPage
        thePageNavigatedTo mustBe routes.AdditionalInformationAddController.displayPage(itemId)
      }

      "user submits valid Yes answer" in {
        val item = anItem(withAdditionalInformation(additionalInformation))
        withNewCaching(aDeclaration(withItems(item)))

        val requestBody = Json.obj("yesNo" -> "Yes")
        val result = controller.submitForm(itemId)(postRequest(requestBody))

        await(result) mustBe aRedirectToTheNextPage
        thePageNavigatedTo mustBe routes.AdditionalInformationAddController.displayPage(itemId)
      }

      "user submits valid Yes answer in error-fix mode" in {
        val item = anItem(withAdditionalInformation(additionalInformation))
        withNewCaching(aDeclaration(withItems(item)))

        val requestBody = Json.obj("yesNo" -> "Yes")
        val result = controller.submitForm(Mode.ErrorFix, itemId)(postRequest(requestBody))

        await(result) mustBe aRedirectToTheNextPage
        thePageNavigatedTo mustBe routes.AdditionalInformationAddController.displayPage(Mode.ErrorFix, itemId)
      }

      onJourney(STANDARD, SIMPLIFIED, OCCASIONAL, SUPPLEMENTARY)(aDeclaration()) { implicit request =>
        "user submits valid No answer redirect to `Is License Required?` " in {

          withNewCaching(aDeclarationAfter(request.cacheModel, withItems(anItem(withAdditionalInformation(additionalInformation)))))

          val requestBody = Json.obj("yesNo" -> "No")
          val result = controller.submitForm(itemId)(postRequest(requestBody))

          await(result) mustBe aRedirectToTheNextPage
          thePageNavigatedTo mustBe routes.IsLicenceRequiredController.displayPage(itemId)
        }

      }

      onJourney(CLEARANCE)(aDeclaration()) { implicit request =>
        "user submits valid No answer redirect to `Additional Documents` " in {

          withNewCaching(aDeclarationAfter(request.cacheModel, withItems(anItem(withAdditionalInformation(additionalInformation)))))

          val requestBody = Json.obj("yesNo" -> "No")
          val result = controller.submitForm(itemId)(postRequest(requestBody))

          await(result) mustBe aRedirectToTheNextPage
          thePageNavigatedTo mustBe routes.AdditionalDocumentsController.displayPage(itemId)
        }

      }

    }
  }
}
