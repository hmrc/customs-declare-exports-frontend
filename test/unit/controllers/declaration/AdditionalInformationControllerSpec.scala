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

import controllers.declaration.{routes, AdditionalInformationController}
import controllers.util.Remove
import forms.declaration.AdditionalInformation
import models.declaration.{AdditionalInformationData, ExportItem}
import models.{DeclarationType, ExportsDeclaration, Mode}
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{verify, when}
import play.api.data.Form
import play.api.mvc.{AnyContentAsEmpty, Request}
import play.api.test.Helpers._
import play.twirl.api.HtmlFormat
import unit.base.ControllerSpec
import unit.mock.ErrorHandlerMocks
import views.html.declaration.additional_information

class AdditionalInformationControllerSpec extends ControllerSpec with ErrorHandlerMocks {

  val additionalInformationPage = mock[additional_information]

  val controller = new AdditionalInformationController(
    mockAuthAction,
    mockJourneyAction,
    mockErrorHandler,
    mockExportsCacheService,
    navigator,
    stubMessagesControllerComponents(),
    additionalInformationPage
  )(ec)

  def journeyFor[A](declaration: ExportsDeclaration)(test: => A): A = {
    withNewCaching(declaration)
    test
  }

  override protected def beforeEach(): Unit = {
    super.beforeEach()
    setupErrorHandler()
    authorizedUser()
    when(additionalInformationPage.apply(any(), any(), any(), any())(any(), any())).thenReturn(HtmlFormat.empty)
  }

  def theResponseForm: Form[AdditionalInformation] = {
    val captor = ArgumentCaptor.forClass(classOf[Form[AdditionalInformation]])
    verify(additionalInformationPage).apply(any(), any(), captor.capture(), any())(any(), any())
    captor.getValue
  }

  override def getFormForDisplayRequest(request: Request[AnyContentAsEmpty.type]): Form[_] = {
    val item = anItem()
    withNewCaching(aDeclaration(withType(DeclarationType.SUPPLEMENTARY), withItem(item)))
    await(controller.displayPage(Mode.Normal, item.id)(request))
    theResponseForm
  }

  val itemCacheData =
    ExportItem("itemId", additionalInformation = Some(AdditionalInformationData(Seq(AdditionalInformation("12345", "description")))))

  val itemWith99InformationCacheData =
    ExportItem("itemId", additionalInformation = Some(AdditionalInformationData(Seq.fill(99)(AdditionalInformation("12345", "description")))))

  def journeyPageController(declaration: ExportsDeclaration): Unit = {
    "return 200 (OK)" when {

      "display page method is invoked with empty cache" in journeyFor(declaration) {
        val result = controller.displayPage(Mode.Normal, "itemId")(getRequest())

        status(result) must be(OK)
      }

      "display page method is invoked with data in cache" in journeyFor(aDeclarationAfter(declaration, withItem(itemCacheData))) {
        val result = controller.displayPage(Mode.Normal, "itemId")(getRequest())

        status(result) must be(OK)
      }
    }

    "return 400 (BAD_REQUEST)" when {

      "user provide wrong action" in journeyFor(declaration) {
        val wrongAction = Seq(("code", "12345"), ("description", "text"), ("WrongAction", ""))

        val result = controller.saveAdditionalInfo(Mode.Normal, "itemId")(postRequestAsFormUrlEncoded(wrongAction: _*))

        status(result) must be(BAD_REQUEST)
      }
    }

    "return 400 (BAD_REQUEST) during adding" when {

      "user put duplicated item" in journeyFor(aDeclarationAfter(declaration, withItem(itemCacheData))) {
        val duplicatedForm = Seq(("code", "12345"), ("description", "description"), addActionUrlEncoded())

        val result =
          controller.saveAdditionalInfo(Mode.Normal, "itemId")(postRequestAsFormUrlEncoded(duplicatedForm: _*))

        status(result) must be(BAD_REQUEST)
      }

      "user reach maximum amount of items" in journeyFor(aDeclarationAfter(declaration, withItem(itemWith99InformationCacheData))) {
        val form = Seq(("code", "12345"), ("description", "text"), addActionUrlEncoded())

        val result = controller.saveAdditionalInfo(Mode.Normal, "itemId")(postRequestAsFormUrlEncoded(form: _*))

        status(result) must be(BAD_REQUEST)
      }
    }

    "return 400 (BAD_REQUEST) during saving" when {

      "user put incorrect data" in journeyFor(declaration) {
        val incorrectForm = Seq(("code", "111"), ("description", ""), saveAndContinueActionUrlEncoded)

        val result =
          controller.saveAdditionalInfo(Mode.Normal, "itemId")(postRequestAsFormUrlEncoded(incorrectForm: _*))

        status(result) must be(BAD_REQUEST)
      }

      "user put duplicated item" in journeyFor(aDeclarationAfter(declaration, withItem(itemCacheData))) {
        val duplicatedForm = Seq(("code", "12345"), ("description", "description"), saveAndContinueActionUrlEncoded)

        val result =
          controller.saveAdditionalInfo(Mode.Normal, "itemId")(postRequestAsFormUrlEncoded(duplicatedForm: _*))

        status(result) must be(BAD_REQUEST)
      }

      "user reach maximum amount of items" in journeyFor(aDeclarationAfter(declaration, withItem(itemWith99InformationCacheData))) {
        val form = Seq(("code", "12345"), ("description", "text"), saveAndContinueActionUrlEncoded)

        val result = controller.saveAdditionalInfo(Mode.Normal, "itemId")(postRequestAsFormUrlEncoded(form: _*))

        status(result) must be(BAD_REQUEST)
      }
    }

    "return 303 (SEE_OTHER)" when {

      "user correctly add new item" in journeyFor(declaration) {
        val correctForm = Seq(("code", "12345"), ("description", "text"), addActionUrlEncoded())

        val result = controller.saveAdditionalInfo(Mode.Normal, "itemId")(postRequestAsFormUrlEncoded(correctForm: _*))

        status(result) must be(SEE_OTHER)
      }

      "user save correct data" in journeyFor(declaration) {
        val correctForm = Seq(("code", "12345"), ("description", "text"), saveAndContinueActionUrlEncoded)

        val result = controller.saveAdditionalInfo(Mode.Normal, "itemId")(postRequestAsFormUrlEncoded(correctForm: _*))

        await(result) mustBe aRedirectToTheNextPage
        thePageNavigatedTo mustBe routes.DocumentsProducedController.displayPage(Mode.Normal, "itemId")
      }

      "user save correct data without new item" in journeyFor(aDeclarationAfter(declaration, withItem(itemWith99InformationCacheData))) {
        val correctForm = saveAndContinueActionUrlEncoded

        val result = controller.saveAdditionalInfo(Mode.Normal, "itemId")(postRequestAsFormUrlEncoded(correctForm))

        await(result) mustBe aRedirectToTheNextPage
        thePageNavigatedTo mustBe routes.DocumentsProducedController.displayPage(Mode.Normal, "itemId")
      }

      "user remove existing item" in journeyFor(declaration) {
        val removeForm = (Remove.toString, "0")

        val result = controller.saveAdditionalInfo(Mode.Normal, "itemId")(postRequestAsFormUrlEncoded(removeForm))

        await(result) mustBe aRedirectToTheNextPage
        thePageNavigatedTo mustBe controllers.declaration.routes.AdditionalInformationController.displayPage(Mode.Normal, "itemId")
      }
    }
  }

  "Additional information controller" when {

    for (decType <- DeclarationType.values) {
      s"we are on $decType journey" should {
        behave like journeyPageController(aDeclaration(withType(decType)))
      }
    }
  }
}
