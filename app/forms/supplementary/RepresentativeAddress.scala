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

import play.api.data.Forms.text
import play.api.data.{Form, Forms}
import play.api.libs.json.Json

case class RepresentativeAddress(
  address: Address,
  statusCode: String  //  numeric, [2] or [3]
) {

  def toMetadataProperties(): Map[String, String] = Map(
    "declaration.agent.id" -> address.eori,
    "declaration.agent.name" -> address.fullName,
    "declaration.agent.address.line" -> address.addressLine,
    "declaration.agent.address.cityName" -> address.townOrCity,
    "declaration.agent.address.postcodeId" -> address.postCode,
    "declaration.agent.address.countryCode" -> address.country,
    "declaration.agent.functionCode" -> statusCode
  )

}

object RepresentativeAddress {
  implicit val format = Json.format[RepresentativeAddress]

  private val representativeStatusCodeAllowedValues = Set("2", "3")

  val formId = "RepresentativeStatusCodeId"

  val mapping = Forms.mapping(
    "address" -> Address.addressMapping,
    "statusCode" -> text().verifying("Please, choose your representative status",
      input => input.nonEmpty && representativeStatusCodeAllowedValues(input))
  )(RepresentativeAddress.apply)(RepresentativeAddress.unapply)

  def form(): Form[RepresentativeAddress] = Form(mapping)
}
