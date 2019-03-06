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

package forms.supplementary

import base.TestHelper
import org.scalatest.{MustMatchers, WordSpec}
import play.api.libs.json.{JsObject, JsString, JsValue}

class DocumentSpec extends WordSpec with MustMatchers {
  import DocumentSpec._

  "Method toMetadataProperties" should {
    "return proper Metadata Properties" in {
      val previousDocuments = correctPreviousDocument
      val expectedMetadataProperties: Map[String, String] = Map(
        "declaration.goodsShipment.previousDocuments[0].categoryCode" -> previousDocuments.documentCategory,
        "declaration.goodsShipment.previousDocuments[0].typeCode" -> previousDocuments.documentType,
        "declaration.goodsShipment.previousDocuments[0].id" -> previousDocuments.documentReference,
        "declaration.goodsShipment.previousDocuments[0].lineNumeric" -> previousDocuments.goodsItemIdentifier.get
      )

      previousDocuments.toMetadataProperties() must equal(expectedMetadataProperties)
    }
  }
}

object DocumentSpec {
  import forms.supplementary.Document.AllowedValues.TemporaryStorage

  val correctPreviousDocument = Document(
    documentCategory = TemporaryStorage,
    documentType = "ABC",
    documentReference = "DocumentReference",
    goodsItemIdentifier = Some("123")
  )
  val emptyPreviousDocument =
    Document(documentCategory = "", documentType = "", documentReference = "", goodsItemIdentifier = None)

  val incorrectPreviousDocuments = Document(
    documentCategory = "Incorrect category",
    documentType = "Incorrect type",
    documentReference = TestHelper.createRandomString(36),
    goodsItemIdentifier = Some("Incorrect identifier")
  )
  val mandatoryPreviousDocuments = Document(
    documentCategory = TemporaryStorage,
    documentType = "ABC",
    documentReference = "DocumentReference",
    goodsItemIdentifier = None
  )

  val correctPreviousDocumentsJSON: JsValue = JsObject(
    Map(
      "documentCategory" -> JsString(TemporaryStorage),
      "documentType" -> JsString("ABC"),
      "documentReference" -> JsString("DocumentReference"),
      "goodsItemIdentifier" -> JsString("123")
    )
  )
  val emptyPreviousDocumentsJSON: JsValue = JsObject(
    Map(
      "documentCategory" -> JsString(""),
      "documentType" -> JsString(""),
      "documentReference" -> JsString(""),
      "goodsItemIdentifier" -> JsString("")
    )
  )
  val mandatoryPreviousDocumentsJSON: JsValue = JsObject(
    Map(
      "documentCategory" -> JsString(TemporaryStorage),
      "documentType" -> JsString("ABC"),
      "documentReference" -> JsString("DocumentReference"),
      "goodsItemIdentifier" -> JsString("")
    )
  )
  val incorrectPreviousDocumentsJSON: JsValue = JsObject(
    Map(
      "documentCategory" -> JsString("Incorrect category"),
      "documentType" -> JsString("Incorrect type"),
      "documentReference" -> JsString(TestHelper.createRandomString(36)),
      "goodsItemIdentifier" -> JsString("Incorrect identifier")
    )
  )

}
