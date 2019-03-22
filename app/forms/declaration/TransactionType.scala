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

package forms.declaration

import forms.MetadataPropertiesConvertable
import play.api.data.Forms._
import play.api.data.{Form, Forms}
import play.api.libs.json.Json
import utils.validators.forms.FieldValidator._

case class TransactionType(documentTypeCode: String, identifier: Option[String]) extends MetadataPropertiesConvertable {

  override def toMetadataProperties(): Map[String, String] =
    Map("declaration.goodsShipment.transactionNatureCode" -> (documentTypeCode + identifier.getOrElse("")))
}

object TransactionType {
  implicit val format = Json.format[TransactionType]

  val formId = "TransactionType"

  val mapping = Forms.mapping(
    "documentTypeCode" -> text()
      .verifying("supplementary.transactionType.documentTypeCode.empty", nonEmpty)
      .verifying(
        "supplementary.transactionType.documentTypeCode.error",
        isEmpty or (isNumeric and hasSpecificLength(1))
      ),
    "identifier" -> optional(
      text()
        .verifying("supplementary.transactionType.identifier.error", isNumeric and hasSpecificLength(1))
    )
  )(TransactionType.apply)(TransactionType.unapply)

  def form(): Form[TransactionType] = Form(mapping)
}
