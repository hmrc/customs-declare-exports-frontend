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
import forms.Choice.choiceId
import forms.declaration.OfficeOfExitSupplementarySpec._
import forms.declaration.officeOfExit.{OfficeOfExit, OfficeOfExitForms, OfficeOfExitStandard, OfficeOfExitSupplementary}
import helpers.views.declaration.OfficeOfExitMessages
import models.declaration.Locations
import org.mockito.Mockito.reset
import play.api.libs.json.{JsObject, JsString, JsValue}
import play.api.test.Helpers._

class OfficeOfExitControllerSpec extends CustomExportsBaseSpec with OfficeOfExitMessages {

  private val uri: String = uriWithContextPath("/declaration/office-of-exit")

  override def beforeEach(): Unit = {
    authorizedUser()
    withNewCaching(createModelWithNoItems())
  }

  override def afterEach() {
    reset(mockExportsCacheService)
  }

  trait SupplementarySetUp {
    withCaching[OfficeOfExitSupplementary](None)
    withCaching[Choice](Some(Choice(Choice.AllowedChoiceValues.SupplementaryDec)), choiceId)
  }

  trait StandardSetUp {
    withCaching[OfficeOfExitStandard](None)
    withCaching[Choice](Some(Choice(Choice.AllowedChoiceValues.StandardDec)), choiceId)
  }

  "Office Of Exit Controller during supplementary declaration on GET" should {

    "return 200 with a success" in new SupplementarySetUp {

      val result = route(app, getRequest(uri)).get

      status(result) must be(OK)
    }

    "read item from cache and display it" in new SupplementarySetUp {

      val cachedData = OfficeOfExitSupplementary("999AAA45")
      withNewCaching(
        createModelWithNoItems().copy(locations = Locations(officeOfExit = Some(OfficeOfExit.from(cachedData))))
      )

      val result = route(app, getRequest(uri)).get

      status(result) must be(OK)
      contentAsString(result) must include("999AAA45")
    }
  }

  "Office Of Exit Controller during standard declaration on GET" should {

    "return 200 with a success" in new StandardSetUp {

      val result = route(app, getRequest(uri)).get

      status(result) must be(OK)
    }

    "read item from cache and display it" in new StandardSetUp {
      val officeId = "12345678"
      val presentationOfficeId = "87654321"
      val circumstancesCode = "Yes"
      val cachedData = OfficeOfExitStandard(officeId, presentationOfficeId, circumstancesCode)
      withNewCaching(
        createModelWithNoItems().copy(locations = Locations(officeOfExit = Some(OfficeOfExit.from(cachedData))))
      )

      val result = route(app, getRequest(uri)).get
      val page = contentAsString(result)

      status(result) must be(OK)
      page must include(officeId)
      page must include(presentationOfficeId)
      page must include(circumstancesCode)
    }
  }

  "Office Of Exit Controller during supplementary declaration on POST" should {

    "return Bad Request for incorrect values" in new SupplementarySetUp {

      val result = route(app, postRequest(uri, incorrectOfficeOfExitJSON)).get

      status(result) must be(BAD_REQUEST)
      contentAsString(result) must include(messages(officeOfExitLength))
      verifyTheCacheIsUnchanged()
    }

    "return Bad Request for empty form" in new SupplementarySetUp {

      val result = route(app, postRequest(uri, emptyOfficeOfExitJSON)).get

      status(result) must be(BAD_REQUEST)
      contentAsString(result) must include(messages(officeOfExitEmpty))
      verifyTheCacheIsUnchanged()
    }

    "redirect to Total Numbers of Items page for correct values" in new SupplementarySetUp {

      val result = route(app, postRequest(uri, correctOfficeOfExitJSON)).get
      val header = result.futureValue.header

      status(result) must be(SEE_OTHER)
      header.headers.get("Location") must be(Some("/customs-declare-exports/declaration/total-numbers-of-items"))
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

      val result = route(app, postRequest(uri, incorrectOfficeOfExit)).get

      status(result) must be(BAD_REQUEST)
      verifyTheCacheIsUnchanged()
    }

    "return Bad Request for empty form" in {
      val emptyOfficeOfExit: JsValue = JsObject(
        Map("officeId" -> JsString(""), "presentationOfficeId" -> JsString(""), "circumstancesCode" -> JsString(""))
      )

      val result = route(app, postRequest(uri, emptyOfficeOfExit)).get

      status(result) must be(BAD_REQUEST)
      verifyTheCacheIsUnchanged()
    }

    "redirect to Total Numbers of Items page" in {
      val correctOfficeOfExit: JsValue = JsObject(
        Map(
          "officeId" -> JsString("12345678"),
          "presentationOfficeId" -> JsString("12345678"),
          "circumstancesCode" -> JsString("Yes")
        )
      )

      val result = route(app, postRequest(uri, correctOfficeOfExit)).get
      val header = result.futureValue.header

      status(result) must be(SEE_OTHER)
      header.headers.get("Location") must be(Some("/customs-declare-exports/declaration/total-numbers-of-items"))
      theCacheModelUpdated.locations.officeOfExit.get mustBe OfficeOfExit("12345678", Some("12345678"), Some("Yes"))
    }
  }
}
