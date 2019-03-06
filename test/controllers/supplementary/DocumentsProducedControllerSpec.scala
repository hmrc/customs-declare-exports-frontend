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
import controllers.util.{Add, Remove, SaveAndContinue}
import forms.supplementary.DocumentsProducedSpec.{correctDocumentsProducedMap, _}
import models.declaration.supplementary.DocumentsProducedData
import models.declaration.supplementary.DocumentsProducedData.formId
import org.scalatest.BeforeAndAfter
import play.api.libs.json.{JsObject, JsString, JsValue}
import play.api.test.Helpers._

class DocumentsProducedControllerSpec extends CustomExportsBaseSpec with BeforeAndAfter {

  private val uri = uriWithContextPath("/declaration/supplementary/add-document")
  private val addActionUrlEncoded = (Add.toString, "")
  private val saveAndContinueActionUrlEncoded = (SaveAndContinue.toString, "")
  private def removeActionUrlEncoded(value: String) = (Remove.toString, value)

  before {
    authorizedUser()
    withCaching[DocumentsProducedData](None, formId)
  }

  "Documents Produced Controller when getting the page" should {
    "return 200 with a success" in {
      val result = route(app, getRequest(uri)).get
      status(result) must be(OK)
    }

    "display add document form with no documents" in {
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

    "display add document form with added documents" in {
      withCaching[DocumentsProducedData](Some(correctDocumentsProducedData), formId)

      val result = route(app, getRequest(uri)).get
      val stringResult = contentAsString(result)

      status(result) must be(OK)
      stringResult must include("AB12")
      stringResult must include("DocumentStatusReason")
      stringResult must include("1234567890.123456")
      stringResult must include("Remove")
      stringResult must include(messages("supplementary.addDocument.title"))
      stringResult must include(messages("supplementary.addDocument.hint"))
      stringResult must include(messages("supplementary.addDocument.documentTypeCode"))
      stringResult must include(messages("supplementary.addDocument.documentIdentifier"))
      stringResult must include(messages("supplementary.addDocument.documentPart"))
      stringResult must include(messages("supplementary.addDocument.documentStatus"))
      stringResult must include(messages("supplementary.addDocument.documentStatusReason"))
      stringResult must include(messages("supplementary.addDocument.documentQuantity"))
    }

    "display back button that links to additional information page" in {
      val result = route(app, getRequest(uri)).get
      val stringResult = contentAsString(result)

      status(result) must be(OK)
      stringResult must include(messages("site.back"))
      stringResult must include(messages("/declaration/supplementary/additional-information"))
    }
  }

  "Documents Produced Controller handling a post" should {
    "display page with an error" when {
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

      "try to remove a non existent document" in {
        withCaching[DocumentsProducedData](Some(correctDocumentsProducedData), formId)
        val body = ("action", "Remove:123-123-123-123-123-123")

        val result = route(app, postRequestFormUrlEncoded(uri, body)).get
        val stringResult = contentAsString(result)

        status(result) must be(BAD_REQUEST)
        stringResult must include(messages("global.error.title"))
        stringResult must include(messages("global.error.heading"))
        stringResult must include(messages("global.error.message"))
      }

      "try to add duplicated document" in {
        withCaching[DocumentsProducedData](Some(correctDocumentsProducedData), formId)

        val duplicatedDocument: Map[String, String] = Map(
          "documentTypeCode" -> "AB12",
          "documentIdentifier" -> "ABCDEF1234567890",
          "documentPart" -> "ABC12",
          "documentStatus" -> "AB",
          "documentStatusReason" -> "DocumentStatusReason",
          "documentQuantity" -> "1234567890.123456")

        val body = duplicatedDocument.toSeq :+ addActionUrlEncoded

        val result = route(app, postRequestFormUrlEncoded(uri, body: _*)).get

        status(result) must be(BAD_REQUEST)
        contentAsString(result) must include(messages("supplementary.addDocument.duplicated"))
      }

      "try to add an undefined document" in {

        withCaching[DocumentsProducedData](Some(correctDocumentsProducedData), formId)

        val undefinedDocument: Map[String, String] = Map(
          "documentTypeCode" -> "",
          "documentIdentifier" -> "",
          "documentPart" -> "",
          "documentStatus" -> "",
          "documentStatusReason" -> "",
          "documentQuantity" -> "")

        val body = undefinedDocument.toSeq :+ addActionUrlEncoded

        val result = route(app, postRequestFormUrlEncoded(uri, body: _*)).get

        status(result) must be(BAD_REQUEST)
        contentAsString(result) must include(messages("supplementary.addDocument.isNotDefined"))
      }
    }

    "add a document sucessfully" when {
      "with an empty cache" in {
        withCaching[DocumentsProducedData](None, formId)
        val body = correctDocumentsProducedMap.toSeq :+ addActionUrlEncoded

        val result = route(app, postRequestFormUrlEncoded(uri, body: _*)).get

        status(result) must be(SEE_OTHER)
      }

      "that does not exist in cache" in {
        withCaching[DocumentsProducedData](Some(correctDocumentsProducedData), formId)
        val body = correctDocumentsProducedMap.toSeq :+ addActionUrlEncoded

        val result = route(app, postRequestFormUrlEncoded(uri, body: _*)).get

        status(result) must be(SEE_OTHER)
      }
    }

    "remove a document successfully" when {
      "exists in cache" in {
        withCaching[DocumentsProducedData](Some(correctDocumentsProducedData), formId)

        val body = removeActionUrlEncoded(correctDocumentsProducedData.documents.head.toJson.toString())

        val result = route(app, postRequestFormUrlEncoded(uri, body)).get

        status(result) must be(SEE_OTHER)
      }
    }

    "redirect to summary page" when {
      "provided with empty form and with empty cache" in {
        val body = emptyDocumentsProducedMap.toSeq :+ saveAndContinueActionUrlEncoded
        val result = route(app, postRequestFormUrlEncoded(uri, body: _*)).get

        val header = result.futureValue.header
        status(result) must be(SEE_OTHER)
        header.headers.get("Location") must be(Some("/customs-declare-exports/declaration/supplementary/summary"))
      }

      "provided with empty form and with existing cache" in {
        withCaching[DocumentsProducedData](Some(correctDocumentsProducedData), formId)
        val body = emptyDocumentsProducedMap.toSeq :+ saveAndContinueActionUrlEncoded
        val result = route(app, postRequestFormUrlEncoded(uri, body: _*)).get

        val header = result.futureValue.header
        status(result) must be(SEE_OTHER)
        header.headers.get("Location") must be(Some("/customs-declare-exports/declaration/supplementary/summary"))
      }

      "provided with a valid document and with empty cache" in {
        val body = correctDocumentsProducedMap.toSeq :+ saveAndContinueActionUrlEncoded
        val result = route(app, postRequestFormUrlEncoded(uri, body: _*)).get
        val header = result.futureValue.header

        status(result) must be(SEE_OTHER)
        header.headers.get("Location") must be(Some("/customs-declare-exports/declaration/supplementary/summary"))
      }

      "provided with a valid document and with existing cache" in {
        withCaching[DocumentsProducedData](Some(correctDocumentsProducedData), formId)
        val body = correctDocumentsProducedMap.toSeq :+ saveAndContinueActionUrlEncoded
        val result = route(app, postRequestFormUrlEncoded(uri, body: _*)).get
        val header = result.futureValue.header

        status(result) must be(SEE_OTHER)
        header.headers.get("Location") must be(Some("/customs-declare-exports/declaration/supplementary/summary"))
      }
    }

  }
}
