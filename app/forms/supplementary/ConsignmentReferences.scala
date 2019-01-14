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

import forms.Ducr
import play.api.data.Forms.{optional, text}
import play.api.data.{Form, Forms}
import play.api.libs.json.Json
import utils.validators.FormFieldValidator.{hasSpecificLength, isAlphanumeric}

case class ConsignmentReferences(
  ducr: Option[Ducr],
  lrn: String // alphanumeric, up to 22 characters
) {

  def toMetadataProperties(): Map[String, String] =
    Map(
      "declaration.goodsShipment.ucr.traderAssignedReferenceId" -> ducr.getOrElse(Ducr("")).ducr,
      "declaration.functionalReferenceId" -> lrn
    )
}

object ConsignmentReferences {
  implicit val format = Json.format[ConsignmentReferences]

  private val lrnMaxLength = 22
  val mapping = Forms.mapping(
    "ducr" -> optional(Ducr.ducrMapping),
    "lrn" -> text()
      .verifying("supplementary.consignmentReferences.lrn.error.empty", _.trim.nonEmpty)
      .verifying("supplementary.consignmentReferences.lrn.error.length", hasSpecificLength(lrnMaxLength))
      .verifying("supplementary.consignmentReferences.lrn.error.specialCharacter", isAlphanumeric)
  )(ConsignmentReferences.apply)(ConsignmentReferences.unapply)

  val id = "ConsignmentReferences"

  def form(): Form[ConsignmentReferences] = Form(mapping)
}
