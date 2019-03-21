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

import base.{CustomExportsBaseSpec, TestHelper, ViewValidator}
import controllers.util.{Add, Remove, SaveAndContinue}
import forms.supplementary.DocumentsProduced
import forms.supplementary.DocumentsProducedSpec.{correctDocumentsProducedMap, _}
import helpers.views.supplementary.{CommonMessages, DocumentsProducedMessages}
import models.declaration.supplementary.DocumentsProducedData
import models.declaration.supplementary.DocumentsProducedData.formId
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatest.BeforeAndAfter
import play.api.libs.json.{JsObject, JsString, JsValue}
import play.api.test.Helpers._
import scala.concurrent.Future
class DocumentsProducedControllerSpec
    extends CustomExportsBaseSpec with DocumentsProducedMessages with CommonMessages with ViewValidator {

  import DocumentsProducedControllerSpec._

  private val uri = uriWithContextPath("/declaration/supplementary/add-document")
  private val addActionUrlEncoded = (Add.toString, "")
  private val saveAndContinueActionUrlEncoded = (SaveAndContinue.toString, "")
  private def removeActionUrlEncoded(value: String) = (Remove.toString, value)

  before {
    authorizedUser()
    withCaching[DocumentsProducedData](None, formId)
  }

  "Documents Produced Controller on GET" should {

    "return 200 with a success" in {
      val result = route(app, getRequest(uri)).get

      status(result) must be(OK)
    }
  }

  "Document Produced Controller on POST" should {

    "display page with errors" when {

      "provided with incorrect document type code" in {
        val incorrectDocumentTypeCode: JsValue = JsObject(Map("documentTypeCode" -> JsString("abcdf")))

        val result = route(app, postRequest(uri, incorrectDocumentTypeCode)).get
        status(result) must be(BAD_REQUEST)
        contentAsString(result) must include(messages(documentTypeCodeError))
      }

      "provided with incorrect document identifier" in {
        val incorrectDocumentIdentifier: JsValue =
          JsObject(Map("documentIdentifier" -> JsString(TestHelper.createRandomString(31))))

        val result = route(app, postRequest(uri, incorrectDocumentIdentifier)).get
        status(result) must be(BAD_REQUEST)
        contentAsString(result) must include(messages(documentIdentifierError))
      }

      "provided with incorrect document part" in {
        val incorrectDocumentPart: JsValue =
          JsObject(Map("documentPart" -> JsString(TestHelper.createRandomString(6))))

        val result = route(app, postRequest(uri, incorrectDocumentPart)).get
        status(result) must be(BAD_REQUEST)
        contentAsString(result) must include(messages(documentPartError))
      }

      "provided with incorrect document status" in {
        val incorrectDocumentStatus: JsValue = JsObject(Map("documentStatus" -> JsString("as")))

        val result = route(app, postRequest(uri, incorrectDocumentStatus)).get
        status(result) must be(BAD_REQUEST)
        contentAsString(result) must include(messages(documentStatusError))
      }

      "provided with incorrect document status reason" in {
        val incorrectDocumentStatusReason: JsValue =
          JsObject(Map("documentStatusReason" -> JsString(TestHelper.createRandomString(36))))

        val result = route(app, postRequest(uri, incorrectDocumentStatusReason)).get
        status(result) must be(BAD_REQUEST)
        contentAsString(result) must include(messages(documentStatusReasonError))
      }

      "provided with incorrect documents quantity" in {
        val incorrectDocumentQuantity: JsValue =
          JsObject(Map("documentQuantity" -> JsString("123456789012.1234567")))

        val result = route(app, postRequest(uri, incorrectDocumentQuantity)).get
        status(result) must be(BAD_REQUEST)
        contentAsString(result) must include(messages(documentQuantityError))
      }

      "try to remove a non existent document" in {

        withCaching[DocumentsProducedData](Some(correctDocumentsProducedData), formId)
        val body = ("action", "Remove:123-123-123-123-123-123")

        val result = route(app, postRequestFormUrlEncoded(uri, body)).get
        val stringResult = contentAsString(result)

        status(result) must be(BAD_REQUEST)
        stringResult must include(messages(globalErrorTitle))
        stringResult must include(messages(globalErrorHeading))
        stringResult must include(messages(globalErrorMessage))
      }

      "try to add duplicated document" in {
        withCaching[DocumentsProducedData](Some(correctDocumentsProducedData), formId)

        val duplicatedDocument: Map[String, String] = Map(
          "documentTypeCode" -> "AB12",
          "documentIdentifier" -> "ABCDEF1234567890",
          "documentPart" -> "ABC12",
          "documentStatus" -> "AB",
          "documentStatusReason" -> "DocumentStatusReason",
          "documentQuantity" -> "1234567890.123456"
        )

        val body = duplicatedDocument.toSeq :+ addActionUrlEncoded
        val result = route(app, postRequestFormUrlEncoded(uri, body: _*)).get
        val page = contentAsString(result)

        status(result) must be(BAD_REQUEST)

        checkErrorsSummary(page)
        checkErrorLink(page, 1, duplicatedItem, "#")
      }

      "try to add an empty document" in {

        withCaching[DocumentsProducedData](Some(correctDocumentsProducedData), formId)

        val undefinedDocument: Map[String, String] = Map(
          "documentTypeCode" -> "",
          "documentIdentifier" -> "",
          "documentPart" -> "",
          "documentStatus" -> "",
          "documentStatusReason" -> "",
          "documentQuantity" -> ""
        )

        val body = undefinedDocument.toSeq :+ addActionUrlEncoded
        val result = route(app, postRequestFormUrlEncoded(uri, body: _*)).get
        val page = contentAsString(result)

        status(result) must be(BAD_REQUEST)

        checkErrorsSummary(page)
        checkErrorLink(page, 1, notDefined, "#")
      }

      "try to add more then 99 documents" in {

        withCaching[DocumentsProducedData](Some(cacheWithMaximumAmountOfHolders), formId)

        val body = Seq(
          ("documentTypeCode", "1234"),
          ("documentIdentifier", "Davis"),
          ("documentPart", "1234"),
          ("documentStatus", "AB"),
          ("documentStatusReason", "1234"),
          ("documentQuantity", "1234"),
          addActionUrlEncoded
        )
        val result = route(app, postRequestFormUrlEncoded(uri, body: _*)).get
        val page = contentAsString(result)

        status(result) must be(BAD_REQUEST)

        checkErrorsSummary(page)
        checkErrorLink(page, 1, maximumAmountReached, "#")
      }
    }

    "add a document successfully" when {

      "cache is empty" in {

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

    "redirect to exports-items page" when {

      "provided with empty form and with empty cache" in {

        when(mockItemsCachingService.addItemToCache(any(),any())(any(), any())).thenReturn(Future.successful(true))
        val body = emptyDocumentsProducedMap.toSeq :+ saveAndContinueActionUrlEncoded
        val result = route(app, postRequestFormUrlEncoded(uri, body: _*)).get
        val header = result.futureValue.header

        status(result) must be(SEE_OTHER)
        header.headers.get("Location") must be(Some("/customs-declare-exports/declaration/supplementary/export-items"))
      }

      "provided with empty form and with existing cache" in {

        withCaching[DocumentsProducedData](Some(correctDocumentsProducedData), formId)

        val body = emptyDocumentsProducedMap.toSeq :+ saveAndContinueActionUrlEncoded
        val result = route(app, postRequestFormUrlEncoded(uri, body: _*)).get
        val header = result.futureValue.header

        status(result) must be(SEE_OTHER)
        header.headers.get("Location") must be(Some("/customs-declare-exports/declaration/supplementary/export-items"))
      }

      "provided with a valid document and with empty cache" in {

        val body = correctDocumentsProducedMap.toSeq :+ saveAndContinueActionUrlEncoded
        val result = route(app, postRequestFormUrlEncoded(uri, body: _*)).get
        val header = result.futureValue.header

        status(result) must be(SEE_OTHER)
        header.headers.get("Location") must be(Some("/customs-declare-exports/declaration/supplementary/export-items"))
      }

      "provided with a valid document and with existing cache" in {

        withCaching[DocumentsProducedData](Some(correctDocumentsProducedData), formId)

        val body = correctDocumentsProducedMap.toSeq :+ saveAndContinueActionUrlEncoded
        val result = route(app, postRequestFormUrlEncoded(uri, body: _*)).get
        val header = result.futureValue.header

        status(result) must be(SEE_OTHER)
        header.headers.get("Location") must be(Some("/customs-declare-exports/declaration/supplementary/export-items"))
      }
    }
  }
}

object DocumentsProducedControllerSpec {
  val cacheWithMaximumAmountOfHolders = DocumentsProducedData(
    Seq
      .range[Int](100, 200, 1)
      .map(
        elem =>
          DocumentsProduced(Some(elem.toString), Some("1234"), Some("1234"), Some("AB"), Some("1234"), Some("1234"))
      )
  )
}
