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
import forms.declaration.additionaldeclarationtype.AdditionalDeclarationTypeSupplementaryDec.AllowedAdditionalDeclarationTypes
import forms.declaration.additionaldeclarationtype.{AdditionalDeclarationType, AdditionalDeclarationTypeSupplementaryDec}
import helpers.views.declaration.DeclarationTypeMessages
import org.mockito.ArgumentMatchers
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, verify}
import play.api.libs.json.{JsObject, JsString, JsValue}
import play.api.test.Helpers._

class DeclarationTypeControllerSpec extends CustomExportsBaseSpec with DeclarationTypeMessages {

  import DeclarationTypeControllerSpec._
  private val dispatchLocationUri = uriWithContextPath("/declaration/dispatch-location")
  private val declarationTypeUri = uriWithContextPath("/declaration/type")

  before {
    authorizedUser()
    withCaching[AdditionalDeclarationType](None, AdditionalDeclarationTypeSupplementaryDec.formId)
    withCaching[DispatchLocation](None, DispatchLocation.formId)
    withCaching[Choice](Some(Choice(Choice.AllowedChoiceValues.SupplementaryDec)), choiceId)
  }

  after {
    reset(mockCustomsCacheService)
  }

  "Declaration Type Controller for dispatch location on GET" should {

    "return 200 code" in {

      val result = route(app, getRequest(dispatchLocationUri)).get
      status(result) must be(OK)
    }

    "populate the form fields with data from cache" in {
      withCaching[DispatchLocation](Some(DispatchLocation(AllowedDispatchLocations.OutsideEU)), DispatchLocation.formId)

      val result = route(app, getRequest(dispatchLocationUri)).get
      contentAsString(result) must include("checked=\"checked\"")
    }
  }

  "Declaration Type Controller for dispatch location on POST" should {

    "save the data to the cache" in {

      val validForm = buildDispatchLocationTestData(AllowedDispatchLocations.OutsideEU)
      route(app, postRequest(dispatchLocationUri, validForm)).get.futureValue

      verify(mockCustomsCacheService)
        .cache[DispatchLocation](any(), ArgumentMatchers.eq(DispatchLocation.formId), any())(any(), any(), any())
    }

    "return 303 code" in {

      val validForm = buildDispatchLocationTestData(AllowedDispatchLocations.OutsideEU)
      val result = route(app, postRequest(dispatchLocationUri, validForm)).get

      status(result) must be(SEE_OTHER)
    }

    "redirect to 'Additional Declaration Type' page" when {

      "dispatch location is Outside EU (EX)" in {
        withCaching[DispatchLocation](None, DispatchLocation.formId)

        val validForm = buildDispatchLocationTestData(AllowedDispatchLocations.OutsideEU)
        val result = route(app, postRequest(dispatchLocationUri, validForm)).get
        val header = result.futureValue.header

        header.headers.get("Location") must be(Some("/customs-declare-exports/declaration/type"))
      }
    }

    "redirect to 'Not-eligible' page" when {

      "dispatch location is a Special Fiscal Territory (CO)" in {
        withCaching[DispatchLocation](None, DispatchLocation.formId)

        val validForm = buildDispatchLocationTestData(AllowedDispatchLocations.SpecialFiscalTerritory)
        val result = route(app, postRequest(dispatchLocationUri, validForm)).get
        val header = result.futureValue.header

        header.headers.get("Location") must be(Some("/customs-declare-exports/declaration/not-eligible"))
      }
    }
  }

  "Declaration Type Controller for declaration type on GET" should {

    "return 200 code" in {

      val result = route(app, getRequest(declarationTypeUri)).get
      status(result) must be(OK)
    }

    "populate the form fields with data from cache" in {

      withCaching[AdditionalDeclarationType](
        Some(AdditionalDeclarationType(AllowedAdditionalDeclarationTypes.Simplified)),
        AdditionalDeclarationTypeSupplementaryDec.formId
      )

      val result = route(app, getRequest(declarationTypeUri)).get
      contentAsString(result) must include("checked=\"checked\"")
    }
  }

  "Declaration Type Controller on declaration type on POST" should {

    "save the data to the cache" in {

      val validForm = buildAdditionalDeclarationTypeTestData(AllowedAdditionalDeclarationTypes.Simplified)
      route(app, postRequest(declarationTypeUri, validForm)).get.futureValue

      verify(mockCustomsCacheService)
        .cache[AdditionalDeclarationType](any(), ArgumentMatchers.eq(AdditionalDeclarationTypeSupplementaryDec.formId), any())(
          any(),
          any(),
          any()
        )
    }

    "return 303 code" in {
      val validForm = buildAdditionalDeclarationTypeTestData(AllowedAdditionalDeclarationTypes.Simplified)
      val result = route(app, postRequest(declarationTypeUri, validForm)).get

      status(result) must be(SEE_OTHER)
    }

    "redirect to 'Consignment references' page" in {

      val validForm = buildAdditionalDeclarationTypeTestData(AllowedAdditionalDeclarationTypes.Simplified)
      val result = route(app, postRequest(declarationTypeUri, validForm)).get
      val header = result.futureValue.header

      header.headers.get("Location") must be(Some("/customs-declare-exports/declaration/consignment-references"))
    }
  }
}

object DeclarationTypeControllerSpec {

  def buildDispatchLocationTestData(value: String = ""): JsValue = JsObject(Map("dispatchLocation" -> JsString(value)))

  def buildAdditionalDeclarationTypeTestData(value: String = ""): JsValue = JsObject(
    Map("additionalDeclarationType" -> JsString(value))
  )
}
