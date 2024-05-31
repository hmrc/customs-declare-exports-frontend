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
import forms.declaration.PackageInformation.typeId
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, times, verify, when}
import org.scalatest.{GivenWhenThen, OptionValues}
import play.api.data.Form
import play.api.libs.json.Json
import play.api.mvc.{AnyContentAsEmpty, Request}
import play.api.test.Helpers._
import play.twirl.api.HtmlFormat
import services.PackageTypesService
import views.declaration.PackageInformationViewSpec.packageInformation
import views.html.declaration.packageInformation.package_information_add

class PackageInformationAddControllerSpec extends ControllerSpec with AuditedControllerSpec with OptionValues with Injector with GivenWhenThen {

  val mockAddPage = mock[package_information_add]
  val mockPackageTypesService = instanceOf[PackageTypesService]

  val controller =
    new PackageInformationAddController(
      mockAuthAction,
      mockJourneyAction,
      mockExportsCacheService,
      navigator,
      mcc,
      mockAddPage
    )(ec, mockPackageTypesService, auditService)

  override protected def beforeEach(): Unit = {
    super.beforeEach()
    authorizedUser()
    when(mockAddPage.apply(any(), any())(any(), any())).thenReturn(HtmlFormat.empty)
  }

  override protected def afterEach(): Unit = {
    reset(mockAddPage)
    super.afterEach()
  }

  override def getFormForDisplayRequest(request: Request[AnyContentAsEmpty.type]): Form[_] = {
    withNewCaching(aDeclaration())
    await(controller.displayPage(item.id)(request))
    theResponseForm
  }

  def theResponseForm: Form[PackageInformation] = {
    val captor = ArgumentCaptor.forClass(classOf[Form[PackageInformation]])
    verify(mockAddPage).apply(any(), captor.capture())(any(), any())
    captor.getValue
  }

  private def verifyAddPageInvoked(numberOfTimes: Int = 1): HtmlFormat.Appendable =
    verify(mockAddPage, times(numberOfTimes)).apply(any(), any())(any(), any())

  val item = anItem()

  "PackageInformationAddController" must {

    "return 200 (OK)" that {
      "display page method is invoked" in {
        withNewCaching(aDeclaration(withItem(item)))

        val result = controller.displayPage(item.id)(getRequest())

        status(result) mustBe OK
        verifyAddPageInvoked()

        theResponseForm.value mustBe empty
      }
    }

    "return 400 (BAD_REQUEST)" when {

      "no value is entered" in {
        withNewCaching(aDeclaration(withItem(item)))

        val requestBody = Json.obj(fieldIdOnError(typeId) -> "", "numberOfPackages" -> "", "shippingMarks" -> "")
        val result = controller.submitForm(item.id)(postRequest(requestBody))

        status(result) mustBe BAD_REQUEST
        verifyNoAudit()

        val errors = theResponseForm.errors
        errors(0).messages.head mustBe "declaration.packageInformation.typesOfPackages.empty"
        errors(1).messages.head mustBe "declaration.packageInformation.numberOfPackages.error"
        errors(2).messages.head mustBe "declaration.packageInformation.shippingMark.empty"
      }

      "user adds invalid data" in {
        withNewCaching(aDeclaration(withItem(item)))

        val requestBody = Json.obj(fieldIdOnError(typeId) -> "invalid", "numberOfPackages" -> "invalid", "shippingMarks" -> "$".repeat(43))
        val result = controller.submitForm(item.id)(postRequest(requestBody))

        status(result) mustBe BAD_REQUEST
        verifyNoAudit()

        val errors = theResponseForm.errors
        errors(0).messages.head mustBe "declaration.packageInformation.typesOfPackages.error"
        errors(1).messages.head mustBe "error.number"
        errors(2).messages.head mustBe "declaration.packageInformation.shippingMark.characterError"
        errors(3).messages.head mustBe "declaration.packageInformation.shippingMark.lengthError"
      }

      "user adds duplicate data" in {
        val item = anItem(withPackageInformation(packageInformation))
        withNewCaching(aDeclaration(withItems(item)))

        val requestBody = Json.obj(
          typeId -> packageInformation.typesOfPackages.get,
          "numberOfPackages" -> packageInformation.numberOfPackages.get.toString,
          "shippingMarks" -> packageInformation.shippingMarks.get
        )
        val result = controller.submitForm(item.id)(postRequest(requestBody))

        status(result) mustBe BAD_REQUEST
        verifyAddPageInvoked()
        verifyNoAudit()
      }

      "user adds too many codes" in {
        val packages = List.fill(99)(packageInformation)
        val item = anItem(withPackageInformation(packages))
        withNewCaching(aDeclaration(withItems(item)))

        val requestBody = Json.obj(typeId -> "AE", "numberOfPackages" -> "1", "shippingMarks" -> "1234")
        val result = controller.submitForm(item.id)(postRequest(requestBody))

        status(result) mustBe BAD_REQUEST
        verifyAddPageInvoked()
        verifyNoAudit()
      }
    }

    "return 303 (SEE_OTHER)" when {
      "user submits valid data" in {
        val item = anItem(withPackageInformation(List(packageInformation)))
        withNewCaching(aDeclaration(withItems(item)))

        val requestBody = Json.obj(typeId -> "AE", "numberOfPackages" -> "1", "shippingMarks" -> "1234")
        val result = controller.submitForm(item.id)(postRequest(requestBody))

        await(result) mustBe aRedirectToTheNextPage
        thePageNavigatedTo mustBe routes.PackageInformationSummaryController.displayPage(item.id)

        val declaration = theCacheModelUpdated
        val savedPackage = declaration.itemBy(item.id).flatMap(_.packageInformation).map(_.last)
        savedPackage.flatMap(_.typesOfPackages) mustBe Some("AE")
        savedPackage.flatMap(_.numberOfPackages) mustBe Some(1)
        savedPackage.flatMap(_.shippingMarks) mustBe Some("1234")

        And("max seq id is updated in dec meta")
        valueOfEso[PackageInformation](declaration).value mustBe 1
        verifyAudit()
      }
    }
  }
}
