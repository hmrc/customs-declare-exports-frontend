/*
 * Copyright 2020 HM Revenue & Customs
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

import controllers.declaration.AdditionalFiscalReferencesRemoveController
import forms.common.YesNoAnswer
import forms.declaration.{AdditionalFiscalReference, AdditionalFiscalReferencesData}
import models.Mode
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, times, verify, when}
import org.scalatest.OptionValues
import play.api.data.Form
import play.api.mvc.{AnyContentAsEmpty, Request}
import play.api.test.Helpers._
import play.twirl.api.HtmlFormat
import unit.base.ControllerSpec
import views.html.declaration.fiscalInformation.additional_fiscal_references_remove

class AdditionalFiscalReferencesRemoveControllerSpec extends ControllerSpec with OptionValues {

  val mockRemovePage = mock[additional_fiscal_references_remove]

  val controller =
    new AdditionalFiscalReferencesRemoveController(
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
    withNewCaching(aDeclaration())
    await(controller.displayPage(Mode.Normal, item.id, referenceId)(request))
    theResponseForm
  }

  def theResponseForm: Form[YesNoAnswer] = {
    val captor = ArgumentCaptor.forClass(classOf[Form[YesNoAnswer]])
    verify(mockRemovePage).apply(any(), any(), any(), any(), captor.capture())(any(), any())
    captor.getValue
  }

  def theAdditionalReference: AdditionalFiscalReference = {
    val captor = ArgumentCaptor.forClass(classOf[AdditionalFiscalReference])
    verify(mockRemovePage).apply(any(), any(), any(), captor.capture(), any())(any(), any())
    captor.getValue
  }

  private def verifyRemovePageInvoked(numberOfTimes: Int = 1) =
    verify(mockRemovePage, times(numberOfTimes)).apply(any(), any(), any(), any(), any())(any(), any())

  val additionalReference = AdditionalFiscalReference("FR", "12345")
  val referenceId = "0.200378103"
  val item = anItem()
  
  "AdditionalFiscalReferences Remove Controller" must {

    onEveryDeclarationJourney() { request =>
      "return 200 (OK)" that {
        "display page method is invoked" in {

          withNewCaching(request.cacheModel)

          val result = controller.displayPage(Mode.Normal, item.id, referenceId)(getRequest())

          status(result) mustBe OK
          verifyRemovePageInvoked()

          theAdditionalReference mustBe additionalReference
        }

      }

      "return 400 (BAD_REQUEST)" when {
        "user submits an invalid answer" in {
          withNewCaching(request.cacheModel)

          val requestBody = Seq("yesNo" -> "invalid")
          val result = controller.submitForm(Mode.Normal, item.id, referenceId)(postRequestAsFormUrlEncoded(requestBody: _*))

          status(result) mustBe BAD_REQUEST
          verifyRemovePageInvoked()
        }

      }
      "return 303 (SEE_OTHER)" when {

        "requested reference id invalid" in {

          withNewCaching(request.cacheModel)

          val result = controller.displayPage(Mode.Normal, item.id, "ref-id")(getRequest())

          await(result) mustBe aRedirectToTheNextPage
          thePageNavigatedTo mustBe controllers.declaration.routes.AdditionalFiscalReferencesController.displayPage(Mode.Normal, item.id)
        }

        "user submits 'Yes' answer" in {
          withNewCaching(request.cacheModel)

          val requestBody = Seq("yesNo" -> "Yes")
          val result = controller.submitForm(Mode.Normal, item.id, referenceId)(postRequestAsFormUrlEncoded(requestBody: _*))

          await(result) mustBe aRedirectToTheNextPage
          thePageNavigatedTo mustBe controllers.declaration.routes.AdditionalFiscalReferencesController.displayPage(Mode.Normal, item.id)

          theCacheModelUpdated.itemBy(item.id).flatMap(_.additionalFiscalReferencesData) mustBe Some(AdditionalFiscalReferencesData(Seq.empty))
        }

        "user submits 'No' answer" in {
          withNewCaching(request.cacheModel)

          val requestBody = Seq("yesNo" -> "No")
          val result = controller.submitForm(Mode.Normal, item.id, referenceId)(postRequestAsFormUrlEncoded(requestBody: _*))

          await(result) mustBe aRedirectToTheNextPage
          thePageNavigatedTo mustBe controllers.declaration.routes.AdditionalFiscalReferencesController.displayPage(Mode.Normal, item.id)

          verifyTheCacheIsUnchanged()
        }
      }
    }

  }
}
