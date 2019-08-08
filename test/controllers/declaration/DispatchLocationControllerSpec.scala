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
import forms.Choice.AllowedChoiceValues.SupplementaryDec
import forms.declaration.DispatchLocation
import forms.declaration.DispatchLocation.AllowedDispatchLocations
import models.ExportsDeclaration
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, times, verify}
import play.api.libs.json.{JsObject, JsString, JsValue}
import play.api.test.Helpers._

class DispatchLocationControllerSpec extends CustomExportsBaseSpec {

  import DispatchLocationControllerSpec._

  private val dispatchLocationUri = uriWithContextPath("/declaration/dispatch-location")

  override def beforeEach() {
    authorizedUser()
    withNewCaching(aDeclaration(withChoice(SupplementaryDec)))
  }

  override def afterEach() {
    reset(mockExportsCacheService)
  }

  "Declaration Type Controller on GET" should {

    "return 200 code" in {

      val result = route(app, getRequest(dispatchLocationUri)).get
      status(result) must be(OK)
      verify(mockExportsCacheService, times(2)).get(any())
    }

    "populate the form fields with data from cache" in {
      withNewCaching(aDeclaration(withChoice("SMP"), withDispatchLocation(AllowedDispatchLocations.OutsideEU)))

      val result = route(app, getRequest(dispatchLocationUri)).get
      contentAsString(result) must include("checked=\"checked\"")
      verify(mockExportsCacheService, times(2)).get(any())
    }
  }

  "Declaration Type Controller on POST" should {

    "save the data to the cache" in {

      val validForm = buildDispatchLocationTestData(AllowedDispatchLocations.OutsideEU)
      route(app, postRequest(dispatchLocationUri, validForm)).get.futureValue

      verify(mockExportsCacheService).update(any(), any[ExportsDeclaration])
      verify(mockExportsCacheService, times(2)).get(any())
    }

    "return 303 code" in {

      val validForm = buildDispatchLocationTestData(AllowedDispatchLocations.OutsideEU)
      val result = route(app, postRequest(dispatchLocationUri, validForm)).get

      status(result) must be(SEE_OTHER)
      theCacheModelUpdated.dispatchLocation must be(Some(DispatchLocation(AllowedDispatchLocations.OutsideEU)))
    }

    "redirect to 'Additional Declaration Type' page" when {

      "dispatch location is Outside EU (EX)" in {

        val validForm = buildDispatchLocationTestData(AllowedDispatchLocations.OutsideEU)
        val result = route(app, postRequest(dispatchLocationUri, validForm)).get

        redirectLocation(result) must be(Some("/customs-declare-exports/declaration/type"))
        theCacheModelUpdated.dispatchLocation must be(Some(DispatchLocation(AllowedDispatchLocations.OutsideEU)))
      }
    }

    "redirect to 'Not-eligible' page" when {

      "dispatch location is a Special Fiscal Territory (CO)" in {

        val validForm = buildDispatchLocationTestData(AllowedDispatchLocations.SpecialFiscalTerritory)
        val result = route(app, postRequest(dispatchLocationUri, validForm)).get

        redirectLocation(result) must be(Some("/customs-declare-exports/declaration/not-eligible"))
        theCacheModelUpdated.dispatchLocation must be(
          Some(DispatchLocation(AllowedDispatchLocations.SpecialFiscalTerritory))
        )
      }
    }
  }

}

object DispatchLocationControllerSpec {

  def buildDispatchLocationTestData(value: String = ""): JsValue = JsObject(Map("dispatchLocation" -> JsString(value)))
}
