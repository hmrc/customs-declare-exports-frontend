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

import forms.common.{Address, Eori}
import play.api.data.Forms.optional
import play.api.data.{Form, Forms}
import play.api.libs.json.Json

case class EntityDetails(
  eori: Option[Eori], // alphanumeric, max length 17 characters
  address: Option[Address]
)

object EntityDetails {
  implicit val format = Json.format[EntityDetails]

  val optionalMapping = Forms
    .mapping("eori" -> optional(Eori.mapping("supplementary")), "address" -> optional(Address.mapping))(EntityDetails.apply)(EntityDetails.unapply)

  val defaultMapping = optionalMapping.verifying("supplementary.namedEntityDetails.error", validateNamedEntityDetails(_))

  private def validateNamedEntityDetails(namedEntity: EntityDetails): Boolean =
    !(namedEntity.eori.isEmpty && namedEntity.address.isEmpty)

  def form(): Form[EntityDetails] = Form(defaultMapping)
}
