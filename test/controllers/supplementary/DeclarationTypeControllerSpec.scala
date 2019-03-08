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

import base.CustomExportsBaseSpec
import forms.supplementary.AdditionalDeclarationType.AllowedAdditionalDeclarationTypes
import forms.supplementary.AdditionalDeclarationType.AllowedAdditionalDeclarationTypes._
import forms.supplementary.DispatchLocation.AllowedDispatchLocations
import forms.supplementary.{AdditionalDeclarationType, DispatchLocation}
import org.mockito.ArgumentMatchers
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, verify}
import org.scalatest.BeforeAndAfter
import play.api.libs.json.{JsObject, JsString, JsValue}
import play.api.mvc.Result
import play.api.test.Helpers._

import scala.concurrent.Future

class DeclarationTypeControllerSpec extends CustomExportsBaseSpec with BeforeAndAfter {

  import DeclarationTypeControllerSpec._
  private val dispatchLocationUri = uriWithContextPath("/declaration/supplementary/dispatch-location")
  private val declarationTypeUri = uriWithContextPath("/declaration/supplementary/type")

  before {
    authorizedUser()
  }

  "Declaration Type Controller on dispatch location page" should {

    "return 200 code" in {
      val result = displayDispatchLocationPageTestScenario()
      status(result) must be(OK)
    }

    "populate the form fields with data from cache" in {
      val result = displayDispatchLocationPageTestScenario(Some(DispatchLocation(AllowedDispatchLocations.OutsideEU)))
      contentAsString(result) must include("checked=\"checked\"")
    }

    def displayDispatchLocationPageTestScenario(cacheValue: Option[DispatchLocation] = None): Future[Result] = {
      withCaching[DispatchLocation](cacheValue, DispatchLocation.formId)
      route(app, getRequest(dispatchLocationUri)).get
    }
  }

  "Declaration Type Controller on submit dispatch location" should {

    "display the form page with error" when {

      "no value provided for dispatch location" in {
        withCaching[DispatchLocation](None, DispatchLocation.formId)

        val formWithoutDeclarationType = buildDispatchLocationTestData()
        val result = route(app, postRequest(dispatchLocationUri, formWithoutDeclarationType)).get

        contentAsString(result) must include(messages("supplementary.dispatchLocation.inputText.error.empty"))
      }
    }

    "save the data to the cache" in {
      reset(mockCustomsCacheService)
      withCaching[DispatchLocation](None, DispatchLocation.formId)

      val validForm = buildDispatchLocationTestData(AllowedDispatchLocations.OutsideEU)
      route(app, postRequest(dispatchLocationUri, validForm)).get.futureValue

      verify(mockCustomsCacheService)
        .cache[DispatchLocation](any(), ArgumentMatchers.eq(DispatchLocation.formId), any())(any(), any(), any())
    }

    "return 303 code" in {
      withCaching[DispatchLocation](None, DispatchLocation.formId)

      val validForm = buildDispatchLocationTestData(AllowedDispatchLocations.OutsideEU)
      val result = route(app, postRequest(dispatchLocationUri, validForm)).get

      status(result) must be(SEE_OTHER)
    }

    "redirect to \"Additional Declaration Type\" page" when {

      "dispatch location is Outside EU (EX)" in {
        withCaching[DispatchLocation](None, DispatchLocation.formId)

        val validForm = buildDispatchLocationTestData(AllowedDispatchLocations.OutsideEU)
        val result = route(app, postRequest(dispatchLocationUri, validForm)).get
        val header = result.futureValue.header

        header.headers.get("Location") must be(Some("/customs-declare-exports/declaration/supplementary/type"))
      }
    }

    "redirect to \"Not-eligible\" page" when {

      "dispatch location is a Special Fiscal Territory (CO)" in {
        withCaching[DispatchLocation](None, DispatchLocation.formId)

        val validForm = buildDispatchLocationTestData(AllowedDispatchLocations.SpecialFiscalTerritory)
        val result = route(app, postRequest(dispatchLocationUri, validForm)).get
        val header = result.futureValue.header

        header.headers.get("Location") must be(Some("/customs-declare-exports/declaration/supplementary/not-eligible"))
      }
    }
  }

  "Declaration Type Controller on display declaration type page" should {

    "return 200 code" in {
      val result = displayDeclarationTypePageTestScenario()
      status(result) must be(OK)
    }

    "populate the form fields with data from cache" in {
      val result = displayDeclarationTypePageTestScenario(Some(AdditionalDeclarationType(Simplified)))
      contentAsString(result) must include("checked=\"checked\"")
    }

    def displayDeclarationTypePageTestScenario(cacheValue: Option[AdditionalDeclarationType] = None): Future[Result] = {
      withCaching[AdditionalDeclarationType](cacheValue, AdditionalDeclarationType.formId)
      route(app, getRequest(declarationTypeUri)).get
    }
  }

  "Declaration Type Controller on submit declaration type page" should {

    "display the form page with error" when {

      "no value provided for declaration type" in {
        withCaching[AdditionalDeclarationType](None, AdditionalDeclarationType.formId)

        val formWithoutAdditionalDeclarationType = buildAdditionalDeclarationTypeTestData()
        val result = route(app, postRequest(declarationTypeUri, formWithoutAdditionalDeclarationType)).get

        contentAsString(result) must include(messages("supplementary.declarationType.inputText.error.empty"))
      }
    }

    "save the data to the cache" in {
      reset(mockCustomsCacheService)
      withCaching[AdditionalDeclarationType](None, AdditionalDeclarationType.formId)

      val validForm = buildAdditionalDeclarationTypeTestData(AllowedAdditionalDeclarationTypes.Simplified)
      route(app, postRequest(declarationTypeUri, validForm)).get.futureValue

      verify(mockCustomsCacheService)
        .cache[AdditionalDeclarationType](any(), ArgumentMatchers.eq(AdditionalDeclarationType.formId), any())(
          any(),
          any(),
          any()
        )
    }

    "return 303 code" in {
      withCaching[AdditionalDeclarationType](None, AdditionalDeclarationType.formId)

      val validForm = buildAdditionalDeclarationTypeTestData(AllowedAdditionalDeclarationTypes.Simplified)
      val result = route(app, postRequest(declarationTypeUri, validForm)).get

      status(result) must be(SEE_OTHER)
    }

    "redirect to \"Consignment references\" page" in {
      withCaching[AdditionalDeclarationType](None, AdditionalDeclarationType.formId)

      val validForm = buildAdditionalDeclarationTypeTestData(AllowedAdditionalDeclarationTypes.Simplified)
      val result = route(app, postRequest(declarationTypeUri, validForm)).get
      val header = result.futureValue.header

      header.headers.get("Location") must be(
        Some("/customs-declare-exports/declaration/supplementary/consignment-references")
      )
    }
  }
}

object DeclarationTypeControllerSpec {

  def buildDispatchLocationTestData(value: String = ""): JsValue = JsObject(Map("dispatchLocation" -> JsString(value)))

  def buildAdditionalDeclarationTypeTestData(value: String = ""): JsValue = JsObject(
    Map("additionalDeclarationType" -> JsString(value))
  )
}
