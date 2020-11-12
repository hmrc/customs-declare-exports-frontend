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
import play.api.data.{Form, Forms, Mapping}
import play.api.libs.json.Json
import utils.validators.forms.FieldValidator._

case class EntityDetails(
  eori: Option[Eori], // alphanumeric, max length 17 characters
  address: Option[Address]
)

object EntityDetails {
  implicit val format = Json.format[EntityDetails]

  val optionalMapping = Forms
    .mapping("eori" -> optional(Eori.mapping), "address" -> optional(Address.mapping))(EntityDetails.apply)(EntityDetails.unapply)

  val defaultMapping = optionalMapping
    .verifying("declaration.namedEntityDetails.error", validateNamedEntityDetails(_))

  val eitherEoriOrAddressMapping = defaultMapping
    .verifying(
      "declaration.carrier.error.addressAndEori",
      details => !validateNamedEntityDetails(details) or (details.eori.isDefined ^ details.address.isDefined)
    )

  val eitherEoriOrAddressOptionalMapping = optionalMapping
    .verifying("declaration.carrier.error.addressAndEori", details => details.eori.isEmpty || details.address.isEmpty)

  val addressMapping: Mapping[EntityDetails] = Forms
    .mapping("address" -> Address.mapping)(address => EntityDetails(None, Some(address)))(entityDetails => entityDetails.address)

  val optionalAddressMapping: Mapping[EntityDetails] = Forms
    .mapping("address" -> optional(Address.mapping))(maybeAddress => EntityDetails(None, maybeAddress))(entityDetails => Some(entityDetails.address))

  private def validateNamedEntityDetails(namedEntity: EntityDetails): Boolean =
    !(namedEntity.eori.isEmpty && namedEntity.address.isEmpty)

  def form(): Form[EntityDetails] = Form(defaultMapping)
}
