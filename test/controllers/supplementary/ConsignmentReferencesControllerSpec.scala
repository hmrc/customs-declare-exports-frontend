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
import forms.Ducr
import forms.supplementary.ConsignmentReferences
import org.mockito.ArgumentMatchers
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, verify}
import org.scalatest.BeforeAndAfter
import play.api.libs.json.{JsObject, JsString, JsValue}
import play.api.mvc.Result
import play.api.test.Helpers._

import scala.concurrent.Future

class ConsignmentReferencesControllerSpec extends CustomExportsBaseSpec with BeforeAndAfter {
  private val consignmentReferencesUri = uriWithContextPath("/declaration/supplementary/consignment-references")

  import ConsignmentReferencesControllerSpec._

  before {
    authorizedUser()
  }

  "ConsignmentReferencesController on displayPage" should {
    "return 200 code" in {
      val result = displayConsignmentReferencesPageTestScenario()
      status(result) must be(OK)
    }

    "display page title" in {
      val result = displayConsignmentReferencesPageTestScenario()
      contentAsString(result) must include(messages("supplementary.consignmentReferences.title"))
    }

    "display \"back\" button that links to \"Declaration Type\" page" in {
      val result = displayConsignmentReferencesPageTestScenario()
      contentAsString(result) must include(messages("site.back"))
      contentAsString(result) must include(messages("/declaration/supplementary/type"))
    }

    "display page header" in {
      val result = displayConsignmentReferencesPageTestScenario()
      contentAsString(result) must include(messages("supplementary.consignmentReferences.header"))
    }

    "display input text with question and hint for reference number/UCR" in {
      val result = displayConsignmentReferencesPageTestScenario()
      contentAsString(result) must include(messages("supplementary.consignmentReferences.ucr.info"))
      contentAsString(result) must include(messages("supplementary.consignmentReferences.ucr.hint"))
    }

    "display input text with question and hint for LRN" in {
      val result = displayConsignmentReferencesPageTestScenario()
      contentAsString(result) must include(messages("supplementary.consignmentReferences.lrn.info"))
      contentAsString(result) must include(messages("supplementary.consignmentReferences.lrn.hint"))
    }

    "display \"Save and continue\" button" in {
      val result = displayConsignmentReferencesPageTestScenario()
      contentAsString(result) must include(messages("site.save_and_continue"))
      contentAsString(result) must include("button id=\"submit\" class=\"button\"")
    }

    "not populate the form fields if cache is empty" in {
      val result = displayConsignmentReferencesPageTestScenario()
      contentAsString(result).replaceAll(" ", "") must include("name=\"ducr.ducr\"\nvalue=\"\"")
      contentAsString(result).replaceAll(" ", "") must include("name=\"lrn\"\nvalue=\"\"")
    }

    "populate the form fields with data from cache" in {
      val result = displayConsignmentReferencesPageTestScenario(
        Some(ConsignmentReferences(ducr = Some(Ducr(exemplaryDucr)), lrn = "1234ABCD"))
      )

      contentAsString(result).replaceAll(" ", "") must include("name=\"ducr.ducr\"\nvalue=\"" + exemplaryDucr + "\"")
      contentAsString(result).replaceAll(" ", "") must include("name=\"lrn\"\nvalue=\"1234ABCD\"")
    }

    def displayConsignmentReferencesPageTestScenario(
      cacheStoredValue: Option[ConsignmentReferences] = None
    ): Future[Result] = {
      withCaching[ConsignmentReferences](cacheStoredValue, ConsignmentReferences.id)
      route(app, getRequest(consignmentReferencesUri)).get
    }
  }

  "ConsignmentReferencesController on submitConsignmentReferences" should {

    "display the form page with error" when {
      "provided DUCR is longer than 35 characters" in {
        withCaching[ConsignmentReferences](None, ConsignmentReferences.id)

        val form =
          buildConsignmentReferencesTestData(ducr = "8GB123456789012-1234567890123456789ABCD", lrn = "123456QWERTY")
        val result = route(app, postRequest(consignmentReferencesUri, form)).get

        contentAsString(result) must include(messages("error.ducr"))
      }

      "no value provided for LRN" in {
        withCaching[ConsignmentReferences](None, ConsignmentReferences.id)

        val form = buildConsignmentReferencesTestData(ducr = exemplaryDucr)
        val result = route(app, postRequest(consignmentReferencesUri, form)).get

        contentAsString(result) must include(messages("supplementary.consignmentReferences.lrn.error.empty"))
      }

      "provided LRN contains special characters" in {
        withCaching[ConsignmentReferences](None, ConsignmentReferences.id)

        val form = buildConsignmentReferencesTestData(ducr = exemplaryDucr, lrn = "123$%&ABC")
        val result = route(app, postRequest(consignmentReferencesUri, form)).get

        contentAsString(result) must include(messages("supplementary.consignmentReferences.lrn.error.specialCharacter"))
      }

      "provided LRN is longer than 22 characters" in {
        withCaching[ConsignmentReferences](None, ConsignmentReferences.id)

        val form = buildConsignmentReferencesTestData(ducr = exemplaryDucr, lrn = "1234567890123456789012QWERTY")
        val result = route(app, postRequest(consignmentReferencesUri, form)).get

        contentAsString(result) must include(messages("supplementary.consignmentReferences.lrn.error.length"))
      }
    }

    "proceed when no ucr provided by user" in {
      withCaching[ConsignmentReferences](None, ConsignmentReferences.id)

      val validForm = buildConsignmentReferencesTestData(lrn = "123ABC")
      val result = route(app, postRequest(consignmentReferencesUri, validForm)).get

      contentAsString(result) mustNot include(messages("error.ducr"))
    }

    "save data to the cache" in {
      reset(mockCustomsCacheService)
      withCaching[ConsignmentReferences](None, ConsignmentReferences.id)

      val validForm = buildConsignmentReferencesTestData(ducr = exemplaryDucr, lrn = "123ABC")
      route(app, postRequest(consignmentReferencesUri, validForm)).get.map { _ =>
        verify(mockCustomsCacheService)
          .cache[ConsignmentReferences](any(), ArgumentMatchers.eq(ConsignmentReferences.id), any())(
            any(),
            any(),
            any()
          )
      }
    }

    pending
    "return 303 code" in {
      withCaching[ConsignmentReferences](None, ConsignmentReferences.id)

      val validForm = buildConsignmentReferencesTestData(ducr = exemplaryDucr, lrn = "123ABC")
      val result = route(app, postRequest(consignmentReferencesUri, validForm)).get

      status(result) must be(SEE_OTHER)
    }

    pending
    "redirect to \"Exporter ID\" page" in {
      withCaching[ConsignmentReferences](None, ConsignmentReferences.id)

      val validForm = buildConsignmentReferencesTestData(ducr = exemplaryDucr, lrn = "123ABC")
      route(app, postRequest(consignmentReferencesUri, validForm)).get.map { resultValue =>
        resultValue.header.headers.get("Location") must be(
          Some("/customs-declare-exports/declaration/supplementary/exporter-id")
        )
      }
    }
  }
}

object ConsignmentReferencesControllerSpec {
  val exemplaryDucr = "8GB123456789012-1234567890123456789"

  def buildConsignmentReferencesTestData(ducr: String = "", lrn: String = ""): JsValue = JsObject(
    Map("ducr.ducr" -> JsString(ducr), "lrn" -> JsString(lrn))
  )
}
