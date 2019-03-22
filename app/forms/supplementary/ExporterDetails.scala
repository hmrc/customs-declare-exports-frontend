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
import forms.MetadataPropertiesConvertable
import play.api.data.{Form, Forms}
import play.api.libs.json.Json
import services.Countries.allCountries

case class ExporterDetails(details: EntityDetails) extends MetadataPropertiesConvertable {

  override def toMetadataProperties(): Map[String, String] =
    Map("declaration.exporter.id" -> details.eori.getOrElse("")) ++ buildAddressProperties()

  private def buildAddressProperties(): Map[String, String] = details.address match {
    case Some(address) =>
      Map(
        "declaration.exporter.name" -> address.fullName,
        "declaration.exporter.address.line" -> address.addressLine,
        "declaration.exporter.address.cityName" -> address.townOrCity,
        "declaration.exporter.address.postcodeId" -> address.postCode,
        "declaration.exporter.address.countryCode" ->
          allCountries.find(country => address.country.contains(country.countryName)).map(_.countryCode).getOrElse("")
      )
    case None => Map.empty
  }

}

object ExporterDetails {
  implicit val format = Json.format[ExporterDetails]

  val id = "ExporterDetails"

  val mapping = Forms.mapping("details" -> EntityDetails.mapping)(ExporterDetails.apply)(ExporterDetails.unapply)

  def form(): Form[ExporterDetails] = Form(mapping)
}
