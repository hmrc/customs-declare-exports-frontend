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

import org.scalatest.{MustMatchers, WordSpec}
import play.api.libs.json.{JsObject, JsValue}
import uk.gov.hmrc.wco.dec.MetaData

class ExporterDetailsSpec extends WordSpec with MustMatchers {
  import ExporterDetailsSpec._

  "Method toMetadataProperties" should {
    "return proper Metadata Properties" in {
      val exporterDetails = correctExporterDetails
      val countryCode = "PL"

      val metadata = MetaData.fromProperties(exporterDetails.toMetadataProperties())

      metadata.declaration must be(defined)
      metadata.declaration.get.exporter must be(defined)
      metadata.declaration.get.exporter.get.id must be(defined)
      metadata.declaration.get.exporter.get.id.get must equal(exporterDetails.details.eori.get)
      metadata.declaration.get.exporter.get.name must be(defined)
      metadata.declaration.get.exporter.get.name.get must equal(exporterDetails.details.address.get.fullName)
      metadata.declaration.get.exporter.get.address must be(defined)
      metadata.declaration.get.exporter.get.address.get.line must be(defined)
      metadata.declaration.get.exporter.get.address.get.line.get must equal(exporterDetails.details.address.get.addressLine)
      metadata.declaration.get.exporter.get.address.get.cityName must be(defined)
      metadata.declaration.get.exporter.get.address.get.cityName.get must equal(exporterDetails.details.address.get.townOrCity)
      metadata.declaration.get.exporter.get.address.get.postcodeId must be(defined)
      metadata.declaration.get.exporter.get.address.get.postcodeId.get must equal(exporterDetails.details.address.get.postCode)
      metadata.declaration.get.exporter.get.address.get.countryCode must be(defined)
      metadata.declaration.get.exporter.get.address.get.countryCode.get must equal(countryCode)
    }
  }


}

object ExporterDetailsSpec {
  import forms.supplementary.EntityDetailsSpec._

  val correctExporterDetails = ExporterDetails(details = EntityDetailsSpec.correctEntityDetails)
  val correctExporterDetailsEORIOnly = ExporterDetails(details = EntityDetailsSpec.correctEntityDetailsEORIOnly)
  val correctExporterDetailsAddressOnly = ExporterDetails(details = EntityDetailsSpec.correctEntityDetailsAddressOnly)
  val incorrectExporterDetails = ExporterDetails(details = EntityDetailsSpec.incorrectEntityDetails)
  val emptyExporterDetails = ExporterDetails(details = EntityDetailsSpec.emptyEntityDetails)

  val correctExporterDetailsJSON: JsValue = JsObject(Map("details" -> correctEntityDetailsJSON))
  val correctExporterDetailsEORIOnlyJSON: JsValue = JsObject(Map("details" -> correctEntityDetailsEORIOnlyJSON))
  val correctExporterDetailsAddressOnlyJSON: JsValue = JsObject(Map("details" -> correctEntityDetailsAddressOnlyJSON))
  val incorrectExporterDetailsJSON: JsValue = JsObject(Map("details" -> incorrectEntityDetailsJSON))
  val emptyExporterDetailsJSON: JsValue = JsObject(Map("details" -> emptyEntityDetailsJSON))
}
