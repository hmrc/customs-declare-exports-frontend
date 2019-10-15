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

import controllers.declaration.DeclarationHolderController
import controllers.util.Remove
import forms.declaration.DeclarationHolder
import models.Mode
import models.declaration.DeclarationHoldersData
import play.api.test.Helpers._
import unit.base.ControllerSpec
import unit.mock.ErrorHandlerMocks
import views.html.declaration.declaration_holder

class DeclarationHolderControllerSpec extends ControllerSpec with ErrorHandlerMocks {

  trait SetUp {
    val declarationHolderPage = new declaration_holder(mainTemplate)

    val controller = new DeclarationHolderController(
      mockAuthAction,
      mockJourneyAction,
      mockErrorHandler,
      mockExportsCacheService,
      navigator,
      stubMessagesControllerComponents(),
      declarationHolderPage
    )

    setupErrorHandler()
    authorizedUser()
    withNewCaching(aDeclaration())
  }

  val declarationWithHolder = aDeclaration(withDeclarationHolders(Some("ACP"), Some("GB123456")))
  val maxAmountOfItems = aDeclaration(
    withDeclarationHolders(Seq.fill(DeclarationHoldersData.limitOfHolders)(DeclarationHolder(Some("ACP"), Some("GB123456"))): _*)
  )

  "Declaration Additional Actors controller" should {

    "return 200 (OK)" when {

      "display page method is invoked with empty cache" in new SetUp {

        val result = controller.displayPage(Mode.Normal)(getRequest())

        status(result) must be(OK)
      }

      "display page method is invoked with data in cache" in new SetUp {

        withNewCaching(aDeclaration(withDeclarationHolders()))

        val result = controller.displayPage(Mode.Normal)(getRequest())

        status(result) must be(OK)
      }
    }

    "return 400 (BAD_REQUEST)" when {

      "user provide wrong action" in new SetUp {

        val wrongAction = Seq(("authorisationTypeCode", "ACP"), ("eori", "GB123456"), ("WrongAction", ""))

        val result = controller.submitHoldersOfAuthorisation(Mode.Normal)(postRequestAsFormUrlEncoded(wrongAction: _*))

        status(result) must be(BAD_REQUEST)
      }
    }

    "return 400 (BAD_REQUEST) during adding" when {

      "user put incorrect data" in new SetUp {

        val incorrectForm = Seq(("authorisationTypeCode", "incorrect"), ("eori", "GB123456"), addActionUrlEncoded())

        val result =
          controller.submitHoldersOfAuthorisation(Mode.Normal)(postRequestAsFormUrlEncoded(incorrectForm: _*))

        status(result) must be(BAD_REQUEST)
      }

      "user put duplicated item" in new SetUp {

        withNewCaching(declarationWithHolder)

        val duplicatedForm = Seq(("authorisationTypeCode", "ACP"), ("eori", "GB123456"), addActionUrlEncoded())

        val result =
          controller.submitHoldersOfAuthorisation(Mode.Normal)(postRequestAsFormUrlEncoded(duplicatedForm: _*))

        status(result) must be(BAD_REQUEST)
      }

      "user reach maximum amount of items" in new SetUp {

        withNewCaching(maxAmountOfItems)

        val correctForm = Seq(("authorisationTypeCode", "ACT"), ("eori", "GB654321"), addActionUrlEncoded())

        val result = controller.submitHoldersOfAuthorisation(Mode.Normal)(postRequestAsFormUrlEncoded(correctForm: _*))

        status(result) must be(BAD_REQUEST)
      }
    }

    "return 400 (BAD_REQUEST) during saving" when {

      "user put incorrect data" in new SetUp {

        val incorrectForm =
          Seq(("authorisationTypeCode", "incorrect"), ("eori", "GB123456"), saveAndContinueActionUrlEncoded)

        val result =
          controller.submitHoldersOfAuthorisation(Mode.Normal)(postRequestAsFormUrlEncoded(incorrectForm: _*))

        status(result) must be(BAD_REQUEST)
      }

      "user put duplicated item" in new SetUp {

        withNewCaching(declarationWithHolder)

        val duplicatedForm =
          Seq(("authorisationTypeCode", "ACP"), ("eori", "GB123456"), saveAndContinueActionUrlEncoded)

        val result =
          controller.submitHoldersOfAuthorisation(Mode.Normal)(postRequestAsFormUrlEncoded(duplicatedForm: _*))

        status(result) must be(BAD_REQUEST)
      }

      "user reach maximum amount of items" in new SetUp {

        withNewCaching(maxAmountOfItems)

        val correctForm = Seq(("authorisationTypeCode", "ACT"), ("eori", "GB654321"), saveAndContinueActionUrlEncoded)

        val result = controller.submitHoldersOfAuthorisation(Mode.Normal)(postRequestAsFormUrlEncoded(correctForm: _*))

        status(result) must be(BAD_REQUEST)
      }
    }

    "return 303 (SEE_OTHER)" when {

      "user correctly add new item" in new SetUp {

        val correctForm = Seq(("authorisationTypeCode", "ACT"), ("eori", "GB654321"), addActionUrlEncoded())

        val result = controller.submitHoldersOfAuthorisation(Mode.Normal)(postRequestAsFormUrlEncoded(correctForm: _*))

        status(result) must be(SEE_OTHER)
      }

      "user save correct data" in new SetUp {

        val correctForm = Seq(("authorisationTypeCode", "ACT"), ("eori", "GB654321"), saveAndContinueActionUrlEncoded)

        val result = controller.submitHoldersOfAuthorisation(Mode.Normal)(postRequestAsFormUrlEncoded(correctForm: _*))

        await(result) mustBe aRedirectToTheNextPage
        thePageNavigatedTo mustBe controllers.declaration.routes.DestinationCountriesController.displayPage()
      }

      "user save correct data without new item" in new SetUp {

        withNewCaching(declarationWithHolder)

        val result =
          controller.submitHoldersOfAuthorisation(Mode.Normal)(postRequestAsFormUrlEncoded(saveAndContinueActionUrlEncoded))

        await(result) mustBe aRedirectToTheNextPage
        thePageNavigatedTo mustBe controllers.declaration.routes.DestinationCountriesController.displayPage()
      }

      "user remove existing item" in new SetUp {

        withNewCaching(declarationWithHolder)

        val removeAction = (Remove.toString, "ACT-GB123456")

        val result = controller.submitHoldersOfAuthorisation(Mode.Normal)(postRequestAsFormUrlEncoded(removeAction))

        status(result) must be(OK)
      }
    }
  }
}
