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
import forms.declaration.PackageInformation
import models.Mode
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, times, verify, when}
import org.scalatest.OptionValues
import play.api.data.Form
import play.api.mvc.{AnyContentAsEmpty, Request}
import play.api.test.Helpers._
import play.twirl.api.HtmlFormat
import views.declaration.PackageInformationViewSpec.packageInformation
import views.html.declaration.package_information_add

class PackageInformationAddControllerSpec extends ControllerSpec with OptionValues {

  val mockAddPage = mock[package_information_add]

  val controller =
    new PackageInformationAddController(
      mockAuthAction,
      mockJourneyAction,
      mockExportsCacheService,
      navigator,
      stubMessagesControllerComponents(),
      mockAddPage
    )(ec)

  override protected def beforeEach(): Unit = {
    super.beforeEach()
    authorizedUser()
    when(mockAddPage.apply(any(), any(), any(), any())(any(), any())).thenReturn(HtmlFormat.empty)
  }

  override protected def afterEach(): Unit = {
    reset(mockAddPage)
    super.afterEach()
  }

  override def getFormForDisplayRequest(request: Request[AnyContentAsEmpty.type]): Form[_] = {
    withNewCaching(aDeclaration())
    await(controller.displayPage(Mode.Normal, item.id)(request))
    thePackageInformation
  }

  def thePackageInformation: Form[PackageInformation] = {
    val captor = ArgumentCaptor.forClass(classOf[Form[PackageInformation]])
    verify(mockAddPage).apply(any(), any(), captor.capture(), any())(any(), any())
    captor.getValue
  }

  private def verifyAddPageInvoked(numberOfTimes: Int = 1) = verify(mockAddPage, times(numberOfTimes)).apply(any(), any(), any(), any())(any(), any())

  val item = anItem()

  "PackageInformation Add Controller" must {

    onEveryDeclarationJourney() { request =>
      "return 200 (OK)" that {
        "display page method is invoked" in {

          withNewCaching(request.cacheModel)

          val result = controller.displayPage(Mode.Normal, item.id)(getRequest())

          status(result) mustBe OK
          verifyAddPageInvoked()

          thePackageInformation.value mustBe empty
        }

      }

      "return 400 (BAD_REQUEST)" when {
        "user adds invalid data" in {
          withNewCaching(request.cacheModel)

          val requestBody = Seq("typesOfPackages" -> "invalid", "numberOfPackages" -> "invalid", "shippingMarks" -> "inva!id")
          val result = controller.submitForm(Mode.Normal, item.id)(postRequestAsFormUrlEncoded(requestBody: _*))

          status(result) mustBe BAD_REQUEST
          verifyAddPageInvoked()
        }

        "user adds duplicate data" in {
          val item = anItem(withPackageInformation(packageInformation))
          withNewCaching(aDeclarationAfter(request.cacheModel, withItems(item)))

          val requestBody = Seq(
            "typesOfPackages" -> packageInformation.typesOfPackages.get,
            "numberOfPackages" -> packageInformation.numberOfPackages.get.toString,
            "shippingMarks" -> packageInformation.shippingMarks.get
          )
          val result = controller.submitForm(Mode.Normal, item.id)(postRequestAsFormUrlEncoded(requestBody: _*))

          status(result) mustBe BAD_REQUEST
          verifyAddPageInvoked()
        }

        "user adds too many codes" in {
          val packages = List.fill(99)(packageInformation)
          val item = anItem(withPackageInformation(packages))
          withNewCaching(aDeclarationAfter(request.cacheModel, withItems(item)))

          val requestBody = Seq("typesOfPackages" -> "AE", "numberOfPackages" -> "1", "shippingMarks" -> "1234")
          val result = controller.submitForm(Mode.Normal, item.id)(postRequestAsFormUrlEncoded(requestBody: _*))

          status(result) mustBe BAD_REQUEST
          verifyAddPageInvoked()
        }
      }
      "return 303 (SEE_OTHER)" when {
        "user submits valid data" in {
          val item = anItem()
          withNewCaching(aDeclarationAfter(request.cacheModel, withItems(item)))

          val requestBody = Seq("typesOfPackages" -> "AE", "numberOfPackages" -> "1", "shippingMarks" -> "1234")
          val result = controller.submitForm(Mode.Normal, item.id)(postRequestAsFormUrlEncoded(requestBody: _*))

          await(result) mustBe aRedirectToTheNextPage
          thePageNavigatedTo mustBe controllers.declaration.routes.PackageInformationSummaryController.displayPage(Mode.Normal, item.id)

          val savedPackage = theCacheModelUpdated.itemBy(item.id).flatMap(_.packageInformation).map(_.head)
          savedPackage.flatMap(_.typesOfPackages) mustBe Some("AE")
          savedPackage.flatMap(_.numberOfPackages) mustBe Some(1)
          savedPackage.flatMap(_.shippingMarks) mustBe Some("1234")
        }

      }
    }

  }
}
