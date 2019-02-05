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
import play.api.libs.json.{JsObject, JsString, JsValue}
import play.api.test.Helpers._

class DocumentsProducedControllerSpec extends CustomExportsBaseSpec {

  val uri = uriWithContextPath("/declaration/supplementary/add-document")

  "DocumentsProducedController" should {
    "return 200 with a success" in {
      authorizedUser()
      withCaching[DocumentsProduced](None)

      val result = route(app, getRequest(uri)).get
      status(result) must be(OK)
    }

    "display add document form" in {
      authorizedUser()
      withCaching[DocumentsProduced](None)

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
    }

    "validate form - incorrect document status" in {
      authorizedUser()
      withCaching[DocumentsProduced](None)

      val incorrectDocumentStatus: JsValue = JsObject(Map("documentStatus" -> JsString("as")))

      val result = route(app, postRequest(uri, incorrectDocumentStatus)).get
      status(result) must be(BAD_REQUEST)
      contentAsString(result) must include(messages("supplementary.addDocument.documentStatus.error"))

    }

    "validate form - incorrect Document status reason" in {
      authorizedUser()
      withCaching[DocumentsProduced](None)

      val incorrectDocumentStatusReason: JsValue =
        JsObject(Map("documentStatusReason" -> JsString(TestHelper.createRandomString(36))))

      val result = route(app, postRequest(uri, incorrectDocumentStatusReason)).get
      status(result) must be(BAD_REQUEST)
      contentAsString(result) must include(messages("supplementary.addDocument.documentStatusReason.error"))
    }

    "validate form - incorrect document identifier" in {
      authorizedUser()
      withCaching[DocumentsProduced](None)

      val incorrectDocumentStatusReason: JsValue =
        JsObject(Map("documentIdentifier" -> JsString(TestHelper.createRandomString(31))))

      val result = route(app, postRequest(uri, incorrectDocumentStatusReason)).get
      status(result) must be(BAD_REQUEST)
      contentAsString(result) must include(messages("supplementary.addDocument.documentIdentifier.error"))
    }

    "validate form - incorrect document part" in {
      authorizedUser()
      withCaching[DocumentsProduced](None)

      val incorrectDocumentStatusReason: JsValue =
        JsObject(Map("documentPart" -> JsString(TestHelper.createRandomString(6))))

      val result = route(app, postRequest(uri, incorrectDocumentStatusReason)).get
      status(result) must be(BAD_REQUEST)
      contentAsString(result) must include(messages("supplementary.addDocument.documentPart.error"))
    }

    "validate form - empty form" in {
      pending
      authorizedUser()
      withCaching[DocumentsProduced](None)

      val result = route(app, postRequest(uri, emptyDocumentsProducedJSON)).get
      val header = result.futureValue.header

      status(result) must be(SEE_OTHER)
      header.headers.get("Location") must be(
        Some("/customs-declare-exports/declaration/supplementary/summary")
      )
    }

    "validate form - correct form" in {
      pending
      authorizedUser()
      withCaching[DocumentsProduced](None)

      val result = route(app, postRequest(uri, correctDocumentsProducedJSON)).get
      val header = result.futureValue.header

      status(result) must be(SEE_OTHER)
      header.headers.get("Location") must be(
        Some("/customs-declare-exports/declaration/supplementary/summary")
      )
    }
  }
}
