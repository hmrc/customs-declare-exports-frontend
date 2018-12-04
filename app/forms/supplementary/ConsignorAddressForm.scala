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

import play.api.data.Forms.{mapping, text}
import play.api.libs.json.Json

case class ConsignorAddressForm(
  eori: String, // alphanumeric, max length 17 characters
  fullName: String, // alphanumeric length 1 - 70
  address: String, // alphanumeric length 1 - 70
  townOrCity: String, // alphanumeric length 1 - 35
  postCode: String, // alphanumeric length 1 - 9
  country: String // 2 upper case alphabetic characters
)

object ConsignorAddressForm {
  implicit val format = Json.format[ConsignorAddressForm]

  val consignorAddressMapping = mapping(
    "eori" -> text(maxLength = 17),
    "fullName" -> text(maxLength = 70),
    "address" -> text(maxLength = 70),
    "townOrCity" -> text(maxLength = 35),
    "postCode" -> text(maxLength = 9),
    "country" -> text(minLength = 1, maxLength = 2)
  )(ConsignorAddressForm.apply)(ConsignorAddressForm.unapply)
}
