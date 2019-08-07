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

package controllers.declaration

import base.CustomExportsBaseSpec
import forms.Choice
import forms.declaration.OfficeOfExitSupplementarySpec._
import forms.declaration.officeOfExit.OfficeOfExit
import helpers.views.declaration.OfficeOfExitMessages
import org.mockito.Mockito.reset
import play.api.libs.json.{JsObject, JsString, JsValue}
import play.api.test.Helpers._
import services.cache.ExportsCacheModel

class OfficeOfExitControllerSpec extends CustomExportsBaseSpec with OfficeOfExitMessages {

  private val uri: String = uriWithContextPath("/declaration/office-of-exit")

  override def beforeEach(): Unit =
    authorizedUser()

  override def afterEach() {
    reset(mockExportsCacheService)
  }

  trait SupplementarySetUp {
    val exampleModel: ExportsCacheModel = aCacheModel(withChoice(Choice.AllowedChoiceValues.SupplementaryDec))
    withNewCaching(exampleModel)
  }

  trait StandardSetUp {
    val exampleModel: ExportsCacheModel = aCacheModel(withChoice(Choice.AllowedChoiceValues.StandardDec))
    withNewCaching(exampleModel)
  }

  "Office Of Exit Controller during supplementary declaration on GET" should {

    "return 200 with a success" in new SupplementarySetUp {

      val Some(result) = route(app, getRequest(uri, sessionId = exampleModel.sessionId))

      status(result) must be(OK)
    }

    "read item from cache and display it" in new SupplementarySetUp {

      val model: ExportsCacheModel = aCacheModel(
        withChoice(Choice.AllowedChoiceValues.SupplementaryDec),
        withOfficeOfExit(officeId = "999AAA45")
      )
      withNewCaching(model)

      val Some(result) = route(app, getRequest(uri, sessionId = model.sessionId))

      status(result) must be(OK)
      contentAsString(result) must include("999AAA45")
    }
  }

  "Office Of Exit Controller during standard declaration on GET" should {

    "return 200 with a success" in new StandardSetUp {

      val Some(result) = route(app, getRequest(uri, sessionId = exampleModel.sessionId))

      status(result) must be(OK)
    }

    "read item from cache and display it" in new StandardSetUp {
      val officeId = "12345678"
      val presentationOfficeId = "87654321"
      val circumstancesCode = "Yes"
      private val model: ExportsCacheModel = aCacheModel(
        withChoice(Choice.AllowedChoiceValues.StandardDec),
        withOfficeOfExit(officeId, Some(presentationOfficeId), Some(circumstancesCode))
      )
      withNewCaching(model)

      val Some(result) = route(app, getRequest(uri, sessionId = model.sessionId))
      val page = contentAsString(result)

      status(result) must be(OK)
      page must include(officeId)
      page must include(presentationOfficeId)
      page must include(circumstancesCode)
    }
  }

  "Office Of Exit Controller during supplementary declaration on POST" should {

    "return Bad Request for incorrect values" in new SupplementarySetUp {

      val Some(result) = route(app, postRequest(uri, incorrectOfficeOfExitJSON, sessionId = exampleModel.sessionId))

      status(result) must be(BAD_REQUEST)
      contentAsString(result) must include(messages(officeOfExitLength))
      verifyTheCacheIsUnchanged()
    }

    "return Bad Request for empty form" in new SupplementarySetUp {

      val Some(result) = route(app, postRequest(uri, emptyOfficeOfExitJSON, sessionId = exampleModel.sessionId))

      status(result) must be(BAD_REQUEST)
      contentAsString(result) must include(messages(officeOfExitEmpty))
      verifyTheCacheIsUnchanged()
    }

    "redirect to Total Numbers of Items page for correct values" in new SupplementarySetUp {

      val Some(result) = route(app, postRequest(uri, correctOfficeOfExitJSON, sessionId = exampleModel.sessionId))

      status(result) must be(SEE_OTHER)
      redirectLocation(result) must be(Some("/customs-declare-exports/declaration/total-numbers-of-items"))
      theCacheModelUpdated.locations.officeOfExit.get mustBe OfficeOfExit("123qwe12", None, None)
    }
  }

  "Office Of Exit Controller during standard declaration on POST" should {
    "return Bad Request for incorrect values" in new StandardSetUp {
      val incorrectOfficeOfExit: JsValue = JsObject(
        Map(
          "officeId" -> JsString("office"),
          "presentationOfficeId" -> JsString("presentationOfficeId"),
          "circumstancesCode" -> JsString("circumnstancesCode")
        )
      )

      val Some(result) = route(app, postRequest(uri, incorrectOfficeOfExit, sessionId = exampleModel.sessionId))

      status(result) must be(BAD_REQUEST)
      verifyTheCacheIsUnchanged()
    }

    "return Bad Request for empty form" in new StandardSetUp {
      val emptyOfficeOfExit: JsValue = JsObject(
        Map("officeId" -> JsString(""), "presentationOfficeId" -> JsString(""), "circumstancesCode" -> JsString(""))
      )

      val Some(result) = route(app, postRequest(uri, emptyOfficeOfExit, sessionId = exampleModel.sessionId))

      status(result) must be(BAD_REQUEST)
      verifyTheCacheIsUnchanged()
    }

    "redirect to Total Numbers of Items page" in new StandardSetUp {
      val correctOfficeOfExit: JsValue = JsObject(
        Map(
          "officeId" -> JsString("12345678"),
          "presentationOfficeId" -> JsString("12345678"),
          "circumstancesCode" -> JsString("Yes")
        )
      )

      val Some(result) = route(app, postRequest(uri, correctOfficeOfExit, sessionId = exampleModel.sessionId))

      status(result) must be(SEE_OTHER)
      redirectLocation(result) must be(Some("/customs-declare-exports/declaration/total-numbers-of-items"))
      theCacheModelUpdated.locations.officeOfExit.get mustBe OfficeOfExit("12345678", Some("12345678"), Some("Yes"))
    }
  }
}
