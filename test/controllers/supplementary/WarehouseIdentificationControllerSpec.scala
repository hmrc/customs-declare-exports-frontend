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
import play.api.libs.json.{JsObject, JsString, JsValue}
import play.api.test.Helpers._

class WarehouseIdentificationControllerSpec extends CustomExportsBaseSpec {
  val uri = uriWithContextPath("/declaration/supplementary/warehouse")

  "Warehouse Identification controller" should {
    "display warehouse identification declaration form" in {
      authorizedUser()
      withCaching[WarehouseIdentification](None)

      val result = route(app, getRequest(uri)).get
      val stringResult = contentAsString(result)

      status(result) must be(OK)
      stringResult must include(messages("supplementary.warehouse.title"))
      stringResult must include(messages("supplementary.warehouse.identificationNumber"))
      stringResult must include(messages("supplementary.warehouse.identificationNumber.hint"))
    }
  }

  "validate form - too many characters" in {
    authorizedUser()
    withCaching[WarehouseIdentification](None)

    val incorrectWarehouseIdentification: JsValue =
      JsObject(Map("identificationNumber" -> JsString(TestHelper.randomString(37))))
    val result = route(app, postRequest(uri, incorrectWarehouseIdentification)).get
    val stringResult = contentAsString(result)

    stringResult must include(messages("supplementary.warehouse.identificationNumber.error"))
  }

  "validate form - less than two characters" in {
    authorizedUser()
    withCaching[WarehouseIdentification](None)

    val incorrectWarehouseIdentification: JsValue =
      JsObject(Map("identificationNumber" -> JsString(TestHelper.randomString(1))))
    val result = route(app, postRequest(uri, incorrectWarehouseIdentification)).get
    val stringResult = contentAsString(result)

    stringResult must include(messages("supplementary.warehouse.identificationNumber.error"))
  }

  "validate form - no answers" in {
    authorizedUser()
    withCaching[WarehouseIdentification](None)

    val emptyWarehouseIdentification: JsValue =
      JsObject(Map("identificationNumber" -> JsString("")))
    val result = route(app, postRequest(uri, emptyWarehouseIdentification)).get
    val header = result.futureValue.header

    status(result) must be(SEE_OTHER)

    header.headers.get("Location") must be(Some("/customs-declare-exports/declaration/supplementary/office-of-exit"))
  }

  "validate form - correct values" in {
    authorizedUser()
    withCaching[WarehouseIdentification](None)

    val correctWarehouseIdentification: JsValue =
      JsObject(Map("identificationNumber" -> JsString("R1234567GB")))
    val result = route(app, postRequest(uri, correctWarehouseIdentification)).get
    val header = result.futureValue.header

    status(result) must be(SEE_OTHER)

    header.headers.get("Location") must be(Some("/customs-declare-exports/declaration/supplementary/office-of-exit"))
  }

  "validate form - first letter is not capital" in {
    authorizedUser()
    withCaching[WarehouseIdentification](None)

    val incorrectWarehouseIdentification: JsValue =
      JsObject(Map("identificationNumber" -> JsString("r1234567GB")))
    val result = route(app, postRequest(uri, incorrectWarehouseIdentification)).get
    val stringResult = contentAsString(result)

    stringResult must include(messages("supplementary.warehouse.identificationNumber.error"))
  }

}
