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

import controllers.declaration.TransportContainerController
import controllers.util.Remove
import forms.Choice.AllowedChoiceValues.SupplementaryDec
import forms.declaration.TransportInformationContainer
import models.Mode
import models.declaration.TransportInformationContainerData
import play.api.test.Helpers._
import unit.base.ControllerSpec
import unit.mock.ErrorHandlerMocks
import views.html.declaration.add_transport_containers

class TransportContainerControllerSpec extends ControllerSpec with ErrorHandlerMocks {

  trait SetUp {
    val transportContainersPage = new add_transport_containers(mainTemplate)

    val controller = new TransportContainerController(
      mockAuthAction,
      mockJourneyAction,
      navigator,
      mockErrorHandler,
      mockExportsCacheService,
      stubMessagesControllerComponents(),
      transportContainersPage
    )(ec)

    authorizedUser()
    setupErrorHandler()
    withNewCaching(aDeclaration(withChoice(SupplementaryDec)))
  }

  val containerData = TransportInformationContainerData(Seq(TransportInformationContainer("id")))
  val maxContainerData = TransportInformationContainerData(
    Seq.fill(TransportInformationContainerData.maxNumberOfItems)(TransportInformationContainer("id"))
  )

  "Transport Container controller" should {

    "return 200 (OK)" when {

      "display page method is invoked with empty cache" in new SetUp {
        val result = controller.displayPage(Mode.Normal)(getRequest())

        status(result) must be(OK)
      }

      "display page method is invoked with data in cache" in new SetUp {

        withNewCaching(aDeclaration(withContainerData(containerData)))

        val result = controller.displayPage(Mode.Normal)(getRequest())

        status(result) must be(OK)
      }
    }

    "return 400 (BAD_REQUEST)" when {

      "user provide wrong action" in new SetUp {

        val wrongAction = Seq(("id", "containerId"), ("WrongAction", ""))

        val result = controller.submitForm(Mode.Normal)(postRequestAsFormUrlEncoded(wrongAction: _*))

        status(result) must be(BAD_REQUEST)
      }
    }

    "return 400 (BAD_REQUEST) during adding" when {

      "user put incorrect data" in new SetUp {

        val incorrectForm = Seq(("id", "!@#$"), addActionUrlEncoded)

        val result = controller.submitForm(Mode.Normal)(postRequestAsFormUrlEncoded(incorrectForm: _*))

        status(result) must be(BAD_REQUEST)
      }

      "user put duplicated item" in new SetUp {

        withNewCaching(aDeclaration(withContainerData(containerData)))

        val duplicatedForm = Seq(("id", "id"), addActionUrlEncoded)

        val result = controller.submitForm(Mode.Normal)(postRequestAsFormUrlEncoded(duplicatedForm: _*))

        status(result) must be(BAD_REQUEST)
      }

      "user reach maximum amount of items" in new SetUp {

        withNewCaching(aDeclaration(withContainerData(maxContainerData)))

        val form = Seq(("id", "id2"), addActionUrlEncoded)

        val result = controller.submitForm(Mode.Normal)(postRequestAsFormUrlEncoded(form: _*))

        status(result) must be(BAD_REQUEST)
      }
    }

    "return 400 (BAD_REQUEST) during saving" when {

      "user put incorrect data" in new SetUp {

        val incorrectForm = Seq(("id", "!@#$"), saveAndContinueActionUrlEncoded)

        val result = controller.submitForm(Mode.Normal)(postRequestAsFormUrlEncoded(incorrectForm: _*))

        status(result) must be(BAD_REQUEST)
      }

      "user put duplicated item" in new SetUp {

        withNewCaching(aDeclaration(withContainerData(containerData)))

        val duplicatedForm = Seq(("id", "id"), saveAndContinueActionUrlEncoded)

        val result = controller.submitForm(Mode.Normal)(postRequestAsFormUrlEncoded(duplicatedForm: _*))

        status(result) must be(BAD_REQUEST)
      }

      "user reach maximum amount of items" in new SetUp {

        withNewCaching(aDeclaration(withContainerData(maxContainerData)))

        val form = Seq(("id", "id2"), saveAndContinueActionUrlEncoded)

        val result = controller.submitForm(Mode.Normal)(postRequestAsFormUrlEncoded(form: _*))

        status(result) must be(BAD_REQUEST)
      }
    }

    "return 303 (SEE_OTHER)" when {

      "user correctly add new item" in new SetUp {

        val correctForm = Seq(("id", "123abc"), addActionUrlEncoded)

        val result = controller.submitForm(Mode.Normal)(postRequestAsFormUrlEncoded(correctForm: _*))

        await(result) mustBe aRedirectToTheNextPage
        thePageNavigatedTo mustBe controllers.declaration.routes.TransportContainerController.displayPage()
      }

      "user save correct data" when {
        "normal mode" in new SetUp {
          val correctForm = Seq(("id", "123abc"), saveAndContinueActionUrlEncoded)

          val result = controller.submitForm(Mode.Normal)(postRequestAsFormUrlEncoded(correctForm: _*))

          await(result) mustBe aRedirectToTheNextPage
          thePageNavigatedTo mustBe controllers.declaration.routes.SummaryController.displayPage(Mode.Normal)
        }

        "draft mode" in new SetUp {
          val correctForm = Seq(("id", "123abc"), saveAndContinueActionUrlEncoded)

          val result = controller.submitForm(Mode.Draft)(postRequestAsFormUrlEncoded(correctForm: _*))

          await(result) mustBe aRedirectToTheNextPage
          thePageNavigatedTo mustBe controllers.declaration.routes.SummaryController.displayPage(Mode.Normal)
        }
      }

      "user save correct data without new item" in new SetUp {

        withNewCaching(aDeclaration(withContainerData(containerData)))

        val result = controller.submitForm(Mode.Normal)(postRequestAsFormUrlEncoded(saveAndContinueActionUrlEncoded))

        await(result) mustBe aRedirectToTheNextPage
        thePageNavigatedTo mustBe controllers.declaration.routes.SealController.displayPage()
      }

      "user remove existing item" in new SetUp {

        withNewCaching(aDeclaration(withContainerData(containerData)))

        val removeForm = (Remove.toString, "0")

        val result = controller.submitForm(Mode.Normal)(postRequestAsFormUrlEncoded(removeForm))

        status(result) must be(OK)
      }
    }
  }
}
