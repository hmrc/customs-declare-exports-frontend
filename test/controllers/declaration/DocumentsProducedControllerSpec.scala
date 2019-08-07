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

package controllers.declaration

import base.{CustomExportsBaseSpec, TestHelper, ViewValidator}
import controllers.util.{Add, Remove, SaveAndContinue}
import forms.Choice
import forms.Choice.AllowedChoiceValues.SupplementaryDec
import forms.declaration.DocumentsProducedSpec
import forms.declaration.DocumentsProducedSpec.{correctDocumentsProducedMap, _}
import forms.declaration.additionaldocuments.DocumentIdentifierAndPart.{documentIdentifierKey, documentPartKey}
import forms.declaration.additionaldocuments.DocumentWriteOff.documentQuantityKey
import forms.declaration.additionaldocuments.DocumentsProduced._
import helpers.views.declaration.{CommonMessages, DocumentsProducedMessages}
import models.declaration.DocumentsProducedData
import models.declaration.DocumentsProducedDataSpec._
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito._
import play.api.libs.json.{JsObject, JsString, JsValue}
import play.api.test.Helpers._
import services.cache.{ExportItem, ExportsCacheModel, ExportsItemBuilder}

class DocumentsProducedControllerSpec
    extends CustomExportsBaseSpec with DocumentsProducedMessages with CommonMessages with ViewValidator with ExportsItemBuilder {

  import DocumentsProducedControllerSpec._

  val exampleItem: ExportItem = aCachedItem()

  val exampleModel: ExportsCacheModel = aCacheModel(withChoice(SupplementaryDec), withItem(exampleItem))

  private def uri(item: ExportItem) = uriWithContextPath(s"/declaration/items/${item.id}/add-document")

  private val exampleUri = uri(exampleItem)

  private val addActionUrlEncoded = (Add.toString, "")
  private val saveAndContinueActionUrlEncoded = (SaveAndContinue.toString, "")

  override def beforeEach() {
    super.beforeEach()
    authorizedUser()
    withNewCaching(exampleModel)
  }

  override def afterEach() {
    super.afterEach()
    reset(mockAuthConnector, mockExportsCacheService)
  }

  private def removeActionUrlEncoded(value: String) = (Remove.toString, value)

  "Documents Produced Controller on GET" should {

    "return 200 with a success" in {
      val result = route(app, getRequest(exampleUri, sessionId = exampleModel.sessionId)).get

      status(result) must be(OK)
    }

    "read item from cache and display it" in {

      val document = DocumentsProducedSpec.correctDocumentsProduced
      val item = ExportItem(id = "id", documentsProducedData = Some(DocumentsProducedData(Seq(document))))
      val cacheModel = aCacheModel(withItem(item), withChoice(Choice.AllowedChoiceValues.SupplementaryDec))

      withNewCaching(cacheModel)

      val result = route(app, getRequest(uri(item), sessionId = cacheModel.sessionId)).get
      val view = contentAsString(result)

      status(result) must be(OK)

      getElementByCss(view, "table.form-group>tbody:nth-child(2)>tr:nth-child(1)>td:nth-child(1)").text() must equal(
        correctDocumentsProduced.documentTypeCode.get
      )
      getElementByCss(view, "table.form-group>tbody:nth-child(2)>tr:nth-child(1)>td:nth-child(2)").text() must equal(
        correctDocumentsProduced.documentIdentifierAndPart.get.documentIdentifier.get
      )
      getElementByCss(view, "table.form-group>tbody:nth-child(2)>tr:nth-child(1)>td:nth-child(3)").text() must equal(
        correctDocumentsProduced.documentIdentifierAndPart.get.documentPart.get
      )
      getElementByCss(view, "table.form-group>tbody:nth-child(2)>tr:nth-child(1)>td:nth-child(4)").text() must equal(
        correctDocumentsProduced.documentStatus.get
      )
      getElementByCss(view, "table.form-group>tbody:nth-child(2)>tr:nth-child(1)>td:nth-child(5)").text() must equal(
        correctDocumentsProduced.documentStatusReason.get
      )
      getElementByCss(view, "table.form-group>tbody:nth-child(2)>tr:nth-child(1)>td:nth-child(6)").text() must equal(
        correctDocumentsProduced.issuingAuthorityName.get
      )
      getElementByCss(view, "table.form-group>tbody:nth-child(2)>tr:nth-child(1)>td:nth-child(7)").text() must equal(
        correctDocumentsProduced.dateOfValidity.get.toString
      )
      getElementByCss(view, "table.form-group>tbody:nth-child(2)>tr:nth-child(1)>td:nth-child(8)").text() must equal(
        correctDocumentsProduced.documentWriteOff.get.measurementUnit.get
      )
      getElementByCss(view, "table.form-group>tbody:nth-child(2)>tr:nth-child(1)>td:nth-child(9)").text() must equal(
        correctDocumentsProduced.documentWriteOff.get.documentQuantity.get.toString
      )
    }
  }

  "Documents Produced Controller on POST" should {

    "display page with errors" when {

      "provided with incorrect document type code" in {
        val incorrectDocumentTypeCode: JsValue = JsObject(Map("documentTypeCode" -> JsString("abcdf")))

        val result = route(app, postRequest(exampleUri, incorrectDocumentTypeCode, sessionId = exampleModel.sessionId)).get
        status(result) must be(BAD_REQUEST)
        contentAsString(result) must include(messages(documentTypeCodeError))
        verifyTheCacheIsUnchanged()
      }

      "provided with incorrect document identifier" in {
        val incorrectDocumentIdentifier: JsValue =
          JsObject(
            Map(
              s"$documentIdentifierAndPartKey.$documentIdentifierKey" -> JsString(
                TestHelper.createRandomAlphanumericString(31)
              )
            )
          )

        val result = route(app, postRequest(exampleUri, incorrectDocumentIdentifier, sessionId = exampleModel.sessionId)).get
        status(result) must be(BAD_REQUEST)
        contentAsString(result) must include(messages(documentIdentifierError))

        verifyTheCacheIsUnchanged()
      }

      "provided with incorrect document part" in {
        val incorrectDocumentPart: JsValue =
          JsObject(
            Map(
              s"$documentIdentifierAndPartKey.$documentPartKey" -> JsString(
                TestHelper.createRandomAlphanumericString(6)
              )
            )
          )

        val result = route(app, postRequest(exampleUri, incorrectDocumentPart, sessionId = exampleModel.sessionId)).get
        status(result) must be(BAD_REQUEST)
        contentAsString(result) must include(messages(documentPartError))

        verifyTheCacheIsUnchanged()
      }

      "provided with incorrect document status" in {
        val incorrectDocumentStatus: JsValue = JsObject(Map(documentStatusKey -> JsString("as")))

        val result = route(app, postRequest(exampleUri, incorrectDocumentStatus, sessionId = exampleModel.sessionId)).get
        status(result) must be(BAD_REQUEST)
        contentAsString(result) must include(messages(documentStatusError))

        verifyTheCacheIsUnchanged()
      }

      "provided with incorrect document status reason" in {
        val incorrectDocumentStatusReason: JsValue =
          JsObject(Map(documentStatusReasonKey -> JsString(TestHelper.createRandomAlphanumericString(36))))

        val result = route(app, postRequest(exampleUri, incorrectDocumentStatusReason, sessionId = exampleModel.sessionId)).get
        status(result) must be(BAD_REQUEST)
        contentAsString(result) must include(messages(documentStatusReasonError))

        verifyTheCacheIsUnchanged()
      }

      "provided with incorrect documents quantity" in {
        val incorrectDocumentQuantity: JsValue =
          JsObject(Map(s"$documentWriteOffKey.$documentQuantityKey" -> JsString("123456789012123.1234567")))

        val result = route(app, postRequest(exampleUri, incorrectDocumentQuantity, sessionId = exampleModel.sessionId)).get
        status(result) must be(BAD_REQUEST)
        contentAsString(result) must include(messages(documentQuantityPrecisionError))

        verifyTheCacheIsUnchanged()

      }

      "try to add duplicated document" in {
        val cachedData = ExportItem(id = "id", documentsProducedData = Some(correctDocumentsProducedData))
        val model = aCacheModel(withItem(cachedData), withChoice(Choice.AllowedChoiceValues.SupplementaryDec))
        withNewCaching(model)

        val duplicatedDocument: Map[String, String] = correctDocumentsProducedMap

        val body = duplicatedDocument.toSeq :+ addActionUrlEncoded
        val result = route(app, postRequestFormUrlEncoded(exampleUri, model.sessionId)(body: _*)).get
        val page = contentAsString(result)

        status(result) must be(BAD_REQUEST)

        checkErrorsSummary(page)
        checkErrorLink(page, 1, duplicatedItemError, "#")

        verifyTheCacheIsUnchanged()
      }

      "try to add an empty document" in {

        val undefinedDocument: Map[String, String] = emptyDocumentsProducedMap

        val body = undefinedDocument.toSeq :+ addActionUrlEncoded
        val result = route(app, postRequestFormUrlEncoded(exampleUri, exampleModel.sessionId)(body: _*)).get
        val page = contentAsString(result)

        status(result) must be(BAD_REQUEST)

        checkErrorsSummary(page)
        checkErrorLink(page, 1, notDefinedError, "#")
        verifyTheCacheIsUnchanged()
      }

      "try to add more then 99 documents" in {
        val cachedData = ExportItem(id = "id", documentsProducedData = Some(cacheWithMaximumAmountOfHolders))
        val model = aCacheModel(withItem(cachedData), withChoice(Choice.AllowedChoiceValues.SupplementaryDec))
        withNewCaching(model)

        val body = (correctDocumentsProducedMap + ("documentIdentifier" -> "Davis")).toSeq :+ addActionUrlEncoded
        val result = route(app, postRequestFormUrlEncoded(exampleUri, model.sessionId)(body: _*)).get
        val page = contentAsString(result)

        status(result) must be(BAD_REQUEST)

        checkErrorsSummary(page)
        checkErrorLink(page, 1, maximumAmountReachedError, "#")

        verifyTheCacheIsUnchanged()
      }
    }

    "add a document successfully" when {

      "cache is empty" in {

        val body = correctDocumentsProducedMap.toSeq :+ addActionUrlEncoded
        val result = route(app, postRequestFormUrlEncoded(exampleUri, exampleModel.sessionId)(body: _*)).get

        status(result) must be(SEE_OTHER)

        verify(mockExportsCacheService, times(2)).get(any[String])
        verify(mockExportsCacheService).update(any[String], any[ExportsCacheModel])
      }

      "that does not exist in cache" in {

        val newDocument = correctDocumentsProducedMap + (s"$documentIdentifierAndPartKey.$documentIdentifierKey" -> "DOCID123")
        val body = newDocument.toSeq :+ addActionUrlEncoded
        val result = route(app, postRequestFormUrlEncoded(exampleUri, exampleModel.sessionId)(body: _*)).get

        status(result) must be(SEE_OTHER)

        verify(mockExportsCacheService, times(2)).get(any[String])
        verify(mockExportsCacheService).update(any[String], any[ExportsCacheModel])
      }
    }

    "remove a document successfully" when {

      "exists in cache" in {

        val firstElementIndex = "0"
        val body = removeActionUrlEncoded(firstElementIndex)
        val result = route(app, postRequestFormUrlEncoded(exampleUri, exampleModel.sessionId)(body)).get

        status(result) must be(OK)

        verify(mockExportsCacheService, times(2)).get(any[String])
        verify(mockExportsCacheService).update(any[String], any[ExportsCacheModel])
      }
    }

    "redirect to 'Export Items' page" when {

      "provided with empty form and with empty cache" in {

        val body = emptyDocumentsProducedMap.toSeq :+ saveAndContinueActionUrlEncoded
        val result = route(app, postRequestFormUrlEncoded(exampleUri, exampleModel.sessionId)(body: _*)).get

        status(result) must be(SEE_OTHER)
        redirectLocation(result) must be(Some("/customs-declare-exports/declaration/export-items"))
        verifyTheCacheIsUnchanged()
      }

      "provided with empty form and with existing cache" in {

        val body = emptyDocumentsProducedMap.toSeq :+ saveAndContinueActionUrlEncoded
        val result = route(app, postRequestFormUrlEncoded(exampleUri, exampleModel.sessionId)(body: _*)).get

        status(result) must be(SEE_OTHER)
        redirectLocation(result) must be(Some("/customs-declare-exports/declaration/export-items"))

        verifyTheCacheIsUnchanged()
      }

      "provided with a valid document and with empty cache" in {

        val body = correctDocumentsProducedMap.toSeq :+ saveAndContinueActionUrlEncoded
        val result = route(app, postRequestFormUrlEncoded(exampleUri, exampleModel.sessionId)(body: _*)).get

        status(result) must be(SEE_OTHER)
        redirectLocation(result) must be(Some("/customs-declare-exports/declaration/export-items"))

        verify(mockExportsCacheService, times(2)).get(any[String])
        verify(mockExportsCacheService).update(any[String], any[ExportsCacheModel])
      }

      "provided with a valid document and with existing cache" in {

        val newDocument = correctDocumentsProducedMap + (s"$documentIdentifierAndPartKey.$documentIdentifierKey" -> "DOCID123")
        val body = newDocument.toSeq :+ saveAndContinueActionUrlEncoded
        val result = route(app, postRequestFormUrlEncoded(exampleUri, exampleModel.sessionId)(body: _*)).get

        status(result) must be(SEE_OTHER)
        redirectLocation(result) must be(Some("/customs-declare-exports/declaration/export-items"))

        verify(mockExportsCacheService, times(2)).get(any[String])
        verify(mockExportsCacheService).update(any[String], any[ExportsCacheModel])
      }
    }
  }
}

object DocumentsProducedControllerSpec {
  val cacheWithMaximumAmountOfHolders = DocumentsProducedData(
    Seq
      .range[Int](100, 200, 1)
      .map(elem => correctDocumentsProduced.copy(documentTypeCode = Some(elem.toString)))
  )
}
