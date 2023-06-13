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

import base.{ControllerSpec, Injector}
import controllers.helpers.SequenceIdHelper
import forms.declaration.PackageInformation
import models.DeclarationMeta
import models.declaration.EsoKeyProvider
import models.declaration.ExportDeclarationTestData.declarationMeta
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, times, verify, when}
import org.mockito.invocation.InvocationOnMock
import org.mockito.stubbing.Answer
import org.scalatest.{GivenWhenThen, OptionValues}
import play.api.data.Form
import play.api.mvc.{AnyContentAsEmpty, Request}
import play.api.test.Helpers._
import play.twirl.api.HtmlFormat
import services.PackageTypesService
import views.declaration.PackageInformationViewSpec.packageInformation
import views.html.declaration.packageInformation.package_information_add

class PackageInformationAddControllerSpec extends ControllerSpec with OptionValues with Injector with GivenWhenThen {

  val mockAddPage = mock[package_information_add]
  val mockPackageTypesService = instanceOf[PackageTypesService]
  private val mockSeqIdHandler = mock[SequenceIdHelper]

  val controller =
    new PackageInformationAddController(
      mockAuthAction,
      mockJourneyAction,
      mockExportsCacheService,
      navigator,
      stubMessagesControllerComponents(),
      mockAddPage,
      mockSeqIdHandler
    )(ec, mockPackageTypesService)

  override protected def beforeEach(): Unit = {
    super.beforeEach()
    authorizedUser()
    when(mockAddPage.apply(any(), any())(any(), any())).thenReturn(HtmlFormat.empty)
    when(mockSeqIdHandler.handleSequencing[PackageInformation](any(), any())(any()))
      .thenAnswer(new Answer[(Seq[PackageInformation], DeclarationMeta)] {
        def answer(invocation: InvocationOnMock): (Seq[PackageInformation], DeclarationMeta) = {
          val args = invocation.getArguments
          (args(0).asInstanceOf[Seq[PackageInformation]], args(1).asInstanceOf[DeclarationMeta])
        }
      })
  }

  override protected def afterEach(): Unit = {
    reset(mockAddPage, mockSeqIdHandler)
    super.afterEach()
  }

  override def getFormForDisplayRequest(request: Request[AnyContentAsEmpty.type]): Form[_] = {
    withNewCaching(aDeclaration())
    await(controller.displayPage(item.id)(request))
    thePackageInformation
  }

  def thePackageInformation: Form[PackageInformation] = {
    val captor = ArgumentCaptor.forClass(classOf[Form[PackageInformation]])
    verify(mockAddPage).apply(any(), captor.capture())(any(), any())
    captor.getValue
  }

  private def verifyAddPageInvoked(numberOfTimes: Int = 1): HtmlFormat.Appendable =
    verify(mockAddPage, times(numberOfTimes)).apply(any(), any())(any(), any())

  val item = anItem()

  "PackageInformation Add Controller" must {

    onEveryDeclarationJourney() { request =>
      "return 200 (OK)" that {
        "display page method is invoked" in {
          withNewCaching(request.cacheModel)

          val result = controller.displayPage(item.id)(getRequest())

          status(result) mustBe OK
          verifyAddPageInvoked()

          thePackageInformation.value mustBe empty
        }

      }

      "return 400 (BAD_REQUEST)" when {

        "user adds invalid data" in {
          withNewCaching(request.cacheModel)

          val requestBody = Seq("typesOfPackages" -> "invalid", "numberOfPackages" -> "invalid", "shippingMarks" -> "inva!id")
          val result = controller.submitForm(item.id)(postRequestAsFormUrlEncoded(requestBody: _*))

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
          val result = controller.submitForm(item.id)(postRequestAsFormUrlEncoded(requestBody: _*))

          status(result) mustBe BAD_REQUEST
          verifyAddPageInvoked()
        }

        "user adds too many codes" in {
          val packages = List.fill(99)(packageInformation)
          val item = anItem(withPackageInformation(packages))
          withNewCaching(aDeclarationAfter(request.cacheModel, withItems(item)))

          val requestBody = Seq("typesOfPackages" -> "AE", "numberOfPackages" -> "1", "shippingMarks" -> "1234")
          val result = controller.submitForm(item.id)(postRequestAsFormUrlEncoded(requestBody: _*))

          status(result) mustBe BAD_REQUEST
          verifyAddPageInvoked()
        }
      }

      "return 303 (SEE_OTHER)" when {
        "user submits valid data" in {
          val meta =
            declarationMeta.copy(maxSequenceIds = declarationMeta.maxSequenceIds + (implicitly[EsoKeyProvider[PackageInformation]].seqIdKey -> 1))
          val item1 = anItem(withPackageInformation(List(packageInformation)))
          withNewCaching(aDeclarationAfter(request.cacheModel, withItems(item1)).copy(declarationMeta = meta))

          val requestBody = Seq("typesOfPackages" -> "AE", "numberOfPackages" -> "1", "shippingMarks" -> "1234")
          val result = controller.submitForm(item.id)(postRequestAsFormUrlEncoded(requestBody: _*))

          await(result) mustBe aRedirectToTheNextPage
          thePageNavigatedTo mustBe routes.PackageInformationSummaryController.displayPage(item.id)

          And("max seq id is updated in dec meta")
          verify(mockSeqIdHandler).handleSequencing[PackageInformation](any(), any())(any())

          val declaration = theCacheModelUpdated
          val savedPackage = declaration.itemBy(item.id).flatMap(_.packageInformation).map(_.last)
          savedPackage.flatMap(_.typesOfPackages) mustBe Some("AE")
          savedPackage.flatMap(_.numberOfPackages) mustBe Some(1)
          savedPackage.flatMap(_.shippingMarks) mustBe Some("1234")
        }
      }
    }
  }
}
