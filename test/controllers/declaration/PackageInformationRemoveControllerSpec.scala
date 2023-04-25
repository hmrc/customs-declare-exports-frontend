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

package controllers.declaration

import base.ControllerSpec
import forms.common.YesNoAnswer
import forms.declaration.PackageInformation
import mock.ErrorHandlerMocks
import models.declaration.ExportDeclarationTestData.declarationMeta
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito._
import org.scalatest.{GivenWhenThen, OptionValues}
import play.api.data.Form
import play.api.mvc.{AnyContentAsEmpty, Request}
import play.api.test.Helpers._
import play.twirl.api.HtmlFormat
import views.html.declaration.packageInformation.package_information_remove

class PackageInformationRemoveControllerSpec extends ControllerSpec with OptionValues with ErrorHandlerMocks with GivenWhenThen {

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
    await(controller.displayPage(item.id, id1)(request))
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

  private val id1 = "pkgId1"
  private val packageInformation1 = PackageInformation(1, id1, Some("AB"), Some(1), Some("SHIP"))
  private val item = anItem(withPackageInformation(packageInformation1))

  "PackageInformation Remove Controller" must {

    onEveryDeclarationJourney() { request =>
      "return 200 (OK)" that {
        "display page method is invoked with existing package info id" in {
          withNewCaching(aDeclarationAfter(request.cacheModel, withItems(item)))

          val result = controller.displayPage(item.id, id1)(getRequest())

          status(result) mustBe OK
          verifyRemovePageInvoked()

          thePackageInformation mustBe packageInformation1
        }

      }

      "return 400 (BAD_REQUEST)" when {

        "user submits an invalid answer" in {
          withNewCaching(aDeclarationAfter(request.cacheModel, withItems(item)))

          val requestBody = Seq("yesNo" -> "invalid")
          val result = controller.submitForm(item.id, id1)(postRequestAsFormUrlEncoded(requestBody: _*))

          status(result) mustBe BAD_REQUEST
          verifyRemovePageInvoked()
        }

        "user tries to display page with non-existent package info" in {
          withNewCaching(aDeclarationAfter(request.cacheModel))

          val result = controller.displayPage(item.id, id1)(getRequest())

          status(result) mustBe BAD_REQUEST
          verifyNoInteractions(mockRemovePage)
          verify(mockErrorHandler).redirectToErrorPage(any())
        }

        "user tries to remove non-existent package info" in {
          withNewCaching(aDeclarationAfter(request.cacheModel))

          val result = controller.submitForm(item.id, id1)(getRequest())

          status(result) mustBe BAD_REQUEST
          verifyNoInteractions(mockRemovePage)
          verify(mockErrorHandler).redirectToErrorPage(any())
        }
      }

      "return 303 (SEE_OTHER)" when {

        "user submits 'Yes' answer" in {
          val id2 = "pkgId2"
          val packageInformation2 = PackageInformation(2, id2, Some("AE"), Some(1), Some("SHIP"))
          val item = anItem(withPackageInformation(packageInformation1, packageInformation2))
          val meta = declarationMeta.copy(maxSequenceIds = declarationMeta.maxSequenceIds + (PackageInformation.seqIdKey -> 2))
          withNewCaching(aDeclarationAfter(request.cacheModel, withItems(item)).copy(declarationMeta = meta))

          val requestBody = Seq("yesNo" -> "Yes")
          val result = controller.submitForm(item.id, id1)(postRequestAsFormUrlEncoded(requestBody: _*))

          await(result) mustBe aRedirectToTheNextPage
          thePageNavigatedTo mustBe routes.PackageInformationSummaryController.displayPage(item.id)

          And("max seq Id remains the same in dec meta")
          val declaration = theCacheModelUpdated
          declaration.declarationMeta.maxSequenceIds.get(PackageInformation.seqIdKey).value mustBe 2

          val packageInfos = declaration.itemBy(item.id).flatMap(_.packageInformation).value
          packageInfos.size mustBe 1
          packageInfos.head.sequenceId mustBe 2
          packageInfos.head.id mustBe id2
        }

        "user submits 'No' answer" in {
          withNewCaching(aDeclarationAfter(request.cacheModel, withItems(item)))

          val requestBody = Seq("yesNo" -> "No")
          val result = controller.submitForm(item.id, id1)(postRequestAsFormUrlEncoded(requestBody: _*))

          await(result) mustBe aRedirectToTheNextPage
          thePageNavigatedTo mustBe routes.PackageInformationSummaryController.displayPage(item.id)

          verifyTheCacheIsUnchanged()
        }
      }
    }
  }
}
