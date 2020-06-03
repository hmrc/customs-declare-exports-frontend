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

import base.Injector
import controllers.declaration.DeclarationHolderController
import controllers.util.Remove
import forms.common.Eori
import forms.declaration.DeclarationHolder
import models.declaration.DeclarationHoldersData
import models.{DeclarationType, Mode}
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, verify, when}
import play.api.data.Form
import play.api.mvc.{AnyContentAsEmpty, Request}
import play.api.test.Helpers._
import play.twirl.api.HtmlFormat
import unit.base.ControllerSpec
import unit.mock.ErrorHandlerMocks
import views.html.declaration.declaration_holder

class DeclarationHolderControllerSpec extends ControllerSpec with ErrorHandlerMocks with Injector {

  val declarationHolderPage = mock[declaration_holder]

  val controller = new DeclarationHolderController(
    mockAuthAction,
    mockJourneyAction,
    mockErrorHandler,
    mockExportsCacheService,
    navigator,
    stubMessagesControllerComponents(),
    declarationHolderPage
  )

  override protected def beforeEach(): Unit = {
    super.beforeEach()
    setupErrorHandler()
    authorizedUser()
    withNewCaching(aDeclaration())

    when(declarationHolderPage.apply(any(), any(), any())(any(), any())).thenReturn(HtmlFormat.empty)
  }

  override protected def afterEach(): Unit = {
    reset(declarationHolderPage)
    super.afterEach()
  }

  def theResponseForm: Form[DeclarationHolder] = {
    val formCaptor = ArgumentCaptor.forClass(classOf[Form[DeclarationHolder]])
    verify(declarationHolderPage).apply(any(), formCaptor.capture(), any())(any(), any())
    formCaptor.getValue
  }

  override def getFormForDisplayRequest(request: Request[AnyContentAsEmpty.type]): Form[_] = {
    withNewCaching(aDeclaration())
    await(controller.displayPage(Mode.Normal)(request))
    theResponseForm
  }

  val declarationWithHolder = aDeclaration(withDeclarationHolders(Some("ACP"), Some(Eori("GB123456"))))
  val maxAmountOfItems = aDeclaration(
    withDeclarationHolders(Seq.fill(DeclarationHoldersData.limitOfHolders)(DeclarationHolder(Some("ACP"), Some(Eori("GB123456")))): _*)
  )

  "Declaration Additional Actors controller" should {

    "return 200 (OK)" when {

      "display page method is invoked with empty cache" in {

        val result = controller.displayPage(Mode.Normal)(getRequest())

        status(result) must be(OK)
      }

      "display page method is invoked with data in cache" in {

        withNewCaching(aDeclaration(withDeclarationHolders()))

        val result = controller.displayPage(Mode.Normal)(getRequest())

        status(result) must be(OK)
      }
    }

    "return 400 (BAD_REQUEST)" when {

      "user provide wrong action" in {

        val wrongAction = Seq(("authorisationTypeCode", "ACP"), ("eori", "GB123456"), ("WrongAction", ""))

        val result = controller.submitHoldersOfAuthorisation(Mode.Normal)(postRequestAsFormUrlEncoded(wrongAction: _*))

        status(result) must be(BAD_REQUEST)
      }
    }

    "return 400 (BAD_REQUEST) during adding" when {

      "user put incorrect data" in {

        val incorrectForm = Seq(("authorisationTypeCode", "incorrect"), ("eori", "GB123456"), addActionUrlEncoded())

        val result =
          controller.submitHoldersOfAuthorisation(Mode.Normal)(postRequestAsFormUrlEncoded(incorrectForm: _*))

        status(result) must be(BAD_REQUEST)
      }

      "user put duplicated item" in {

        withNewCaching(declarationWithHolder)

        val duplicatedForm = Seq(("authorisationTypeCode", "ACP"), ("eori", "GB123456"), addActionUrlEncoded())

        val result =
          controller.submitHoldersOfAuthorisation(Mode.Normal)(postRequestAsFormUrlEncoded(duplicatedForm: _*))

        status(result) must be(BAD_REQUEST)
      }

      "user reach maximum amount of items" in {

        withNewCaching(maxAmountOfItems)

        val correctForm = Seq(("authorisationTypeCode", "ACT"), ("eori", "GB654321"), addActionUrlEncoded())

        val result = controller.submitHoldersOfAuthorisation(Mode.Normal)(postRequestAsFormUrlEncoded(correctForm: _*))

        status(result) must be(BAD_REQUEST)
      }
    }

    "return 400 (BAD_REQUEST) during saving" when {

      "user put incorrect data" in {

        val incorrectForm =
          Seq(("authorisationTypeCode", "incorrect"), ("eori", "GB123456"), saveAndContinueActionUrlEncoded)

        val result =
          controller.submitHoldersOfAuthorisation(Mode.Normal)(postRequestAsFormUrlEncoded(incorrectForm: _*))

        status(result) must be(BAD_REQUEST)
      }

      "user put duplicated item" in {

        withNewCaching(declarationWithHolder)

        val duplicatedForm =
          Seq(("authorisationTypeCode", "ACP"), ("eori", "GB123456"), saveAndContinueActionUrlEncoded)

        val result =
          controller.submitHoldersOfAuthorisation(Mode.Normal)(postRequestAsFormUrlEncoded(duplicatedForm: _*))

        status(result) must be(BAD_REQUEST)
      }

      "user reach maximum amount of items" in {

        withNewCaching(maxAmountOfItems)

        val correctForm = Seq(("authorisationTypeCode", "ACT"), ("eori", "GB654321"), saveAndContinueActionUrlEncoded)

        val result = controller.submitHoldersOfAuthorisation(Mode.Normal)(postRequestAsFormUrlEncoded(correctForm: _*))

        status(result) must be(BAD_REQUEST)
      }
    }

    "return 303 (SEE_OTHER)" when {

      "user correctly add new item" in {

        val correctForm = Seq(("authorisationTypeCode", "ACT"), ("eori", "GB65432123456789"), addActionUrlEncoded())

        val result = controller.submitHoldersOfAuthorisation(Mode.Normal)(postRequestAsFormUrlEncoded(correctForm: _*))

        status(result) must be(SEE_OTHER)
      }

      "user save correct data" in {

        val correctForm = Seq(("authorisationTypeCode", "ACT"), ("eori", "GB65432123456789"), saveAndContinueActionUrlEncoded)

        val result = controller.submitHoldersOfAuthorisation(Mode.Normal)(postRequestAsFormUrlEncoded(correctForm: _*))

        await(result) mustBe aRedirectToTheNextPage
        thePageNavigatedTo mustBe controllers.declaration.routes.OriginationCountryController.displayPage()
      }

      "user save correct data without new item" in {

        withNewCaching(declarationWithHolder)

        val result =
          controller.submitHoldersOfAuthorisation(Mode.Normal)(postRequestAsFormUrlEncoded(saveAndContinueActionUrlEncoded))

        await(result) mustBe aRedirectToTheNextPage
        thePageNavigatedTo mustBe controllers.declaration.routes.OriginationCountryController.displayPage()
      }

      "user remove existing item" in {

        withNewCaching(declarationWithHolder)

        val removeAction = (Remove.toString, "ACT-GB123456")

        val result = controller.submitHoldersOfAuthorisation(Mode.Normal)(postRequestAsFormUrlEncoded(removeAction))

        await(result) mustBe aRedirectToTheNextPage
        thePageNavigatedTo mustBe controllers.declaration.routes.DeclarationHolderController.displayPage(Mode.Normal)
      }
    }

    "should redirect to Origination Country page" when {

      "user is during Supplementary journey" in {

        withNewCaching(aDeclaration(withType(DeclarationType.SUPPLEMENTARY)))

        val correctForm = Seq(("authorisationTypeCode", "ACT"), ("eori", "GB65432112345655"), saveAndContinueActionUrlEncoded)

        val result = controller.submitHoldersOfAuthorisation(Mode.Normal)(postRequestAsFormUrlEncoded(correctForm: _*))

        await(result) mustBe aRedirectToTheNextPage
        thePageNavigatedTo mustBe controllers.declaration.routes.OriginationCountryController.displayPage()
      }

      "user is during Standard journey" in {

        withNewCaching(aDeclaration(withType(DeclarationType.STANDARD)))

        val correctForm = Seq(("authorisationTypeCode", "ACT"), ("eori", "GB6543211234567"), saveAndContinueActionUrlEncoded)

        val result = controller.submitHoldersOfAuthorisation(Mode.Normal)(postRequestAsFormUrlEncoded(correctForm: _*))

        await(result) mustBe aRedirectToTheNextPage
        thePageNavigatedTo mustBe controllers.declaration.routes.OriginationCountryController.displayPage()
      }
    }

    "should redirect to Destination Country page" when {

      "user is during Simplified journey" in {

        withNewCaching(aDeclaration(withType(DeclarationType.SIMPLIFIED)))

        val correctForm = Seq(("authorisationTypeCode", "ACT"), ("eori", "GB6543211234567"), saveAndContinueActionUrlEncoded)

        val result = controller.submitHoldersOfAuthorisation(Mode.Normal)(postRequestAsFormUrlEncoded(correctForm: _*))

        await(result) mustBe aRedirectToTheNextPage
        thePageNavigatedTo mustBe controllers.declaration.routes.DestinationCountryController.displayPage()
      }
    }
  }
}
