/*
 * Copyright 2019 HM Revenue & Customs
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
import forms.Choice
import forms.declaration.AdditionalInformation
import models.{DeclarationType, ExportsDeclaration, Mode}
import models.declaration.{AdditionalInformationData, ExportItem}
import play.api.test.Helpers._
import unit.base.ControllerSpec
import unit.mock.ErrorHandlerMocks
import views.html.declaration.additional_information

class AdditionalInformationControllerSpec extends ControllerSpec with ErrorHandlerMocks {

  val additionalInformationPage = new additional_information(mainTemplate)

  val controller = new AdditionalInformationController(
    mockAuthAction,
    mockJourneyAction,
    mockErrorHandler,
    mockExportsCacheService,
    navigator,
    stubMessagesControllerComponents(),
    additionalInformationPage
  )(ec)

  val standardDeclaration = aDeclaration(withType(DeclarationType.STANDARD))

  val supplementaryDeclaration = aDeclaration(withType(DeclarationType.SUPPLEMENTARY))

  val simplifiedDeclaration = aDeclaration(withType(DeclarationType.SIMPLIFIED))

  def journeyFor[A](declaration: ExportsDeclaration)(test: => A): A = {
    withNewCaching(declaration)
    test
  }

  override protected def beforeEach(): Unit = {
    super.beforeEach()
    setupErrorHandler()
    authorizedUser()
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

        status(result) must be(OK)
      }
    }
  }

  "Additional information controller" when {
    "we are on Supplementary journey" should {
      behave like journeyPageController(supplementaryDeclaration)
    }

    "we are on Standard journey" should {
      behave like journeyPageController(standardDeclaration)
    }

    "we are on Simplified journey" should {
      behave like journeyPageController(simplifiedDeclaration)
    }
  }
}
