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
import forms.supplementary.ConsignmentReferences
import forms.supplementary.ConsignmentReferencesSpec._
import org.mockito.ArgumentMatchers
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, verify}
import org.scalatest.BeforeAndAfter
import play.api.libs.json.{JsObject, JsString, JsValue}
import play.api.mvc.Result
import play.api.test.Helpers._

import scala.concurrent.Future

class ConsignmentReferencesControllerSpec extends CustomExportsBaseSpec with BeforeAndAfter {

  private val uri = uriWithContextPath("/declaration/supplementary/consignment-references")

  import ConsignmentReferencesControllerSpec._

  before {
    authorizedUser()
  }

  "Consignment References Controller on page" should {

    "return 200 code" in {
      val result = displayConsignmentReferencesPageTestScenario()
      status(result) must be(OK)
    }

    "not populate the form fields if cache is empty" in {
      val result = displayConsignmentReferencesPageTestScenario()
      val resultAsString = contentAsString(result)

      resultAsString.replaceAll(" ", "") must include("name=\"ducr.ducr\"\nvalue=\"\"")
      resultAsString.replaceAll(" ", "") must include("name=\"lrn\"\nvalue=\"\"")
    }

    "populate the form fields with data from cache" in {
      val result = displayConsignmentReferencesPageTestScenario(Some(correctConsignmentReferences))
      val resultAsString = contentAsString(result)

      resultAsString.replaceAll(" ", "") must include("name=\"ducr.ducr\"\nvalue=\"" + exemplaryDucr + "\"")
      resultAsString.replaceAll(" ", "") must include(
        "name=\"lrn\"\nvalue=\"" + correctConsignmentReferences.lrn + "\""
      )
    }

    def displayConsignmentReferencesPageTestScenario(
      cacheStoredValue: Option[ConsignmentReferences] = None
    ): Future[Result] = {
      withCaching[ConsignmentReferences](cacheStoredValue, ConsignmentReferences.id)
      route(app, getRequest(uri)).get
    }
  }

  "Consignment References Controller on submit page" should {

    "display the form page with error" when {

      "provided UCR is longer than 35 characters" in {
        withCaching[ConsignmentReferences](None, ConsignmentReferences.id)

        val form =
          buildConsignmentReferencesTestData(ducr = "8GB123456789012-1234567890123456789ABCD", lrn = "123456QWERTY")
        val result = route(app, postRequest(uri, form)).get

        contentAsString(result) must include(messages("error.ducr"))
      }

      "no value provided for LRN" in {
        withCaching[ConsignmentReferences](None, ConsignmentReferences.id)

        val form = buildConsignmentReferencesTestData(ducr = exemplaryDucr)
        val result = route(app, postRequest(uri, form)).get

        contentAsString(result) must include(messages("supplementary.consignmentReferences.lrn.error.empty"))
      }

      "provided LRN contains special characters" in {
        withCaching[ConsignmentReferences](None, ConsignmentReferences.id)

        val form = buildConsignmentReferencesTestData(ducr = exemplaryDucr, lrn = "123$%&ABC")
        val result = route(app, postRequest(uri, form)).get

        contentAsString(result) must include(messages("supplementary.consignmentReferences.lrn.error.specialCharacter"))
      }

      "provided LRN is longer than 22 characters" in {
        withCaching[ConsignmentReferences](None, ConsignmentReferences.id)

        val form = buildConsignmentReferencesTestData(ducr = exemplaryDucr, lrn = "1234567890123456789012QWERTY")
        val result = route(app, postRequest(uri, form)).get

        contentAsString(result) must include(messages("supplementary.consignmentReferences.lrn.error.length"))
      }
    }

    "proceed when no UCR provided by user" in {
      withCaching[ConsignmentReferences](None, ConsignmentReferences.id)

      val validForm = buildConsignmentReferencesTestData(lrn = "123ABC")
      val result = route(app, postRequest(uri, validForm)).get

      contentAsString(result) mustNot include(messages("error.ducr"))
    }

    "save data to the cache" in {
      reset(mockCustomsCacheService)
      withCaching[ConsignmentReferences](None, ConsignmentReferences.id)

      route(app, postRequest(uri, correctConsignmentReferencesJSON)).get.map { _ =>
        verify(mockCustomsCacheService)
          .cache[ConsignmentReferences](any(), ArgumentMatchers.eq(ConsignmentReferences.id), any())(
            any(),
            any(),
            any()
          )
      }
    }

    "return 303 code" in {
      withCaching[ConsignmentReferences](None, ConsignmentReferences.id)

      val result = route(app, postRequest(uri, correctConsignmentReferencesJSON)).get

      status(result) must be(SEE_OTHER)
    }

    "redirect to \"Exporter Details\" page" in {
      withCaching[ConsignmentReferences](None, ConsignmentReferences.id)

      val result = route(app, postRequest(uri, correctConsignmentReferencesJSON)).get
      val header = result.futureValue.header

      header.headers.get("Location") must be(
        Some("/customs-declare-exports/declaration/supplementary/exporter-details")
      )
    }
  }
}

object ConsignmentReferencesControllerSpec {
  def buildConsignmentReferencesTestData(ducr: String = "", lrn: String = ""): JsValue = JsObject(
    Map("ducr.ducr" -> JsString(ducr), "lrn" -> JsString(lrn))
  )
}
