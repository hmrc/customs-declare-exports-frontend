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

package controllers

import base.CustomExportsBaseSpec
import forms.CancelDeclarationSpec
import metrics.MetricIdentifiers
import models.requests.CancellationRequested
import org.scalatest.BeforeAndAfter
import play.api.libs.json.{JsObject, JsString, JsValue}
import play.api.test.Helpers._

class CancelDeclarationControllerSpec extends CustomExportsBaseSpec with BeforeAndAfter {

  val uri = uriWithContextPath("/cancel-declaration")

  before {
    authorizedUser()
  }

  "Cancel Declaration Controller on GET" should {

    "return 200 code" in {

      val result = route(app, getRequest(uri)).get
      status(result) mustBe OK
    }
  }

  "Cancel Declaration Controller on POST" should {

    "validate request and redirect - all values provided" in {

      successfulCustomsDeclareExportsResponse()
      successfulCancelDeclarationResponse(CancellationRequested)
      val result = route(app, postRequest(uri, CancelDeclarationSpec.correctCancelDeclarationJSON)).get

      status(result) mustBe OK

      val stringResult = contentAsString(result)
      stringResult must include(messages("cancellation.confirmationPage.message"))
    }

    "record cancellation timing and increase the Success Counter when response is OK" in {
      val timer = metrics.timers(MetricIdentifiers.cancelMetric).getCount
      val counter = metrics.counters(MetricIdentifiers.cancelMetric).getCount
      successfulCustomsDeclareExportsResponse()
      successfulCancelDeclarationResponse(CancellationRequested)
      val result = route(app, postRequest(uri, CancelDeclarationSpec.correctCancelDeclarationJSON)).get

      status(result) mustBe OK

      val stringResult = contentAsString(result)
      stringResult must include(messages("cancellation.confirmationPage.message"))
      metrics.timers(MetricIdentifiers.cancelMetric).getCount mustBe timer + 1
      metrics.counters(MetricIdentifiers.cancelMetric).getCount mustBe counter + 1
    }
  }
}

object CancelDeclarationControllerSpec {
  def buildCancelDeclarationJsonInput(
    functionalReferenceId: String = "",
    declarationId: String = "",
    statementDescription: String = "",
    changeReason: String = ""
  ): JsValue = JsObject(
    Map(
      "functionalReferenceId" -> JsString(functionalReferenceId),
      "declarationId" -> JsString(declarationId),
      "statementDescription" -> JsString(statementDescription),
      "changeReason" -> JsString(changeReason)
    )
  )
}
