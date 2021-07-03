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
import forms.common.YesNoAnswer
import forms.declaration.AdditionalInformation
import models.Mode
import models.declaration.AdditionalInformationData
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, times, verify, when}
import org.scalatest.OptionValues
import play.api.data.Form
import play.api.mvc.{AnyContentAsEmpty, Request}
import play.api.test.Helpers._
import play.twirl.api.HtmlFormat
import utils.ListItem
import views.html.declaration.additionalInformation.additional_information_remove

class AdditionalInformationRemoveControllerSpec extends ControllerSpec with OptionValues {

  val mockRemovePage = mock[additional_information_remove]

  val controller =
    new AdditionalInformationRemoveController(
      mockAuthAction,
      mockJourneyAction,
      mockExportsCacheService,
      navigator,
      stubMessagesControllerComponents(),
      mockRemovePage
    )(ec)

  override protected def beforeEach(): Unit = {
    super.beforeEach()
    authorizedUser()
    when(mockRemovePage.apply(any(), any(), any(), any(), any())(any(), any())).thenReturn(HtmlFormat.empty)
  }

  override protected def afterEach(): Unit = {
    reset(mockRemovePage)
    super.afterEach()
  }

  override def getFormForDisplayRequest(request: Request[AnyContentAsEmpty.type]): Form[_] = {
    withNewCaching(aDeclaration(withItems(itemWithTwoAdditionalInformation)))
    await(controller.displayPage(Mode.Normal, itemId, additionalInformationId)(request))
    theResponseForm
  }

  def theResponseForm: Form[YesNoAnswer] = {
    val captor = ArgumentCaptor.forClass(classOf[Form[YesNoAnswer]])
    verify(mockRemovePage).apply(any(), any(), any(), any(), captor.capture())(any(), any())
    captor.getValue
  }

  def theAdditionalInformation: AdditionalInformation = {
    val captor = ArgumentCaptor.forClass(classOf[AdditionalInformation])
    verify(mockRemovePage).apply(any(), any(), any(), captor.capture(), any())(any(), any())
    captor.getValue
  }

  private def verifyRemovePageInvoked(numberOfTimes: Int = 1) =
    verify(mockRemovePage, times(numberOfTimes)).apply(any(), any(), any(), any(), any())(any(), any())

  private val additionalInformation = AdditionalInformation("00400", "Some description")
  private val additionalInformationOther = AdditionalInformation("00401", "Some description")
  private val additionalInformationId = ListItem.createId(0, additionalInformation)
  val itemId = "itemId"
  val itemWithTwoAdditionalInformation =
    anItem(withItemId(itemId), withAdditionalInformation(additionalInformation, additionalInformationOther))
  val itemWithSingleAdditionalInformation =
    anItem(withItemId(itemId), withAdditionalInformation(additionalInformation))

  "AdditionalInformation Remove Controller" must {

    onEveryDeclarationJourney() { request =>
      "return 200 (OK)" that {
        "display page method is invoked" in {

          withNewCaching(aDeclarationAfter(request.cacheModel, withItem(itemWithTwoAdditionalInformation)))

          val result = controller.displayPage(Mode.Normal, itemId, additionalInformationId)(getRequest())

          status(result) mustBe OK
          verifyRemovePageInvoked()

          theAdditionalInformation mustBe additionalInformation
        }

      }

      "return 400 (BAD_REQUEST)" when {
        "user submits an invalid answer" in {
          withNewCaching(aDeclarationAfter(request.cacheModel, withItem(itemWithTwoAdditionalInformation)))

          val requestBody = Seq("yesNo" -> "invalid")
          val result = controller.submitForm(Mode.Normal, itemId, additionalInformationId)(postRequestAsFormUrlEncoded(requestBody: _*))

          status(result) mustBe BAD_REQUEST
          verifyRemovePageInvoked()
        }

      }
      "return 303 (SEE_OTHER)" when {

        "requested document id invalid" in {

          withNewCaching(aDeclarationAfter(request.cacheModel, withItem(itemWithTwoAdditionalInformation)))

          val result = controller.displayPage(Mode.Normal, itemId, "some-id")(getRequest())

          await(result) mustBe aRedirectToTheNextPage
          thePageNavigatedTo mustBe controllers.declaration.routes.AdditionalInformationController.displayPage(Mode.Normal, itemId)
        }

        "user submits 'Yes' answer when multiple additional information exists" in {
          withNewCaching(aDeclarationAfter(request.cacheModel, withItem(itemWithTwoAdditionalInformation)))

          val requestBody = Seq("yesNo" -> "Yes")
          val result = controller.submitForm(Mode.Normal, itemId, additionalInformationId)(postRequestAsFormUrlEncoded(requestBody: _*))

          await(result) mustBe aRedirectToTheNextPage
          thePageNavigatedTo mustBe controllers.declaration.routes.AdditionalInformationController.displayPage(Mode.Normal, itemId)

          theCacheModelUpdated.itemBy(itemId).flatMap(_.additionalInformation) mustBe Some(AdditionalInformationData(Seq(additionalInformationOther)))
        }

        "user submits 'Yes' answer when single additional information exists" in {
          withNewCaching(aDeclarationAfter(request.cacheModel, withItem(itemWithSingleAdditionalInformation)))

          val requestBody = Seq("yesNo" -> "Yes")
          val result = controller.submitForm(Mode.Normal, itemId, additionalInformationId)(postRequestAsFormUrlEncoded(requestBody: _*))

          await(result) mustBe aRedirectToTheNextPage
          thePageNavigatedTo mustBe controllers.declaration.routes.AdditionalInformationRequiredController.displayPage(Mode.Normal, itemId)

          theCacheModelUpdated.itemBy(itemId).flatMap(_.additionalInformation) mustBe Some(
            AdditionalInformationData(Some(YesNoAnswer.Yes), Seq.empty)
          )
        }

        "user submits 'No' answer" in {
          withNewCaching(aDeclarationAfter(request.cacheModel, withItem(itemWithTwoAdditionalInformation)))

          val requestBody = Seq("yesNo" -> "No")
          val result = controller.submitForm(Mode.Normal, itemId, additionalInformationId)(postRequestAsFormUrlEncoded(requestBody: _*))

          await(result) mustBe aRedirectToTheNextPage
          thePageNavigatedTo mustBe controllers.declaration.routes.AdditionalInformationController.displayPage(Mode.Normal, itemId)

          verifyTheCacheIsUnchanged()
        }
      }
    }

  }
}
