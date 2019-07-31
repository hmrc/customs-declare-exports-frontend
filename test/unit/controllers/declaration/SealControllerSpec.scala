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

import controllers.declaration.SealController
import controllers.util.{Add, Remove, SaveAndContinue}
import forms.Choice.AllowedChoiceValues.SupplementaryDec
import forms.declaration.Seal
import org.scalatest.concurrent.ScalaFutures
import play.api.test.Helpers._
import uk.gov.hmrc.http.InternalServerException
import unit.base.ControllerSpec
import unit.mock.ErrorHandlerMocks
import views.html.declaration.seal

import scala.concurrent.Await

class SealControllerSpec extends ControllerSpec with ScalaFutures with ErrorHandlerMocks {

  trait SetUp {
    val sealPage = new seal(mainTemplate)

    val controller = new SealController(
      mockAuthAction,
      mockJourneyAction,
      mockErrorHandler,
      mockExportsCacheService,
      stubMessagesControllerComponents(),
      sealPage
    )

    authorizedUser()
    setupErrorHandler()
    withNewCaching(aCacheModel(withChoice(SupplementaryDec)))
  }

  "Package Information Controller" should {

    "return 200 (OK)" when {

      "display page method is invoked and cache is empty" in new SetUp {

        val result = controller.displayForm()(getRequest())

        status(result) must be(OK)
      }

      "display page method is invoked and cache contain some data" in new SetUp {

        withNewCaching(aCacheModel(withSeal(Seal("id"))))

        val result = controller.displayForm()(getRequest())

        status(result) must be(OK)
      }
    }

    "return 400 (BAD_REQUEST)" when {

      "action is incorrect" in new SetUp {

        val body = Seq(("id", "value"), ("wrongAction", ""))

        val result = controller.submitForm()(postRequestAsFormUrlEncoded(body: _*))

        status(result) must be(BAD_REQUEST)
      }

      "user tried to save and continue incorrect item" in new SetUp {

        val body = Seq(("id", "!@#$"), (SaveAndContinue.toString, ""))

        val result = controller.submitForm()(postRequestAsFormUrlEncoded(body: _*))

        status(result) must be(BAD_REQUEST)
      }

      "user tried to add incorrect item" in new SetUp {

        val body = Seq(("id", "!@#$"), (Add.toString, ""))

        val result = controller.submitForm()(postRequestAsFormUrlEncoded(body: _*))

        status(result) must be(BAD_REQUEST)
      }

      "user reached limit of items" in new SetUp {

        val seals = Seq.fill(9999)(Seal("id"))
        withNewCaching(aCacheModel(withSeals(seals)))

        val body = Seq(("id", "value"), (Add.toString, ""))

        val result = controller.submitForm()(postRequestAsFormUrlEncoded(body: _*))

        status(result) must be(BAD_REQUEST)
      }

      "user tried to add duplicated value" in new SetUp {

        withNewCaching(aCacheModel(withSeal(Seal("value"))))

        val body = Seq(("id", "value"), (Add.toString, ""))

        val result = controller.submitForm()(postRequestAsFormUrlEncoded(body: _*))

        status(result) must be(BAD_REQUEST)
      }
    }

    "throw an exception" when {

      "id is incorrect" in new SetUp {

        val body = (Remove.toString, "")

        val caught = intercept[InternalServerException] {
          Await.result(controller.submitForm()(postRequestAsFormUrlEncoded(body)), patienceConfig.timeout)
        }

        assert(caught.getMessage == "Incorrect id")
      }
    }

    "return 303 (SEE_OTHER)" when {

      "user added correct item" in new SetUp {

        withCaching(None)

        val body = Seq(("id", "value"), (Add.toString, ""))

        val result = controller.submitForm()(postRequestAsFormUrlEncoded(body: _*))

        status(result) must be(SEE_OTHER)
      }

      "user clicked save and continue with data in form" in new SetUp {

        val body = Seq("id" -> "value", (SaveAndContinue.toString, ""))

        val result = controller.submitForm()(postRequestAsFormUrlEncoded(body: _*))

        status(result) must be(SEE_OTHER)
      }

      "user clicked save and continue with item in a cache" in new SetUp {

        withNewCaching(aCacheModel(withSeal(Seal("value"))))

        val body = Seq((SaveAndContinue.toString, ""))

        val result = controller.submitForm()(postRequestAsFormUrlEncoded(body: _*))

        status(result) must be(SEE_OTHER)
      }
    }
  }
}
