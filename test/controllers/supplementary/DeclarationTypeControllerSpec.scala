/*
 * Copyright 2018 HM Revenue & Customs
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

import base.{CustomExportsBaseSpec, ExportsTestData}
import forms.supplementary.DeclarationType
import org.mockito.ArgumentMatchers
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, verify}
import org.scalatest.BeforeAndAfter
import play.api.libs.json.{JsObject, JsString}
import play.api.test.Helpers._

class DeclarationTypeControllerSpec extends CustomExportsBaseSpec with BeforeAndAfter {

  private val dispatchLocationUri = uriWithContextPath("/declaration/supplementary/dispatch-location")
  private val declarationTypeUri = uriWithContextPath("/declaration/supplementary/type")

  before {
    authorizedUser()
  }


  "DeclarationTypeController on displayDispatchLocationPage" should {
    "return 200 code" in {
      withCaching[DeclarationType](None, DeclarationType.formId)

      val result = route(app, getRequest(dispatchLocationUri)).get

      status(result) must be(OK)
    }

    "display page title" in {
      withCaching[DeclarationType](None, DeclarationType.formId)

      val result = route(app, getRequest(dispatchLocationUri)).get

      contentAsString(result) must include(messages("supplementary.dispatchLocation.title"))
    }

    "display \"back\" button that links to \"What do you want to do\" page" in {
      withCaching[DeclarationType](None, DeclarationType.formId)

      val result = route(app, getRequest(dispatchLocationUri)).get

      contentAsString(result) must include(messages("site.back"))
      contentAsString(result) must include("choice")
    }

    "display page header with hint" in {
      withCaching[DeclarationType](None, DeclarationType.formId)

      val result = route(app, getRequest(dispatchLocationUri)).get

      contentAsString(result) must include(messages("supplementary.dispatchLocation.header"))
      contentAsString(result) must include(messages("supplementary.dispatchLocation.header.hint"))
    }

    "display radio button with question text for declaration type" in {
      withCaching[DeclarationType](None, DeclarationType.formId)

      val result = route(app, getRequest(dispatchLocationUri)).get

      contentAsString(result) must include(
        messages("supplementary.dispatchLocation.inputText.outsideEU"))
      contentAsString(result) must include(
        messages("supplementary.dispatchLocation.inputText.specialFiscalTerritory"))
    }

    "display \"Save and continue\" button" in {
      withCaching[DeclarationType](None, DeclarationType.formId)

      val result = route(app, getRequest(dispatchLocationUri)).get

      contentAsString(result) must include(messages("site.save_and_continue"))
      contentAsString(result) must include("button id=\"submit\" class=\"button\"")
    }

    "not populate the form fields if cache is empty" in {
      withCaching[DeclarationType](None, DeclarationType.formId)

      val result = route(app, getRequest(dispatchLocationUri)).get

      contentAsString(result) mustNot include("checked=\"checked\"")
    }

    "populate the form fields with data from cache" in {
      withCaching[DeclarationType](Some(DeclarationType("EX", "")), DeclarationType.formId)

      val result = route(app, getRequest(dispatchLocationUri)).get

      contentAsString(result) must include("checked=\"checked\"")
    }
  }

  "DeclarationTypeController on submitDispatchLocation" should {

    "display the form page with error" when {
      "no value provided for dipatch location" in {
        withCaching[DeclarationType](None)

        val formWithoutDeclarationType = JsObject(Map("additionalDeclarationType" -> JsString("Y")))
        val result = route(app, postRequest(dispatchLocationUri, formWithoutDeclarationType)).get

        contentAsString(result) must include(messages("supplementary.dispatchLocation.inputText.errorMessage"))
      }
    }

    "save the data to the cache" in {
      reset(mockCustomsCacheService)
      withCaching[DeclarationType](None)

      val validForm = ExportsTestData.correctDeclarationType
      route(app, postRequest(dispatchLocationUri, validForm)).get.futureValue

      verify(mockCustomsCacheService)
        .cache[DeclarationType](any(), ArgumentMatchers.eq(DeclarationType.formId), any())(any(), any(), any())
    }

    "return 303 code" in {
      withCaching[DeclarationType](None)

      val validForm = ExportsTestData.correctDeclarationType
      val result = route(app, postRequest(dispatchLocationUri, validForm)).get

      status(result) must be(SEE_OTHER)
    }

    "redirect to \"Additional Declaration Type\" page" in {
      withCaching[DeclarationType](None)

      val validForm = ExportsTestData.correctDeclarationType
      val result = route(app, postRequest(dispatchLocationUri, validForm)).get
      val header = result.futureValue.header

      header.headers.get("Location") must be(
        Some("/customs-declare-exports/declaration/supplementary/type"))
    }
  }



  "DeclarationTypeController on displayDeclarationTypePage" should {
    "return 200 code" in {
      withCaching[DeclarationType](None, DeclarationType.formId)

      val result = route(app, getRequest(declarationTypeUri)).get

      status(result) must be(OK)
    }

    "display page title" in {
      withCaching[DeclarationType](None, DeclarationType.formId)

      val result = route(app, getRequest(declarationTypeUri)).get

      contentAsString(result) must include(messages("supplementary.declarationType.title"))
    }

    "display \"back\" button that links to \"Dispatch Location\" page" in {
      withCaching[DeclarationType](None, DeclarationType.formId)

      val result = route(app, getRequest(declarationTypeUri)).get

      contentAsString(result) must include(messages("site.back"))
      contentAsString(result) must include("dispatch-location")
    }

    "display page header with hint" in {
      withCaching[DeclarationType](None, DeclarationType.formId)

      val result = route(app, getRequest(declarationTypeUri)).get

      contentAsString(result) must include(messages("supplementary.declarationType.header"))
      contentAsString(result) must include(messages("supplementary.declarationType.header.hint"))
    }

    "display radio button with question text for declaration type" in {
      withCaching[DeclarationType](None, DeclarationType.formId)

      val result = route(app, getRequest(declarationTypeUri)).get

      contentAsString(result) must include(
        messages("supplementary.declarationType.inputText.simplified"))
      contentAsString(result) must include(
        messages("supplementary.declarationType.inputText.standard"))
    }

    "display \"Save and continue\" button" in {
      withCaching[DeclarationType](None, DeclarationType.formId)

      val result = route(app, getRequest(declarationTypeUri)).get

      contentAsString(result) must include(messages("site.save_and_continue"))
      contentAsString(result) must include("button id=\"submit\" class=\"button\"")
    }

    "not populate the form fields if cache is empty" in {
      withCaching[DeclarationType](None, DeclarationType.formId)

      val result = route(app, getRequest(declarationTypeUri)).get

      contentAsString(result) mustNot include("checked=\"checked\"")
    }

    "populate the form fields with data from cache" in {
      withCaching[DeclarationType](Some(DeclarationType("", "Y")), DeclarationType.formId)

      val result = route(app, getRequest(declarationTypeUri)).get

      contentAsString(result) must include("checked=\"checked\"")
    }
  }

  "DeclarationTypeController on submitDeclarationTypePage" should {

    "display the form page with error" when {
      "no value provided for declaration type" in {
        withCaching[DeclarationType](None)

        val formWithoutAdditionalDeclarationType = JsObject(Map("declarationType" -> JsString("EX")))
        val result = route(app, postRequest(declarationTypeUri, formWithoutAdditionalDeclarationType)).get

        contentAsString(result) must include(messages("supplementary.declarationType.inputText.errorMessage"))
      }
    }

    "save the data to the cache" in {
      reset(mockCustomsCacheService)
      withCaching[DeclarationType](None)

      val validForm = ExportsTestData.correctDeclarationType
      route(app, postRequest(declarationTypeUri, validForm)).get.futureValue

      verify(mockCustomsCacheService)
        .cache[DeclarationType](any(), ArgumentMatchers.eq(DeclarationType.formId), any())(any(), any(), any())
    }

    pending
    "return 303 code" in {
      withCaching[DeclarationType](None)

      val validForm = ExportsTestData.correctDeclarationType
      val result = route(app, postRequest(declarationTypeUri, validForm)).get

      status(result) must be(SEE_OTHER)
    }

    pending
    "redirect to \"Consignment references\" page" in {
      withCaching[DeclarationType](None)

      val validForm = ExportsTestData.correctDeclarationType
      val result = route(app, postRequest(declarationTypeUri, validForm)).get
      val header = result.futureValue.header

      header.headers.get("Location") must be(
        Some("/customs-declare-exports/declaration/supplementary/consignment-references"))
    }
  }

}
