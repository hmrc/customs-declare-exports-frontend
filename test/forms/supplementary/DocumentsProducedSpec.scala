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

import org.scalatest.{MustMatchers, WordSpec}
import play.api.libs.json.{JsObject, JsString, JsValue}

class DocumentsProducedSpec extends WordSpec with MustMatchers {
  import DocumentsProducedSpec._

  "Method toMetadataProperties" should {
    "return proper Metadata Properties" in {
      val documentsProduced = correctDocumentsProduced
      val expectedID = documentsProduced.documentIdentifier.get + documentsProduced.documentPart.get
      val expectedMetadataProperties: Map[String, String] = Map(
        "declaration.goodsShipment.governmentAgencyGoodsItems[0].additionalDocuments[0].categoryCode" -> categoryCode,
        "declaration.goodsShipment.governmentAgencyGoodsItems[0].additionalDocuments[0].typeCode" -> typeCode,
        "declaration.goodsShipment.governmentAgencyGoodsItems[0].additionalDocuments[0].id" -> expectedID,
        "declaration.goodsShipment.governmentAgencyGoodsItems[0].additionalDocuments[0].lpcoExemptionCode" -> documentsProduced.documentStatus.get,
        "declaration.goodsShipment.governmentAgencyGoodsItems[0].additionalDocuments[0].name" -> documentsProduced.documentStatusReason.get,
        "declaration.goodsShipment.governmentAgencyGoodsItems[0].additionalDocuments[0].writeOff.quantity" -> documentsProduced.documentQuantity.get
      )

      documentsProduced.toMetadataProperties() must equal(expectedMetadataProperties)
    }
  }

}

object DocumentsProducedSpec {
  private val categoryCode = "A"
  private val typeCode = "B12"

  val correctDocumentsProduced = DocumentsProduced(
    documentTypeCode = Some(categoryCode + typeCode),
    documentIdentifier = Some("ABCDEF1234567890"),
    documentPart = Some("ABC12"),
    documentStatus = Some("AB"),
    documentStatusReason = Some("DocumentStatusReason"),
    documentQuantity = Some("1234567890.123456")
  )
  val emptyDocumentsProduced = DocumentsProduced(
    documentTypeCode = None,
    documentIdentifier = None,
    documentPart = None,
    documentStatus = None,
    documentStatusReason = None,
    documentQuantity = None
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

}
