/*
 * Copyright 2021 HM Revenue & Customs
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

import play.api.data.Forms.text
import play.api.data.{Form, Forms}
import play.api.libs.json.Json
import services.Countries.allCountries
import utils.validators.forms.FieldValidator._

case class Address(
  fullName: String, // alphanumeric length 1 - 70
  addressLine: String, // alphanumeric length 1 - 70
  townOrCity: String, // alphanumeric length 1 - 35
  postCode: String, // alphanumeric length 1 - 9
  country: String // full country name, convert to 2 upper case alphabetic characters for backend
)

object Address {
  implicit val format = Json.format[Address]

  val mapping = Forms.mapping(
    "fullName" -> text()
      .verifying("declaration.address.fullName.empty", nonEmpty)
      .verifying("declaration.address.fullName.error", isValidAddressField)
      .verifying("declaration.address.fullName.length", noLongerThan(70)),
    "addressLine" -> text()
      .verifying("declaration.address.addressLine.empty", nonEmpty)
      .verifying("declaration.address.addressLine.error", isValidAddressField)
      .verifying("declaration.address.addressLine.length", noLongerThan(70)),
    "townOrCity" -> text()
      .verifying("declaration.address.townOrCity.empty", nonEmpty)
      .verifying("declaration.address.townOrCity.error", isValidAddressField)
      .verifying("declaration.address.townOrCity.length", noLongerThan(35)),
    "postCode" -> text()
      .verifying("declaration.address.postCode.empty", nonEmpty)
      .verifying("declaration.address.postCode.error", isAlphanumericWithSpace)
      .verifying("declaration.address.postCode.length", noLongerThan(9)),
    "country" -> text()
      .verifying("declaration.address.country.empty", nonEmpty)
      .verifying(
        "declaration.address.country.error",
        input => input.isEmpty || allCountries.exists(_.countryName == input) || allCountries.exists(_.countryCode == input)
      )
  )(Address.apply)(Address.unapply)

  def form(): Form[Address] = Form(mapping)
}
