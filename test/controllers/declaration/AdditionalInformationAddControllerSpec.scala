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
import controllers.declaration.routes.AdditionalInformationController
import forms.common.YesNoAnswer.{No, Yes}
import forms.declaration.AdditionalInformation
import mock.ErrorHandlerMocks
import models.declaration.AdditionalInformationData
import models.declaration.AdditionalInformationData.maxNumberOfItems
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, times, verify, when}
import play.api.data.Form
import play.api.libs.json.Json
import play.api.mvc.{AnyContentAsEmpty, Request}
import play.api.test.Helpers._
import play.twirl.api.HtmlFormat
import views.html.declaration.additionalInformation.additional_information_add

class AdditionalInformationAddControllerSpec extends ControllerSpec with ErrorHandlerMocks {

  val mockAddPage = mock[additional_information_add]

  val controller = new AdditionalInformationAddController(
    mockAuthAction,
    mockJourneyAction,
    mockExportsCacheService,
    navigator,
    stubMessagesControllerComponents(),
    mockAddPage
  )(ec)

  val itemId = "itemId"

  override protected def beforeEach(): Unit = {
    super.beforeEach()
    authorizedUser()
    withNewCaching(aDeclaration(withItem(anItem(withItemId(itemId)))))
    when(mockAddPage.apply(any(), any(), any())(any(), any())).thenReturn(HtmlFormat.empty)
  }

  override protected def afterEach(): Unit = {
    super.afterEach()
    reset(mockAddPage)
  }

  def theResponseForm: Form[AdditionalInformation] = {
    val formCaptor = ArgumentCaptor.forClass(classOf[Form[AdditionalInformation]])
    verify(mockAddPage).apply(any(), any(), formCaptor.capture())(any(), any())
    formCaptor.getValue
  }

  override def getFormForDisplayRequest(request: Request[AnyContentAsEmpty.type]): Form[_] = {
    await(controller.displayPage(itemId)(request))
    theResponseForm
  }

  private def verifyPageInvoked(numberOfTimes: Int = 1): HtmlFormat.Appendable =
    verify(mockAddPage, times(numberOfTimes)).apply(any(), any(), any())(any(), any())

  private val additionalInformation = AdditionalInformation("12345", "Description")

  "AdditionalInformationAdd controller" should {

    "return 200 (OK)" when {
      "display page method is invoked" in {
        val result = controller.displayPage(itemId)(getRequest())

        status(result) mustBe OK
        verifyPageInvoked()
      }
    }

    "return 400 (BAD_REQUEST) during adding" when {

      "user does not enter any data" in {
        val formData = Json.toJson(AdditionalInformation("", ""))
        val result = controller.submitForm(itemId)(postRequest(formData))

        status(result) mustBe BAD_REQUEST
        verifyPageInvoked()
      }

      "user enters 'RRS01' as code" in {
        val formData = Json.toJson(AdditionalInformation("RRS01", "description"))
        val result = controller.submitForm(itemId)(postRequest(formData))

        status(result) mustBe BAD_REQUEST
        verifyPageInvoked()
      }

      "user enters 'LIC99' as code" in {
        val formData = Json.toJson(AdditionalInformation("LIC99", "description"))
        val result = controller.submitForm(itemId)(postRequest(formData))

        status(result) mustBe BAD_REQUEST
        verifyPageInvoked()
      }

      "user enters duplicated item" in {
        val item = anItem(withItemId("itemId"), withAdditionalInformation(additionalInformation))
        withNewCaching(aDeclaration(withItems(item)))

        val formData = Json.toJson(additionalInformation)
        val result = controller.submitForm(itemId)(postRequest(formData))

        status(result) mustBe BAD_REQUEST
        verifyPageInvoked()
      }

      "user reaches maximum amount of items" in {
        val items = Seq.fill(maxNumberOfItems)(additionalInformation)
        val modifier = withAdditionalInformationData(AdditionalInformationData(items))
        withNewCaching(aDeclaration(withItems(anItem(withItemId("itemId"), modifier))))

        val formData = Json.toJson(AdditionalInformation("54321", "New data"))
        val result = controller.submitForm(itemId)(postRequest(formData))

        status(result) mustBe BAD_REQUEST
        verifyPageInvoked()
      }
    }

    "return 303 (SEE_OTHER)" when {
      "user correctly adds new item" in {
        val modifier = withAdditionalInformationData(AdditionalInformationData(Yes, Seq.empty))
        withNewCaching(aDeclaration(withItems(anItem(withItemId("itemId"), modifier))))

        val correctForm = Json.toJson(additionalInformation)
        val result = controller.submitForm(itemId)(postRequest(correctForm))

        await(result) mustBe aRedirectToTheNextPage
        thePageNavigatedTo mustBe AdditionalInformationController.displayPage(itemId)
        verifyPageInvoked(0)

        val savedDocuments = theCacheModelUpdated.itemBy(itemId).flatMap(_.additionalInformation)
        savedDocuments mustBe Some(AdditionalInformationData(Seq(additionalInformation)))
      }
    }
  }

  "AdditionalItems isRequired is changed to true if currently set to false" when {
    "user adds an item" in {
      val modifier = withAdditionalInformationData(AdditionalInformationData(No, Seq.empty))
      withNewCaching(aDeclaration(withItems(anItem(withItemId("itemId"), modifier))))

      val correctForm = Json.toJson(additionalInformation)
      val result = controller.submitForm(itemId)(postRequest(correctForm))

      await(result) mustBe aRedirectToTheNextPage
      thePageNavigatedTo mustBe AdditionalInformationController.displayPage(itemId)
      verifyPageInvoked(0)

      val isRequired = theCacheModelUpdated.itemBy(itemId).flatMap(_.additionalInformation.flatMap(_.isRequired))
      isRequired mustBe Yes
    }
  }
}
