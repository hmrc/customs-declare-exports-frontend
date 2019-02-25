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

import base.{CustomExportsBaseSpec, TestHelper}
import forms.supplementary.DocumentsProduced
import forms.supplementary.DocumentsProducedSpec._
import org.scalatest.BeforeAndAfter
import play.api.libs.json.{JsObject, JsString, JsValue}
import play.api.test.Helpers._

class DocumentsProducedControllerSpec extends CustomExportsBaseSpec with BeforeAndAfter {

  before {
    authorizedUser()
    withCaching[DocumentsProduced](None)
  }

  val uri = uriWithContextPath("/declaration/supplementary/add-document")

  "DocumentsProducedController" should {
    "return 200 with a success" in {
      val result = route(app, getRequest(uri)).get
      status(result) must be(OK)
    }

    "display add document form" in {
      val result = route(app, getRequest(uri)).get
      val stringResult = contentAsString(result)

      status(result) must be(OK)
      stringResult must include(messages("supplementary.addDocument.title"))
      stringResult must include(messages("supplementary.addDocument.hint"))
      stringResult must include(messages("supplementary.addDocument.documentTypeCode"))
      stringResult must include(messages("supplementary.addDocument.documentIdentifier"))
      stringResult must include(messages("supplementary.addDocument.documentPart"))
      stringResult must include(messages("supplementary.addDocument.documentStatus"))
      stringResult must include(messages("supplementary.addDocument.documentStatusReason"))
      stringResult must include(messages("supplementary.addDocument.documentQuantity"))
    }

    "display page with errors" when {
      "provided with incorrect document status" in {
        val incorrectDocumentStatus: JsValue = JsObject(Map("documentStatus" -> JsString("as")))

        val result = route(app, postRequest(uri, incorrectDocumentStatus)).get
        status(result) must be(BAD_REQUEST)
        contentAsString(result) must include(messages("supplementary.addDocument.documentStatus.error"))
      }

      "provided with incorrect document status reason" in {
        val incorrectDocumentStatusReason: JsValue =
          JsObject(Map("documentStatusReason" -> JsString(TestHelper.createRandomString(36))))

        val result = route(app, postRequest(uri, incorrectDocumentStatusReason)).get
        status(result) must be(BAD_REQUEST)
        contentAsString(result) must include(messages("supplementary.addDocument.documentStatusReason.error"))
      }

      "provided with incorrect document identifier" in {
        val incorrectDocumentStatusReason: JsValue =
          JsObject(Map("documentIdentifier" -> JsString(TestHelper.createRandomString(31))))

        val result = route(app, postRequest(uri, incorrectDocumentStatusReason)).get
        status(result) must be(BAD_REQUEST)
        contentAsString(result) must include(messages("supplementary.addDocument.documentIdentifier.error"))
      }

      "provided with incorrect document part" in {
        val incorrectDocumentStatusReason: JsValue =
          JsObject(Map("documentPart" -> JsString(TestHelper.createRandomString(6))))

        val result = route(app, postRequest(uri, incorrectDocumentStatusReason)).get
        status(result) must be(BAD_REQUEST)
        contentAsString(result) must include(messages("supplementary.addDocument.documentPart.error"))
      }

      "provided with incorrect documents quantity" in {
        val incorrectDocumentQuantity: JsValue =
          JsObject(Map("documentQuantity" -> JsString("123456789012.1234567")))

        val result = route(app, postRequest(uri, incorrectDocumentQuantity)).get
        status(result) must be(BAD_REQUEST)
        contentAsString(result) must include(messages("supplementary.addDocument.documentQuantity.error"))
      }
    }

    "redirect to summary page" when {
      "provided with empty form" in {
        val result = route(app, postRequest(uri, emptyDocumentsProducedJSON)).get
        val header = result.futureValue.header

        status(result) must be(SEE_OTHER)
        header.headers.get("Location") must be(Some("/customs-declare-exports/declaration/supplementary/summary"))
      }

      "provided with correct form" in {
        val result = route(app, postRequest(uri, correctDocumentsProducedJSON)).get
        val header = result.futureValue.header

        status(result) must be(SEE_OTHER)
        header.headers.get("Location") must be(Some("/customs-declare-exports/declaration/supplementary/summary"))
      }
    }

  }
}
