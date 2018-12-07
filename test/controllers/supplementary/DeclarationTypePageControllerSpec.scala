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

class DeclarationTypePageControllerSpec extends CustomExportsBaseSpec with BeforeAndAfter {

  private val uri = uriWithContextPath("/declaration/supplementary/type")

  before {
    authorizedUser()
  }


  "DeclarationTypePageController on displayDeclarationTypePage" should {
    "return 200 code" in {
      withCaching[DeclarationType](None, DeclarationType.formId)

      val result = route(app, getRequest(uri)).get

      status(result) must be(OK)
    }

    "display page title" in {
      withCaching[DeclarationType](None, DeclarationType.formId)

      val result = route(app, getRequest(uri)).get

      contentAsString(result) must include(
        messages("supplementary.declarationTypePage.title"))
    }

    "display page header" in {
      withCaching[DeclarationType](None, DeclarationType.formId)

      val result = route(app, getRequest(uri)).get

      contentAsString(result) must include(
        messages("supplementary.declarationTypePage.heading"))
    }

    "display information content" in {
      withCaching[DeclarationType](None, DeclarationType.formId)

      val result = route(app, getRequest(uri)).get

      contentAsString(result) must include(
        messages("supplementary.declarationTypePage.heading.secondary"))
    }

    "display radio button with question text for declaration type" in {
      withCaching[DeclarationType](None, DeclarationType.formId)

      val result = route(app, getRequest(uri)).get

      contentAsString(result) must include(
        messages("supplementary.declarationTypePage.inputText.declarationType"))
      contentAsString(result) must include(
        messages("supplementary.declarationTypePage.inputText.declarationType.hint"))
    }

    "display radio button with question text for additional declaration type" in {
      withCaching[DeclarationType](None, DeclarationType.formId)

      val result = route(app, getRequest(uri)).get

      contentAsString(result) must include(
        messages("supplementary.declarationTypePage.inputText.additionalDeclarationType"))
      contentAsString(result) must include(
        messages("supplementary.declarationTypePage.inputText.additionalDeclarationType.hint"))
    }

    "display \"Save and continue\" button" in {
      withCaching[DeclarationType](None, DeclarationType.formId)

      val result = route(app, getRequest(uri)).get

      contentAsString(result) must include(messages("site.save_and_continue"))
      contentAsString(result) must include("button id=\"submit\" class=\"button\"")
    }

    "not populate the form fields if cache is empty" in {
      withCaching[DeclarationType](None, DeclarationType.formId)

      val result = route(app, getRequest(uri)).get

      contentAsString(result) mustNot include("checked=\"checked\"")
    }

    "populate the form fields with data from cache" in {
      withCaching[DeclarationType](Some(DeclarationType("CO", "Y")), DeclarationType.formId)

      val result = route(app, getRequest(uri)).get

      contentAsString(result) must include("checked=\"checked\"")
    }
  }


  "DeclarationTypePageController on submitDeclarationType" should {

    "display the form page with error" when {
      "no value provided for declaration type" in {
        withCaching[DeclarationType](None)

        val formWithoutDeclarationType = JsObject(Map("additionalDeclarationType" -> JsString("Y")))
        val result = route(app, postRequest(uri, formWithoutDeclarationType)).get

        contentAsString(result) must include("Please enter a value")
      }

      "no value provided for additional declaration type" in {
        withCaching[DeclarationType](None)

        val formWithoutAdditionalDeclarationType = JsObject(Map("declarationType" -> JsString("EX")))
        val result = route(app, postRequest(uri, formWithoutAdditionalDeclarationType)).get

        contentAsString(result) must include("Please enter a value")
      }
    }

    "save the data to the cache" in {
      reset(mockCustomsCacheService)
      withCaching[DeclarationType](None)

      val validForm = ExportsTestData.correctDeclarationType
      route(app, postRequest(uri, validForm)).get.futureValue

      verify(mockCustomsCacheService)
        .cache[DeclarationType](any(), ArgumentMatchers.eq(DeclarationType.formId), any())(any(), any(), any())
    }

    pending
    "return 303 code" in {
      withCaching[DeclarationType](None)

      val validForm = ExportsTestData.correctDeclarationType
      val result = route(app, postRequest(uri, validForm)).get

      status(result) must be(SEE_OTHER)
    }

    pending
    "redirect to \"Consignment Reference\" page" in {
      withCaching[DeclarationType](None)

      val validForm = ExportsTestData.correctDeclarationType
      val result = route(app, postRequest(uri, validForm)).get
      val header = result.futureValue.header

      header.headers.get("Location") must be(
        Some("/customs-declare-exports/declaration/supplementary/consignment-reference"))
    }
  }

}
