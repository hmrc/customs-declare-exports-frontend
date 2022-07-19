/*
 * Copyright 2022 HM Revenue & Customs
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

package forms.common

import connectors.CodeListConnector
import play.api.data.Forms.text
import play.api.data.{Form, Forms, Mapping}
import play.api.i18n.Messages
import play.api.libs.json.Json
import services.Countries._
import utils.validators.forms.FieldValidator._

case class Address(
  fullName: String, // alphanumeric length 1 - 35
  addressLine: String, // alphanumeric length 1 - 35
  townOrCity: String, // alphanumeric length 1 - 35
  postCode: String, // alphanumeric length 1 - 9
  country: String // full country name, convert to 2 upper case alphabetic characters for backend
)

object Address {
  implicit val format = Json.format[Address]

  def mapping(implicit messages: Messages, codeListConnector: CodeListConnector): Mapping[Address] =
    Forms.mapping(
      "fullName" -> text()
        .verifying("declaration.address.fullName.empty", nonEmpty)
        .verifying("declaration.address.fullName.error", isEmpty or isValidAddressField)
        .verifying("declaration.address.fullName.length", isEmpty or noLongerThan(35)),
      "addressLine" -> text()
        .verifying("declaration.address.addressLine.empty", nonEmpty)
        .verifying("declaration.address.addressLine.error", isEmpty or isValidAddressField)
        .verifying("declaration.address.addressLine.length", isEmpty or noLongerThan(70)),
      "townOrCity" -> text()
        .verifying("declaration.address.townOrCity.empty", nonEmpty)
        .verifying("declaration.address.townOrCity.error", isEmpty or isValidAddressField)
        .verifying("declaration.address.townOrCity.length", isEmpty or noLongerThan(35)),
      "postCode" -> text()
        .verifying("declaration.address.postCode.empty", nonEmpty)
        .verifying("declaration.address.postCode.error", isEmpty or isAlphanumericWithSpaceAndHyphen)
        .verifying("declaration.address.postCode.length", isEmpty or noLongerThan(9)),
      "country" -> text()
        .verifying("declaration.address.country.empty", nonEmpty)
        .verifying("declaration.address.country.error", input => input.isEmpty || isValidCountryName(input) || isValidCountryCode(input))
    )(Address.apply)(Address.unapply)

  def form(implicit messages: Messages, codeListConnector: CodeListConnector): Form[Address] = Form(mapping)
}
