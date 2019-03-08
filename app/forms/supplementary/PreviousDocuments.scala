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

import forms.MetadataPropertiesConvertable
import play.api.data.Forms.{optional, text}
import play.api.data.{Form, Forms}
import play.api.libs.json.Json
import utils.validators.forms.FieldValidator._

case class PreviousDocuments(
  documentCategory: String,
  documentType: String,
  documentReference: String,
  goodsItemIdentifier: Option[String]
) extends MetadataPropertiesConvertable {

  override def toMetadataProperties(): Map[String, String] =
    Map(
      "declaration.goodsShipment.previousDocuments[0].categoryCode" -> documentCategory,
      "declaration.goodsShipment.previousDocuments[0].typeCode" -> documentType,
      "declaration.goodsShipment.previousDocuments[0].id" -> documentReference,
      "declaration.goodsShipment.previousDocuments[0].lineNumeric" -> goodsItemIdentifier.getOrElse("")
    )
}

object PreviousDocuments {
  implicit val format = Json.format[PreviousDocuments]

  val formId = "PreviousDocuments"

  val correctDocumentCategories = Set("X", "Y", "Z")

  val mapping = Forms.mapping(
    "documentCategory" -> text()
      .verifying("supplementary.previousDocuments.documentCategory.empty", nonEmpty)
      .verifying(
        "supplementary.previousDocuments.documentCategory.error",
        isEmpty or isContainedIn(correctDocumentCategories)
      ),
    "documentType" -> text()
      .verifying("supplementary.previousDocuments.documentType.empty", nonEmpty)
      .verifying("supplementary.previousDocuments.documentType.error", isEmpty or (isAlphanumeric and noLongerThan(3))),
    "documentReference" -> text()
      .verifying("supplementary.previousDocuments.documentReference.empty", nonEmpty)
      .verifying(
        "supplementary.previousDocuments.documentReference.error",
        isEmpty or (isAlphanumeric and noLongerThan(35))
      ),
    "goodsItemIdentifier" -> optional(
      text().verifying("supplementary.previousDocuments.goodsItemIdentifier.error", isNumeric and noLongerThan(3))
    )
  )(PreviousDocuments.apply)(PreviousDocuments.unapply)

  def form(): Form[PreviousDocuments] = Form(mapping)

  object AllowedValues {
    val TemporaryStorage = "X"
    val SimplifiedDeclaration = "Y"
    val PreviousDocument = "Z"
  }
}
