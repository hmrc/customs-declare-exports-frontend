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
import forms.declaration.DispatchLocation
import forms.declaration.DispatchLocation.AllowedDispatchLocations
import org.mockito.ArgumentMatchers
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, verify}
import play.api.libs.json.{JsObject, JsString, JsValue}
import play.api.test.Helpers._

class DispatchLocationPageControllerSpec extends CustomExportsBaseSpec {

  import DispatchLocationPageControllerSpec._
  private val dispatchLocationUri = uriWithContextPath("/declaration/dispatch-location")

  before {
    authorizedUser()
    withCaching[DispatchLocation](None, DispatchLocation.formId)
    withCaching[Choice](Some(Choice(Choice.AllowedChoiceValues.SupplementaryDec)), choiceId)
  }

  after {
    reset(mockCustomsCacheService)
  }

  "Declaration Type Controller on GET" should {

    "return 200 code" in {

      val result = route(app, getRequest(dispatchLocationUri)).get
      status(result) mustBe OK
    }

    "populate the form fields with data from cache" in {
      withCaching[DispatchLocation](Some(DispatchLocation(AllowedDispatchLocations.OutsideEU)), DispatchLocation.formId)

      val result = route(app, getRequest(dispatchLocationUri)).get
      contentAsString(result) must include("checked=\"checked\"")
    }
  }

  "Declaration Type Controller on POST" should {

    "save the data to the cache" in {

      val validForm = buildDispatchLocationTestData(AllowedDispatchLocations.OutsideEU)
      route(app, postRequest(dispatchLocationUri, validForm)).get.futureValue

      verify(mockCustomsCacheService)
        .cache[DispatchLocation](any(), ArgumentMatchers.eq(DispatchLocation.formId), any())(any(), any(), any())
    }

    "return 303 code" in {

      val validForm = buildDispatchLocationTestData(AllowedDispatchLocations.OutsideEU)
      val result = route(app, postRequest(dispatchLocationUri, validForm)).get

      status(result) mustBe SEE_OTHER
    }

    "redirect to 'Additional Declaration Type' page" when {

      "dispatch location is Outside EU (EX)" in {

        val validForm = buildDispatchLocationTestData(AllowedDispatchLocations.OutsideEU)
        val result = route(app, postRequest(dispatchLocationUri, validForm)).get
        val header = result.futureValue.header

        header.headers.get("Location") must be(Some("/customs-declare-exports/declaration/type"))
      }
    }

    "redirect to 'Not-eligible' page" when {

      "dispatch location is a Special Fiscal Territory (CO)" in {

        val validForm = buildDispatchLocationTestData(AllowedDispatchLocations.SpecialFiscalTerritory)
        val result = route(app, postRequest(dispatchLocationUri, validForm)).get
        val header = result.futureValue.header

        header.headers.get("Location") must be(Some("/customs-declare-exports/declaration/not-eligible"))
      }
    }
  }

}

object DispatchLocationPageControllerSpec {

  def buildDispatchLocationTestData(value: String = ""): JsValue = JsObject(Map("dispatchLocation" -> JsString(value)))
}
