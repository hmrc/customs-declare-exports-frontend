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

import models.declaration.supplementary.DocumentsProducedData
import org.scalatest.{MustMatchers, WordSpec}
import play.api.libs.json.{JsArray, JsObject, JsString, JsValue}

class DocumentsProducedSpec extends WordSpec with MustMatchers {
  "Documents Produced object" should {
    "contains correct limit value" in {
      DocumentsProducedData.maxNumberOfItems must be(99)
    }
  }

}

object DocumentsProducedSpec {
  private val categoryCode = "A"
  private val typeCode = "B12"

  val correctDocumentsProducedData = DocumentsProducedData(
    Seq(
      DocumentsProduced(
        documentTypeCode = Some(categoryCode + typeCode),
        documentIdentifier = Some("ABCDEF1234567890"),
        documentPart = Some("ABC12"),
        documentStatus = Some("AB"),
        documentStatusReason = Some("DocumentStatusReason"),
        documentQuantity = Some("1234567890.123456")
      )
    )
  )

  val emptyDocumentsProduced = DocumentsProduced(
    documentTypeCode = None,
    documentIdentifier = None,
    documentPart = None,
    documentStatus = None,
    documentStatusReason = None,
    documentQuantity = None
  )

  val correctDocumentsProducedMap: Map[String, String] = Map(
    "documentTypeCode" -> "AB13",
    "documentIdentifier" -> "ABCDEF1234567890",
    "documentPart" -> "ABC12",
    "documentStatus" -> "AB",
    "documentStatusReason" -> "DocumentStatusReason",
    "documentQuantity" -> "1234567890.123456"
  )

  val correctDocumentsProducedJSON: JsValue = JsObject(
    Map(
      "documentTypeCode" -> JsString(categoryCode + typeCode),
      "documentIdentifier" -> JsString("ABCDEF1234567890"),
      "documentPart" -> JsString("ABC12"),
      "documentStatus" -> JsString("AB"),
      "documentStatusReason" -> JsString("DocumentStatusReason"),
      "documentQuantity" -> JsString("1234567890.123456")
    )
  )

  val correctDocumentsProducedDataJSON: JsValue = JsObject(
    Map("documents" -> JsArray(Seq(correctDocumentsProducedJSON)))
  )

  val emptyDocumentsProducedMap: Map[String, String] = Map(
    "documentTypeCode" -> "",
    "documentIdentifier" -> "",
    "documentPart" -> "",
    "documentStatus" -> "",
    "documentStatusReason" -> "",
    "documentQuantity" -> ""
  )

  val emptyDocumentsProducedJSON: JsValue = JsObject(
    Map(
      "documentTypeCode" -> JsString(""),
      "documentIdentifier" -> JsString(""),
      "documentPart" -> JsString(""),
      "documentStatus" -> JsString(""),
      "documentStatusReason" -> JsString(""),
      "documentQuantity" -> JsString("")
    )
  )

  val emptyDocumentsProducedDataJSON: JsValue = JsObject(Map("documents" -> JsArray(Seq(correctDocumentsProducedJSON))))
}
