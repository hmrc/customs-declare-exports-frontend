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
import controllers.navigation.Navigator
import forms.common.YesNoAnswer
import forms.declaration.AdditionalInformation
import mock.ErrorHandlerMocks
import models.Mode
import models.declaration.AdditionalInformationData
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, times, verify, when}
import play.api.data.Form
import play.api.mvc.{AnyContentAsEmpty, Request}
import play.api.test.Helpers._
import play.twirl.api.HtmlFormat
import services.TariffApiService
import services.TariffApiService.CommodityCodeNotFound
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
      .thenReturn(Future.successful(routes.CommodityMeasureController.displayPage(Mode.Normal, itemId)))
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
    await(controller.displayPage(Mode.Normal, item.id)(request))
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

        val result = controller.displayPage(Mode.Normal, item.id)(getRequest())

        status(result) mustBe OK
        verifyPageInvoked()
      }
    }

    "return 400 (BAD_REQUEST)" when {

      "user provide wrong action" in {

        val requestBody = Seq("yesNo" -> "invalid")
        val result = controller.submitForm(Mode.Normal, itemId)(postRequestAsFormUrlEncoded(requestBody: _*))

        status(result) mustBe BAD_REQUEST
        verifyPageInvoked()
      }
    }

    "return 303 (SEE_OTHER)" when {

      "user has not answered 'do you need to add additional information'" in {

        val result = controller.displayPage(Mode.Normal, itemId)(getRequest())

        await(result) mustBe aRedirectToTheNextPage
        thePageNavigatedTo mustBe routes.AdditionalInformationRequiredController.displayPage(Mode.Normal, itemId)
      }

      "no additional information items in the cache" in {
        val item = anItem(withItemId(itemId), withAdditionalInformationData(AdditionalInformationData(Seq.empty)))
        withNewCaching(aDeclaration(withItems(item)))

        val result = controller.displayPage(Mode.Normal, itemId)(getRequest())

        await(result) mustBe aRedirectToTheNextPage
        thePageNavigatedTo mustBe routes.AdditionalInformationAddController.displayPage(Mode.Normal, itemId)
      }

      "user submits valid Yes answer" in {
        val item = anItem(withAdditionalInformation(additionalInformation))
        withNewCaching(aDeclaration(withItems(item)))

        val requestBody = Seq("yesNo" -> "Yes")
        val result = controller.submitForm(Mode.Normal, itemId)(postRequestAsFormUrlEncoded(requestBody: _*))

        await(result) mustBe aRedirectToTheNextPage
        thePageNavigatedTo mustBe routes.AdditionalInformationAddController.displayPage(Mode.Normal, itemId)
      }

      "user submits valid No answer" in {
        val item = anItem(withAdditionalInformation(additionalInformation))
        withNewCaching(aDeclaration(withItems(item)))

        val requestBody = Seq("yesNo" -> "No")
        val result = controller.submitForm(Mode.Normal, itemId)(postRequestAsFormUrlEncoded(requestBody: _*))

        await(result) mustBe aRedirectToTheNextPage
        thePageNavigatedTo mustBe routes.AdditionalDocumentsController.displayPage(Mode.Normal, itemId)
      }
    }
  }
}
