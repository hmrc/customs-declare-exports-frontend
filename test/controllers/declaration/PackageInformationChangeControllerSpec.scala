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

import base.{AuditedControllerSpec, ControllerSpec, Injector}
import controllers.helpers.SequenceIdHelper.valueOfEso
import forms.declaration.PackageInformation
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito._
import org.scalatest.OptionValues
import play.api.data.Form
import play.api.mvc.{AnyContentAsEmpty, Request}
import play.api.test.Helpers._
import play.twirl.api.HtmlFormat
import services.PackageTypesService
import views.html.declaration.packageInformation.package_information_change

class PackageInformationChangeControllerSpec extends ControllerSpec with AuditedControllerSpec with OptionValues with Injector {

  val mockChangePage = mock[package_information_change]
  val mockPackageTypesService = instanceOf[PackageTypesService]

  val controller =
    new PackageInformationChangeController(
      mockAuthAction,
      mockJourneyAction,
      navigator,
      mockExportsCacheService,
      mockErrorHandler,
      mcc,
      mockChangePage
    )(ec, mockPackageTypesService, auditService)

  override protected def beforeEach(): Unit = {
    super.beforeEach()
    authorizedUser()
    setupErrorHandler()
    when(mockChangePage.apply(any(), any(), any())(any(), any())).thenReturn(HtmlFormat.empty)
  }

  override protected def afterEach(): Unit = {
    reset(mockChangePage)
    super.afterEach()
  }

  override def getFormForDisplayRequest(request: Request[AnyContentAsEmpty.type]): Form[_] = {
    withNewCaching(aDeclaration(withItems(item)))
    await(controller.displayPage(item.id, id)(request))
    thePackageInformation
  }

  def thePackageInformation: Form[PackageInformation] = {
    val captor = ArgumentCaptor.forClass(classOf[Form[PackageInformation]])
    verify(mockChangePage).apply(any(), captor.capture(), any())(any(), any())
    captor.getValue
  }

  private def verifyChangePageInvoked(numberOfTimes: Int = 1): HtmlFormat.Appendable =
    verify(mockChangePage, times(numberOfTimes)).apply(any(), any(), any())(any(), any())

  val id = "pkgId"
  val packageInformation = PackageInformation(3, id, Some("AB"), Some(1), Some("SHIP"))
  val item = anItem(withPackageInformation(packageInformation))
  val anotherId = "differentId"

  "PackageInformation Change Controller" must {
    onEveryDeclarationJourney() { request =>
      "return 200 (OK)" that {
        "display page method is invoked" in {
          val item = anItem(withPackageInformation(packageInformation))
          withNewCaching(aDeclarationAfter(request.cacheModel, withItems(item)))

          val result = controller.displayPage(item.id, id)(getRequest())

          status(result) mustBe OK
          verifyChangePageInvoked()

          thePackageInformation.value mustBe Some(packageInformation)
        }
      }

      "return 400 (BAD_REQUEST)" when {

        "user makes invalid changes to data" in {
          withNewCaching(aDeclarationAfter(request.cacheModel, withItems(item)))

          val requestBody = Seq("typesOfPackages" -> "invalid", "numberOfPackages" -> "invalid", "shippingMarks" -> "inva!id")
          val result = controller.submitForm(item.id, id)(postRequestAsFormUrlEncoded(requestBody: _*))

          status(result) mustBe BAD_REQUEST
          verifyChangePageInvoked()
          verifyNoAudit()
        }

        "user makes changes resulting in duplicate data" in {
          val morePackageInformation = PackageInformation(1, anotherId, None, None, None)
          val item = anItem(withPackageInformation(packageInformation, morePackageInformation))
          withNewCaching(aDeclarationAfter(request.cacheModel, withItems(item)))

          val requestBody = Seq(
            "typesOfPackages" -> packageInformation.typesOfPackages.get,
            "numberOfPackages" -> packageInformation.numberOfPackages.get.toString,
            "shippingMarks" -> packageInformation.shippingMarks.get
          )
          val result = controller.submitForm(item.id, anotherId)(postRequestAsFormUrlEncoded(requestBody: _*))

          status(result) mustBe BAD_REQUEST
          verifyChangePageInvoked()
          verifyNoAudit()
        }

        "user tries to display page with non-existent package info" in {
          withNewCaching(aDeclarationAfter(request.cacheModel))

          val result = controller.displayPage(item.id, id)(getRequest())

          status(result) mustBe BAD_REQUEST
          verifyNoInteractions(mockChangePage)
          verify(mockErrorHandler).redirectToErrorPage(any())
          verifyNoAudit()
        }

        "user tries to remove non-existent package info" in {
          withNewCaching(aDeclarationAfter(request.cacheModel))

          val result = controller.submitForm(item.id, id)(getRequest())

          status(result) mustBe BAD_REQUEST
          verify(mockErrorHandler).redirectToErrorPage(any())
          verifyNoAudit()
        }
      }

      "return 303 (SEE_OTHER)" when {
        "user submits valid data" in {
          withNewCaching(aDeclarationAfter(request.cacheModel, withItems(item)))

          val requestBody = Seq("typesOfPackages" -> "AE", "numberOfPackages" -> "1", "shippingMarks" -> "1234")
          val result = controller.submitForm(item.id, id)(postRequestAsFormUrlEncoded(requestBody: _*))

          await(result) mustBe aRedirectToTheNextPage
          thePageNavigatedTo mustBe routes.PackageInformationSummaryController.displayPage(item.id)

          val declaration = theCacheModelUpdated
          val savedPackage = declaration.itemBy(item.id).flatMap(_.packageInformation).value.head
          savedPackage.typesOfPackages mustBe Some("AE")
          savedPackage.numberOfPackages mustBe Some(1)
          savedPackage.shippingMarks mustBe Some("1234")

          valueOfEso[PackageInformation](declaration).value mustBe 1
          verifyAudit()
        }
      }
    }
  }
}
