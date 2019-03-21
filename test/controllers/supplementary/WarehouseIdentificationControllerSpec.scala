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

package controllers.supplementary

import base.{CustomExportsBaseSpec, TestHelper}
import forms.supplementary.WarehouseIdentification
import forms.supplementary.WarehouseIdentificationSpec._
import org.scalatest.BeforeAndAfter
import play.api.libs.json.{JsObject, JsString, JsValue}
import play.api.test.Helpers._

class WarehouseIdentificationControllerSpec extends CustomExportsBaseSpec with BeforeAndAfter {
  val uri = uriWithContextPath("/declaration/warehouse")

  before {
    authorizedUser()
  }

  "Warehouse Identification Controller on display" should {

    "display warehouse identification declaration form" in {
      withCaching[WarehouseIdentification](None)

      val result = route(app, getRequest(uri)).get
      val stringResult = contentAsString(result)

      status(result) must be(OK)
      stringResult must include(messages("supplementary.warehouse.title"))
      stringResult must include(messages("supplementary.warehouse.identificationNumber"))
      stringResult must include(messages("supplementary.warehouse.identificationNumber.hint"))
    }

    "display \"Save and continue\" button on page" in {

      val result = route(app, getRequest(uri)).get
      val resultAsString = contentAsString(result)

      resultAsString must include(messages("site.save_and_continue"))
      resultAsString must include("button id=\"submit\" class=\"button\"")
    }

    "display \"Back\" button that links to \"Supervising Office\" page" in {
      val result = route(app, getRequest(uri)).get

      contentAsString(result) must include(messages("site.back"))
      contentAsString(result) must include("/declaration/supervising-office")
    }
  }

  "Warehouse Identification Controller on form" should {

    "validate form - too many characters" in {
      withCaching[WarehouseIdentification](None)

      val incorrectWarehouseIdentification: JsValue =
        JsObject(Map("identificationNumber" -> JsString(TestHelper.createRandomString(37))))
      val result = route(app, postRequest(uri, incorrectWarehouseIdentification)).get
      val stringResult = contentAsString(result)

      stringResult must include(messages("supplementary.warehouse.identificationNumber.error"))
    }

    "validate form - less than two characters" in {
      withCaching[WarehouseIdentification](None)

      val incorrectWarehouseIdentification: JsValue =
        JsObject(Map("identificationNumber" -> JsString(TestHelper.createRandomString(1))))
      val result = route(app, postRequest(uri, incorrectWarehouseIdentification)).get
      val stringResult = contentAsString(result)

      stringResult must include(messages("supplementary.warehouse.identificationNumber.error"))
    }

    "validate form - first letter is not capital" in {
      withCaching[WarehouseIdentification](None)

      val incorrectWarehouseIdentification: JsValue =
        JsObject(Map("identificationNumber" -> JsString("r1234567GB")))
      val result = route(app, postRequest(uri, incorrectWarehouseIdentification)).get
      val stringResult = contentAsString(result)

      stringResult must include(messages("supplementary.warehouse.identificationNumber.error"))
    }

    "validate form - no answers" in {
      withCaching[WarehouseIdentification](None)

      val result = route(app, postRequest(uri, emptyWarehouseIdentificationJSON)).get
      val header = result.futureValue.header

      status(result) must be(SEE_OTHER)

      header.headers.get("Location") must be(Some("/customs-declare-exports/declaration/export-items"))
    }

    "validate form - correct values" in {
      withCaching[WarehouseIdentification](None)

      val result = route(app, postRequest(uri, correctWarehouseIdentificationJSON)).get
      val header = result.futureValue.header

      status(result) must be(SEE_OTHER)

      header.headers.get("Location") must be(Some("/customs-declare-exports/declaration/export-items"))
    }
  }
}
