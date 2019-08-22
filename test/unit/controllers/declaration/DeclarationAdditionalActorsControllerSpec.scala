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

import base.TestHelper
import controllers.declaration.DeclarationAdditionalActorsController
import controllers.util.Remove
import forms.Choice.AllowedChoiceValues.SupplementaryDec
import forms.declaration.DeclarationAdditionalActors
import models.declaration.DeclarationAdditionalActorsData
import play.api.libs.json.Json
import play.api.test.Helpers._
import unit.base.ControllerSpec
import unit.mock.ErrorHandlerMocks
import views.html.declaration.declaration_additional_actors

class DeclarationAdditionalActorsControllerSpec extends ControllerSpec with ErrorHandlerMocks {

  trait SetUp {
    val declarationAdditionalActorsPage = new declaration_additional_actors(mainTemplate)

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
    withNewCaching(aDeclaration(withChoice(SupplementaryDec)))
  }

  val eori = "GB123456"
  val additionalActor = DeclarationAdditionalActors(Some(eori), Some("CS"))
  val declarationWithActor =
    aDeclaration(withDeclarationAdditionalActors(additionalActor))

  val maxAmountOfItems = aDeclaration(
    withDeclarationAdditionalActors(
      DeclarationAdditionalActorsData(Seq.fill(DeclarationAdditionalActorsData.maxNumberOfItems)(additionalActor))
    )
  )

  "Declaration Additional Actors controller" should {

    "return 200 (OK)" when {

      "display page method is invoked with empty cache" in new SetUp {

        val result = controller.displayForm()(getRequest())

        status(result) must be(OK)
      }

      "display page method is invoked with data in cache" in new SetUp {

        withNewCaching(declarationWithActor)

        val result = controller.displayForm()(getRequest())

        status(result) must be(OK)
      }
    }

    "return 400 (BAD_REQUEST)" when {

      "user provide wrong action" in new SetUp {

        val wrongAction = Seq(("eori", "GB123456"), ("partyType", "CS"), ("WrongAction", ""))

        val result = controller.saveForm()(postRequestAsFormUrlEncoded(wrongAction: _*))

        status(result) must be(BAD_REQUEST)
      }
    }

    "return 400 (BAD_REQUEST) during adding" when {

      "user put incorrect data" in new SetUp {

        val longerEori = TestHelper.createRandomAlphanumericString(18)
        val wrongAction = Seq(("eori", longerEori), ("partyType", "CS"), addActionUrlEncoded)

        val result = controller.saveForm()(postRequestAsFormUrlEncoded(wrongAction: _*))

        status(result) must be(BAD_REQUEST)
      }

      "user put duplicated item" in new SetUp {

        withNewCaching(declarationWithActor)

        val duplication = Seq(("eori", eori), ("partyType", "CS"), addActionUrlEncoded)

        val result = controller.saveForm()(postRequestAsFormUrlEncoded(duplication: _*))

        status(result) must be(BAD_REQUEST)
      }

      "user reach maximum amount of items" in new SetUp {

        withNewCaching(maxAmountOfItems)

        val correctForm = Seq(("eori", "GB123456"), ("partyType", "CS"), addActionUrlEncoded)

        val result = controller.saveForm()(postRequestAsFormUrlEncoded(correctForm: _*))

        status(result) must be(BAD_REQUEST)
      }
    }

    "return 400 (BAD_REQUEST) during saving" when {

      "user put incorrect data" in new SetUp {

        val longerEori = TestHelper.createRandomAlphanumericString(18)
        val wrongAction = Seq(("eori", longerEori), ("partyType", "CS"), saveAndContinueActionUrlEncoded)

        val result = controller.saveForm()(postRequestAsFormUrlEncoded(wrongAction: _*))

        status(result) must be(BAD_REQUEST)
      }

      "user put duplicated item" in new SetUp {

        withNewCaching(declarationWithActor)

        val duplication = Seq(("eori", eori), ("partyType", "CS"), saveAndContinueActionUrlEncoded)

        val result = controller.saveForm()(postRequestAsFormUrlEncoded(duplication: _*))

        status(result) must be(BAD_REQUEST)
      }

      "user reach maximum amount of items" in new SetUp {

        withNewCaching(maxAmountOfItems)

        val correctForm = Seq(("eori", "GB123456"), ("partyType", "CS"), saveAndContinueActionUrlEncoded)

        val result = controller.saveForm()(postRequestAsFormUrlEncoded(correctForm: _*))

        status(result) must be(BAD_REQUEST)
      }
    }

    "return 303 (SEE_OTHER)" when {

      "user correctly add new item" in new SetUp {

        val correctForm = Seq(("eori", eori), ("partyType", "CS"), addActionUrlEncoded)

        val result = controller.saveForm()(postRequestAsFormUrlEncoded(correctForm: _*))

        status(result) must be(SEE_OTHER)
      }

      "user save correct data" in new SetUp {

        val correctForm = Seq(("eori", eori), ("partyType", "CS"), saveAndContinueActionUrlEncoded)

        val result = controller.saveForm()(postRequestAsFormUrlEncoded(correctForm: _*))

        status(result) must be(SEE_OTHER)
      }

      "user save correct data without new item" in new SetUp {

        withNewCaching(declarationWithActor)

        val result = controller.saveForm()(postRequestAsFormUrlEncoded(saveAndContinueActionUrlEncoded))

        status(result) must be(SEE_OTHER)
      }

      "user remove existing item" in new SetUp {

        withNewCaching(declarationWithActor)

        val removeForm = (Remove.toString, Json.toJson(additionalActor).toString)

        val result = controller.saveForm()(postRequestAsFormUrlEncoded(removeForm))

        status(result) must be(OK)
      }
    }
  }
}
