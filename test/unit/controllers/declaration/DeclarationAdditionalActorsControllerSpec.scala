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

import base.{Injector, TestHelper}
import controllers.declaration.DeclarationAdditionalActorsController
import controllers.util.Remove
import forms.common.Eori
import forms.declaration.DeclarationAdditionalActors
import models.{DeclarationType, Mode}
import models.declaration.DeclarationAdditionalActorsData
import play.api.libs.json.Json
import play.api.test.Helpers._
import unit.base.ControllerSpec
import unit.mock.ErrorHandlerMocks
import views.html.declaration.declaration_additional_actors

class DeclarationAdditionalActorsControllerSpec extends ControllerSpec with ErrorHandlerMocks with Injector {

  trait SetUp {
    val declarationAdditionalActorsPage = instanceOf[declaration_additional_actors]

    val controller = new DeclarationAdditionalActorsController(
      mockAuthAction,
      mockJourneyAction,
      mockErrorHandler,
      mockExportsCacheService,
      navigator,
      stubMessagesControllerComponents(),
      declarationAdditionalActorsPage
    )(ec)

    setupErrorHandler()
    authorizedUser()
    withNewCaching(aDeclaration(withType(DeclarationType.SUPPLEMENTARY)))
  }

  val eori = "GB12345678912345"
  val additionalActor = DeclarationAdditionalActors(Some(Eori(eori)), Some("CS"))
  val declarationWithActor =
    aDeclaration(withDeclarationAdditionalActors(additionalActor))

  val maxAmountOfItems = aDeclaration(
    withDeclarationAdditionalActors(DeclarationAdditionalActorsData(Seq.fill(DeclarationAdditionalActorsData.maxNumberOfActors)(additionalActor)))
  )

  "Declaration Additional Actors controller" should {

    "return 200 (OK)" when {

      "display page method is invoked with empty cache" in new SetUp {

        val result = controller.displayPage(Mode.Normal)(getRequest())

        status(result) must be(OK)
      }

      "display page method is invoked with data in cache" in new SetUp {

        withNewCaching(declarationWithActor)

        val result = controller.displayPage(Mode.Normal)(getRequest())

        status(result) must be(OK)
      }
    }

    "return 400 (BAD_REQUEST)" when {

      "user provide wrong action" in new SetUp {

        val wrongAction = Seq(("eori", "GB123456"), ("partyType", "CS"), ("WrongAction", ""))

        val result = controller.saveForm(Mode.Normal)(postRequestAsFormUrlEncoded(wrongAction: _*))

        status(result) must be(BAD_REQUEST)
      }
    }

    "return 400 (BAD_REQUEST) during adding" when {

      "user put incorrect data" in new SetUp {

        val longerEori = TestHelper.createRandomAlphanumericString(18)
        val wrongAction = Seq(("eori", longerEori), ("partyType", "CS"), addActionUrlEncoded())

        val result = controller.saveForm(Mode.Normal)(postRequestAsFormUrlEncoded(wrongAction: _*))

        status(result) must be(BAD_REQUEST)
      }

      "user put duplicated item" in new SetUp {

        withNewCaching(declarationWithActor)

        val duplication = Seq(("eori", eori), ("partyType", "CS"), addActionUrlEncoded())

        val result = controller.saveForm(Mode.Normal)(postRequestAsFormUrlEncoded(duplication: _*))

        status(result) must be(BAD_REQUEST)
      }

      "user reach maximum amount of items" in new SetUp {

        withNewCaching(maxAmountOfItems)

        val correctForm = Seq(("eori", "GB123456"), ("partyType", "CS"), addActionUrlEncoded())

        val result = controller.saveForm(Mode.Normal)(postRequestAsFormUrlEncoded(correctForm: _*))

        status(result) must be(BAD_REQUEST)
      }
    }

    "return 400 (BAD_REQUEST) during saving" when {

      "user put incorrect data" in new SetUp {

        val longerEori = TestHelper.createRandomAlphanumericString(18)
        val wrongAction = Seq(("eori", longerEori), ("partyType", "CS"), saveAndContinueActionUrlEncoded)

        val result = controller.saveForm(Mode.Normal)(postRequestAsFormUrlEncoded(wrongAction: _*))

        status(result) must be(BAD_REQUEST)
      }

      "user put duplicated item" in new SetUp {

        withNewCaching(declarationWithActor)

        val duplication = Seq(("eori", eori), ("partyType", "CS"), saveAndContinueActionUrlEncoded)

        val result = controller.saveForm(Mode.Normal)(postRequestAsFormUrlEncoded(duplication: _*))

        status(result) must be(BAD_REQUEST)
      }

      "user reach maximum amount of items" in new SetUp {

        withNewCaching(maxAmountOfItems)

        val correctForm = Seq(("eori", "GB123456"), ("partyType", "CS"), saveAndContinueActionUrlEncoded)

        val result = controller.saveForm(Mode.Normal)(postRequestAsFormUrlEncoded(correctForm: _*))

        status(result) must be(BAD_REQUEST)
      }
    }

    "return 303 (SEE_OTHER)" when {

      "user correctly add new consolidator" in new SetUp {

        val correctForm = Seq(("eoriCS", eori), ("partyType", "CS"), addActionUrlEncoded())

        val result = controller.saveForm(Mode.Normal)(postRequestAsFormUrlEncoded(correctForm: _*))

        status(result) must be(SEE_OTHER)
      }

      "user correctly add new manufacturer" in new SetUp {

        val correctForm = Seq(("eoriMF", eori), ("partyType", "MF"), addActionUrlEncoded())

        val result = controller.saveForm(Mode.Normal)(postRequestAsFormUrlEncoded(correctForm: _*))

        status(result) must be(SEE_OTHER)
      }

      "user correctly add new freight forwarder" in new SetUp {

        val correctForm = Seq(("eoriFW", eori), ("partyType", "FW"), addActionUrlEncoded())

        val result = controller.saveForm(Mode.Normal)(postRequestAsFormUrlEncoded(correctForm: _*))

        status(result) must be(SEE_OTHER)
      }

      "user correctly add new warehouse keeper" in new SetUp {

        val correctForm = Seq(("eoriWH", eori), ("partyType", "WH"), addActionUrlEncoded())

        val result = controller.saveForm(Mode.Normal)(postRequestAsFormUrlEncoded(correctForm: _*))

        status(result) must be(SEE_OTHER)
      }

      "user save correct consolidator" in new SetUp {

        val correctForm = Seq(("eoriCS", eori), ("partyType", "CS"), saveAndContinueActionUrlEncoded)

        val result = controller.saveForm(Mode.Normal)(postRequestAsFormUrlEncoded(correctForm: _*))

        await(result) mustBe aRedirectToTheNextPage
        thePageNavigatedTo mustBe controllers.declaration.routes.DeclarationHolderController.displayPage(Mode.Normal)
      }

      "user save correct manufacturer" in new SetUp {

        val correctForm = Seq(("eoriMF", eori), ("partyType", "MF"), saveAndContinueActionUrlEncoded)

        val result = controller.saveForm(Mode.Normal)(postRequestAsFormUrlEncoded(correctForm: _*))

        await(result) mustBe aRedirectToTheNextPage
        thePageNavigatedTo mustBe controllers.declaration.routes.DeclarationHolderController.displayPage(Mode.Normal)
      }

      "user save correct freight forwarder" in new SetUp {

        val correctForm = Seq(("eoriFW", eori), ("partyType", "FW"), saveAndContinueActionUrlEncoded)

        val result = controller.saveForm(Mode.Normal)(postRequestAsFormUrlEncoded(correctForm: _*))

        await(result) mustBe aRedirectToTheNextPage
        thePageNavigatedTo mustBe controllers.declaration.routes.DeclarationHolderController.displayPage(Mode.Normal)
      }

      "user save correct warehouse keeper" in new SetUp {

        val correctForm = Seq(("eoriWH", eori), ("partyType", "WH"), saveAndContinueActionUrlEncoded)

        val result = controller.saveForm(Mode.Normal)(postRequestAsFormUrlEncoded(correctForm: _*))

        await(result) mustBe aRedirectToTheNextPage
        thePageNavigatedTo mustBe controllers.declaration.routes.DeclarationHolderController.displayPage(Mode.Normal)
      }

      "user remove existing item" in new SetUp {

        withNewCaching(declarationWithActor)

        val removeForm = (Remove.toString, Json.toJson(additionalActor).toString)

        val result = controller.saveForm(Mode.Normal)(postRequestAsFormUrlEncoded(removeForm))

        await(result) mustBe aRedirectToTheNextPage
        thePageNavigatedTo mustBe controllers.declaration.routes.DeclarationAdditionalActorsController.displayPage(Mode.Normal)
      }
    }
  }
}
