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
import models.DeclarationType._
import models.Mode
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, times, verify, when}
import org.scalatest.OptionValues
import play.api.data.Form
import play.api.libs.json.Json
import play.api.mvc.{AnyContentAsEmpty, Request}
import play.api.test.Helpers._
import play.twirl.api.HtmlFormat
import views.declaration.PackageInformationViewSpec.packageInformation
import views.html.declaration.package_information

class PackageInformationSummaryControllerSpec extends ControllerSpec with OptionValues {

  val mockPage = mock[package_information]

  val controller = new PackageInformationSummaryController(
    mockAuthAction,
    mockJourneyAction,
    mockExportsCacheService,
    navigator,
    stubMessagesControllerComponents(),
    mockPage
  )

  override protected def beforeEach(): Unit = {
    super.beforeEach()
    authorizedUser()
    when(mockPage.apply(any(), any(), any(), any())(any(), any())).thenReturn(HtmlFormat.empty)
  }

  override protected def afterEach(): Unit = {
    reset(mockPage)
    super.afterEach()
  }

  override def getFormForDisplayRequest(request: Request[AnyContentAsEmpty.type]): Form[_] = {
    val item = anItem(withPackageInformation(packageInformation))
    withNewCaching(aDeclaration(withItems(item)))
    await(controller.displayPage(Mode.Normal, item.id)(request))
    theResponseForm
  }

  def theResponseForm: Form[YesNoAnswer] = {
    val captor = ArgumentCaptor.forClass(classOf[Form[YesNoAnswer]])
    verify(mockPage).apply(any(), any(), captor.capture(), any())(any(), any())
    captor.getValue
  }

  def thePackageInformationList: Seq[PackageInformation] = {
    val captor = ArgumentCaptor.forClass(classOf[Seq[PackageInformation]])
    verify(mockPage).apply(any(), any(), any(), captor.capture())(any(), any())
    captor.getValue
  }

  private def verifyPageInvoked(numberOfTimes: Int = 1) = verify(mockPage, times(numberOfTimes)).apply(any(), any(), any(), any())(any(), any())

  "PackageInformation Summary Controller" should {

    onEveryDeclarationJourney() { request =>
      "return 200 (OK)" that {
        "display page method is invoked and cache contains data" in {
          val item = anItem(withPackageInformation(packageInformation))
          withNewCaching(aDeclarationAfter(request.cacheModel, withItems(item)))

          val result = controller.displayPage(Mode.Normal, item.id)(getRequest())

          status(result) mustBe OK
          verifyPageInvoked()

          thePackageInformationList must be(Seq(packageInformation))
        }
      }

      "return 400 (BAD_REQUEST)" when {

        "user submits invalid answer" in {
          val item = anItem(withPackageInformation(packageInformation))
          withNewCaching(aDeclarationAfter(request.cacheModel, withItems(item)))

          val requestBody = Json.obj("yesNo" -> "invalid")
          val result = controller.submitForm(Mode.Normal, item.id)(postRequest(requestBody))

          status(result) mustBe BAD_REQUEST
          verifyPageInvoked()
        }

      }

      "return 303 (SEE_OTHER)" when {

        "there is no package information in the cache" in {
          val item = anItem()
          withNewCaching(aDeclarationAfter(request.cacheModel, withItems(item)))

          val result = controller.displayPage(Mode.Normal, item.id)(getRequest())

          await(result) mustBe aRedirectToTheNextPage
          thePageNavigatedTo mustBe controllers.declaration.routes.PackageInformationAddController.displayPage(Mode.Normal, item.id)
        }

        "user submits valid Yes answer" in {
          val item = anItem(withPackageInformation(packageInformation))
          withNewCaching(aDeclarationAfter(request.cacheModel, withItems(item)))

          val requestBody = Json.obj("yesNo" -> "Yes")
          val result = controller.submitForm(Mode.Normal, item.id)(postRequest(requestBody))

          await(result) mustBe aRedirectToTheNextPage
          thePageNavigatedTo mustBe controllers.declaration.routes.PackageInformationAddController.displayPage(Mode.Normal, item.id)
        }

        "user submits valid Yes answer in error-fix mode" in {
          val item = anItem(withPackageInformation(packageInformation))
          withNewCaching(aDeclarationAfter(request.cacheModel, withItems(item)))

          val requestBody = Json.obj("yesNo" -> "Yes")
          val result = controller.submitForm(Mode.ErrorFix, item.id)(postRequest(requestBody))

          await(result) mustBe aRedirectToTheNextPage
          thePageNavigatedTo mustBe controllers.declaration.routes.PackageInformationAddController.displayPage(Mode.ErrorFix, item.id)
        }
      }
    }

    "re-direct to next question" when {
      onJourney(STANDARD, SUPPLEMENTARY, CLEARANCE) { request =>
        "user submits valid No answer" in {
          val item = anItem(withPackageInformation(packageInformation))
          withNewCaching(aDeclarationAfter(request.cacheModel, withItems(item)))

          val requestBody = Json.obj("yesNo" -> "No")
          val result = controller.submitForm(Mode.Normal, item.id)(postRequest(requestBody))

          await(result) mustBe aRedirectToTheNextPage
          thePageNavigatedTo mustBe controllers.declaration.routes.CommodityMeasureController.displayPage(Mode.Normal, item.id)
        }
      }

      onJourney(SIMPLIFIED, OCCASIONAL) { request =>
        "user submits valid No answer" in {
          val item = anItem(withPackageInformation(packageInformation))
          withNewCaching(aDeclarationAfter(request.cacheModel, withItems(item)))

          val requestBody = Json.obj("yesNo" -> "No")
          val result = controller.submitForm(Mode.Normal, item.id)(postRequest(requestBody))

          await(result) mustBe aRedirectToTheNextPage
          thePageNavigatedTo mustBe controllers.declaration.routes.AdditionalInformationRequiredController.displayPage(Mode.Normal, item.id)
        }
      }
    }

  }
}
