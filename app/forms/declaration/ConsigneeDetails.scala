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
import play.api.data.{Form, Forms}
import play.api.libs.json.Json
import services.Countries.allCountries

case class ConsigneeDetails(details: EntityDetails) extends MetadataPropertiesConvertable {

  override def toMetadataProperties(): Map[String, String] =
    Map("declaration.goodsShipment.consignee.id" -> details.eori.getOrElse("")) ++ buildAddressProperties()

  private def buildAddressProperties(): Map[String, String] = details.address match {
    case Some(address) =>
      Map(
        "declaration.goodsShipment.consignee.name" -> address.fullName,
        "declaration.goodsShipment.consignee.address.line" -> address.addressLine,
        "declaration.goodsShipment.consignee.address.cityName" -> address.townOrCity,
        "declaration.goodsShipment.consignee.address.postcodeId" -> address.postCode,
        "declaration.goodsShipment.consignee.address.countryCode" ->
          allCountries.find(country => address.country.contains(country.countryName)).map(_.countryCode).getOrElse("")
      )
    case None => Map.empty
  }
}

object ConsigneeDetails {
  implicit val format = Json.format[ConsigneeDetails]

  val id = "ConsigneeDetails"

  val mapping = Forms.mapping("details" -> EntityDetails.mapping)(ConsigneeDetails.apply)(ConsigneeDetails.unapply)

  def form(): Form[ConsigneeDetails] = Form(mapping)
}
