/*
 * Copyright 2018 HM Revenue & Customs
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

import play.api.data.Forms.{mapping, nonEmptyText, text}
import play.api.libs.json.Json

case class AddressForm(
  eori: String, // alphanumeric, max length 17 characters
  fullName: String, // alphanumeric length 1 - 70
  address: String, // alphanumeric length 1 - 70
  townOrCity: String, // alphanumeric length 1 - 35
  postCode: String, // alphanumeric length 1 - 9
  country: String // 2 upper case alphabetic characters
)

object AddressForm {
  implicit val format = Json.format[AddressForm]

  val addressMapping = mapping(
    "eori" -> text().verifying("supplementary.eori.empty", !_.isEmpty)
      .verifying("supplementary.eori.error", _.length <= 17),
    "fullName" -> text().verifying("supplementary.fullName.empty", !_.isEmpty)
      .verifying("supplementary.fullName.error", _.length <= 70),
    "address" -> text().verifying("supplementary.address.empty", !_.isEmpty)
      .verifying("supplementary.address.error", _.length <= 70),
    "townOrCity" -> text().verifying("supplementary.townOrCity.empty", !_.isEmpty)
      .verifying("supplementary.townOrCity.error", _.length <= 35),
    "postCode" -> text().verifying("supplementary.postCode.empty", !_.isEmpty)
      .verifying("supplementary.postCode.error", _.length <= 9),
    "country" -> text().verifying("supplementary.country.empty", !_.isEmpty)
      .verifying("supplementary.country.error", _.length <= 2)
  )(AddressForm.apply)(AddressForm.unapply)
}
