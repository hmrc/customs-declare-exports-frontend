/*
 * Copyright 2020 HM Revenue & Customs
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

import play.api.data.Forms.{optional, text}
import play.api.data.{Form, Forms}
import play.api.libs.json.{Json, OFormat}

case class TotalPackageQuantity(totalPackage: Option[String])

object TotalPackageQuantity {
  implicit val format: OFormat[TotalPackageQuantity] = Json.format[TotalPackageQuantity]

  val formId = "TotalPackageQuantity"

  import utils.validators.forms.FieldValidator._

  val mapping = Forms.mapping(
    "totalPackage" -> optional(
      text()
        .verifying("supplementary.totalPackageQuantity.empty", nonEmpty)
        .verifying("supplementary.totalPackageQuantity.error", isEmpty or (isNumeric and noLongerThan(8)))
    )
  )(TotalPackageQuantity.apply)(TotalPackageQuantity.unapply)

  def form(): Form[TotalPackageQuantity] = Form(mapping)
}
