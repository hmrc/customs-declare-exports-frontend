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
import forms.declaration.PackageInformation
import mock.ErrorHandlerMocks
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito._
import org.scalatest.OptionValues
import play.api.data.Form
import play.api.mvc.{AnyContentAsEmpty, Request}
import play.api.test.Helpers._
import play.twirl.api.HtmlFormat
import views.html.declaration.packageInformation.package_information_remove

class PackageInformationRemoveControllerSpec extends ControllerSpec with OptionValues with ErrorHandlerMocks {

  val mockRemovePage = mock[package_information_remove]

  val controller =
    new PackageInformationRemoveController(
      mockAuthAction,
      mockJourneyAction,
      mockExportsCacheService,
      mockErrorHandler,
      navigator,
      stubMessagesControllerComponents(),
      mockRemovePage
    )(ec)

  override protected def beforeEach(): Unit = {
    super.beforeEach()
    authorizedUser()
    setupErrorHandler()
    when(mockRemovePage.apply(any(), any(), any())(any(), any())).thenReturn(HtmlFormat.empty)
  }

  override protected def afterEach(): Unit = {
    reset(mockRemovePage)
    super.afterEach()
  }

  override def getFormForDisplayRequest(request: Request[AnyContentAsEmpty.type]): Form[_] = {
    withNewCaching(aDeclaration(withItems(item)))
    await(controller.displayPage(item.id, id)(request))
    theResponseForm
  }

  def theResponseForm: Form[YesNoAnswer] = {
    val captor = ArgumentCaptor.forClass(classOf[Form[YesNoAnswer]])
    verify(mockRemovePage).apply(any(), any(), captor.capture())(any(), any())
    captor.getValue
  }

  def thePackageInformation: PackageInformation = {
    val captor = ArgumentCaptor.forClass(classOf[PackageInformation])
    verify(mockRemovePage).apply(any(), captor.capture(), any())(any(), any())
    captor.getValue
  }

  private def verifyRemovePageInvoked(numberOfTimes: Int = 1): HtmlFormat.Appendable =
    verify(mockRemovePage, times(numberOfTimes)).apply(any(), any(), any())(any(), any())

  val id = "pkgId"
  val packageInformation = PackageInformation(id, Some("AB"), Some(1), Some("SHIP"))
  val item = anItem(withPackageInformation(packageInformation))

  "PackageInformation Remove Controller" must {

    onEveryDeclarationJourney() { request =>
      "return 200 (OK)" that {
        "display page method is invoked with existing package info id" in {
          withNewCaching(aDeclarationAfter(request.cacheModel, withItems(item)))

          val result = controller.displayPage(item.id, id)(getRequest())

          status(result) mustBe OK
          verifyRemovePageInvoked()

          thePackageInformation mustBe packageInformation
        }

      }

      "return 400 (BAD_REQUEST)" when {

        "user submits an invalid answer" in {
          withNewCaching(aDeclarationAfter(request.cacheModel, withItems(item)))

          val requestBody = Seq("yesNo" -> "invalid")
          val result = controller.submitForm(item.id, id)(postRequestAsFormUrlEncoded(requestBody: _*))

          status(result) mustBe BAD_REQUEST
          verifyRemovePageInvoked()
        }

        "user tries to display page with non-existent package info" in {
          withNewCaching(aDeclarationAfter(request.cacheModel))

          val result = controller.displayPage(item.id, id)(getRequest())

          status(result) mustBe BAD_REQUEST
          verifyNoInteractions(mockRemovePage)
          verify(mockErrorHandler).displayErrorPage(any())
        }

        "user tries to remove non-existent package info" in {
          withNewCaching(aDeclarationAfter(request.cacheModel))

          val result = controller.submitForm(item.id, id)(getRequest())

          status(result) mustBe BAD_REQUEST
          verifyNoInteractions(mockRemovePage)
          verify(mockErrorHandler).displayErrorPage(any())
        }
      }

      "return 303 (SEE_OTHER)" when {

        "user submits 'Yes' answer" in {
          withNewCaching(aDeclarationAfter(request.cacheModel, withItems(item)))

          val requestBody = Seq("yesNo" -> "Yes")
          val result = controller.submitForm(item.id, id)(postRequestAsFormUrlEncoded(requestBody: _*))

          await(result) mustBe aRedirectToTheNextPage
          thePageNavigatedTo mustBe routes.PackageInformationSummaryController.displayPage(item.id)

          theCacheModelUpdated.itemBy(item.id).flatMap(_.packageInformation) mustBe Some(Seq.empty)
        }

        "user submits 'No' answer" in {
          withNewCaching(aDeclarationAfter(request.cacheModel, withItems(item)))

          val requestBody = Seq("yesNo" -> "No")
          val result = controller.submitForm(item.id, id)(postRequestAsFormUrlEncoded(requestBody: _*))

          await(result) mustBe aRedirectToTheNextPage
          thePageNavigatedTo mustBe routes.PackageInformationSummaryController.displayPage(item.id)

          verifyTheCacheIsUnchanged()
        }
      }
    }
  }
}
