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
import base.TestHelper._
import models.requests.{CancellationRequestExists, CancellationRequested, MissingDeclaration}
import org.scalatest.BeforeAndAfter
import play.api.libs.json.{JsObject, JsString, JsValue}
import play.api.test.Helpers._

class CancelDeclarationControllerSpec extends CustomExportsBaseSpec with BeforeAndAfter {

  val uri = uriWithContextPath("/cancel-declaration")

  before {
    authorizedUser()
  }

  val correctCancelJson: JsValue = JsObject(
    Map(
      "functionalReferenceId" -> JsString("5GB123456789000-123ABC456DEFIIIII"),
      "declarationId" -> JsString("DeclarationId"),
      "statementDescription" -> JsString("StatementDescription")
    )
  )

  "CancelDeclarationController" should {
    "return 200 with a success" in {
      val result = route(app, getRequest(uri)).get
      val stringResult = contentAsString(result)

      status(result) must be(OK)
      stringResult must include(messages("cancellation.functionalReferenceId"))
      stringResult must include(messages("cancellation.declarationId"))
      stringResult must include(messages("cancellation.statementDescription"))
    }

    "validate form - empty json" in {
      val emptyJson: JsValue = JsObject(
        Map(
          "functionalReferenceId" -> JsString(""),
          "declarationId" -> JsString(""),
          "statementDescription" -> JsString("")
        )
      )

      val result = route(app, postRequest(uri, emptyJson)).get
      val stringResult = contentAsString(result)

      status(result) must be(BAD_REQUEST)
      stringResult must include(messages("cancellation.functionalReferenceId.empty"))
      stringResult must include(messages("cancellation.declarationId.empty"))
      stringResult must include(messages("cancellation.statementDescription.empty"))
    }

    "validate form - too long answers" in {
      val wrongCancelJson: JsValue = JsObject(
        Map(
          "functionalReferenceId" -> JsString(createRandomString(36)),
          "declarationId" -> JsString(createRandomString(71)),
          "statementDescription" -> JsString(createRandomString(513))
        )
      )

      val result = route(app, postRequest(uri, wrongCancelJson)).get
      val stringResult = contentAsString(result)

      status(result) must be(BAD_REQUEST)
      stringResult must include(messages("cancellation.functionalReferenceId.tooLong"))
      stringResult must include(messages("cancellation.declarationId.tooLong"))
      stringResult must include(messages("cancellation.statementDescription.tooLong"))
    }

    "validate form - too short answers" in {
      val wrongCancelJson: JsValue = JsObject(Map("functionalReferenceId" -> JsString(createRandomString(10))))

      val result = route(app, postRequest(uri, wrongCancelJson)).get

      status(result) must be(BAD_REQUEST)
      contentAsString(result) must include(messages("cancellation.functionalReferenceId.tooShort"))
    }

    "redirect to error page" when {
      "cancellation failed in customs declarations" in {
        customsDeclaration400Response()

        val result = route(app, postRequest(uri, correctCancelJson)).get
        val stringResult = contentAsString(result)

        status(result) must be(BAD_REQUEST)
        stringResult must include(messages("global.error.heading"))
        stringResult must include(messages("global.error.message"))
      }
    }

    "redirect to not existing declaration error page" when {
      "user try to cancel not existing declaration" in {
        successfulCustomsDeclarationResponse()
        successfulCancelDeclarationResponse(MissingDeclaration)

        val result = route(app, postRequest(uri, correctCancelJson)).get
        val stringResult = contentAsString(result)

        status(result) must be(BAD_REQUEST)
        stringResult must include(messages("cancellation.error.heading"))
        stringResult must include(messages("cancellation.error.message"))
      }
    }

    "redirect to cancellation request exists page" when {
      "user try to cancel declaration once again" in {
        successfulCustomsDeclarationResponse()
        successfulCancelDeclarationResponse(CancellationRequestExists)

        val result = route(app, postRequest(uri, correctCancelJson)).get
        val stringResult = contentAsString(result)

        status(result) must be(BAD_REQUEST)
        stringResult must include(messages("cancellation.exists.error.heading"))
        stringResult must include(messages("cancellation.exists.error.message"))
      }
    }

    "redirect to confirmation page" when {
      "provided data is correct" in {
        successfulCustomsDeclarationResponse()
        successfulCancelDeclarationResponse(CancellationRequested)

        val result = route(app, postRequest(uri, correctCancelJson)).get
        val stringResult = contentAsString(result)

        status(result) must be(OK)
        stringResult must include(messages("cancellation.confirmationPage.message"))
      }
    }
  }
}
