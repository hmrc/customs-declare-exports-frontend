/*
 * Copyright 2024 HM Revenue & Customs
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
import controllers.helpers.SequenceIdHelper.valueOfEso
import controllers.section5.routes.PackageInformationSummaryController
import forms.common.YesNoAnswer
import forms.section5.PackageInformation
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito._
import org.scalatest.{GivenWhenThen, OptionValues}
import play.api.data.Form
import play.api.libs.json.Json
import play.api.mvc.{AnyContentAsEmpty, Request}
import play.api.test.Helpers._
import play.twirl.api.HtmlFormat
import views.html.section5.packageInformation.package_information_remove

class PackageInformationRemoveControllerSpec extends ControllerSpec with AuditedControllerSpec with GivenWhenThen with OptionValues {

  val mockRemovePage = mock[package_information_remove]

  val controller =
    new PackageInformationRemoveController(mockAuthAction, mockJourneyAction, mockExportsCacheService, navigator, mcc, mockRemovePage)(
      ec,
      auditService
    )

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
      "return 200 (OK)" when {
        "display page method is invoked with existing package info id" in {
          withNewCaching(aDeclarationAfter(request.cacheModel, withItems(item)))

          val result = controller.displayPage(item.id, id1)(getRequest())

          status(result) mustBe OK
          verifyRemovePageInvoked()

          thePackageInformation mustBe packageInformation1
        }
      }

      "redirect to /packages-list" when {

        "display page method is invoked with invalid package id" in {
          withNewCaching(aDeclarationAfter(request.cacheModel, withItems(item)))

          val result = controller.displayPage(item.id, "invalid")(getRequest())

          status(result) mustBe SEE_OTHER
          redirectLocation(result) mustBe Some(PackageInformationSummaryController.displayPage(item.id).url)

          verifyNoInteractions(mockRemovePage)
        }

        "user submits a correct answer but with invalid package id" in {
          withNewCaching(aDeclarationAfter(request.cacheModel, withItems(item)))

          val requestBody = Json.obj("yesNo" -> "Yes")
          val result = controller.submitForm(item.id, "non-existent")(postRequest(requestBody))

          status(result) mustBe SEE_OTHER
          redirectLocation(result) mustBe Some(PackageInformationSummaryController.displayPage(item.id).url)

          verifyNoInteractions(mockRemovePage)
          verifyNoAudit()
        }
      }

      "return 303 (SEE_OTHER)" when {

        "user submits 'Yes' answer" in {
          val id2 = "pkgId2"
          val packageInformation2 =
            PackageInformation(id = id2, typesOfPackages = Some("AE"), numberOfPackages = Some(1), shippingMarks = Some("SHIP"))
          val item = anItem(withPackageInformation(packageInformation1, packageInformation2))

          withNewCaching(aDeclarationAfter(request.cacheModel, withItems(item)))

          val requestBody = Seq("yesNo" -> "Yes")
          val result = controller.submitForm(item.id, id1)(postRequestAsFormUrlEncoded(requestBody: _*))

          await(result) mustBe aRedirectToTheNextPage
          thePageNavigatedTo mustBe PackageInformationSummaryController.displayPage(item.id)

          val declaration = theCacheModelUpdated
          val packageInfos = declaration.itemBy(item.id).flatMap(_.packageInformation).value
          packageInfos.size mustBe 1
          packageInfos.head.id mustBe id2

          And("max seq Id remains the same in dec meta")
          valueOfEso[PackageInformation](declaration).value mustBe 1
          verifyAudit()
        }

        "user submits 'No' answer" in {
          withNewCaching(aDeclarationAfter(request.cacheModel, withItems(item)))

          val requestBody = Seq("yesNo" -> "No")
          val result = controller.submitForm(item.id, id1)(postRequestAsFormUrlEncoded(requestBody: _*))

          await(result) mustBe aRedirectToTheNextPage
          thePageNavigatedTo mustBe PackageInformationSummaryController.displayPage(item.id)

          verifyTheCacheIsUnchanged()
          verifyNoAudit()
        }
      }
    }
  }
}
