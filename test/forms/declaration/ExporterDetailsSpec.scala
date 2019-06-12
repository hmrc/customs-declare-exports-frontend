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

      metadata.declaration mustBe defined
      metadata.declaration.get.exporter mustBe defined
      metadata.declaration.get.exporter.get.id mustBe defined
      metadata.declaration.get.exporter.get.id.get mustEqual exporterDetails.details.eori.get
      metadata.declaration.get.exporter.get.name mustBe defined
      metadata.declaration.get.exporter.get.name.get mustEqual exporterDetails.details.address.get.fullName
      metadata.declaration.get.exporter.get.address mustBe defined
      metadata.declaration.get.exporter.get.address.get.line mustBe defined
      metadata.declaration.get.exporter.get.address.get.line.get mustEqual exporterDetails.details.address.get.addressLine
      metadata.declaration.get.exporter.get.address.get.cityName mustBe defined
      metadata.declaration.get.exporter.get.address.get.cityName.get mustEqual exporterDetails.details.address.get.townOrCity
      metadata.declaration.get.exporter.get.address.get.postcodeId mustBe defined
      metadata.declaration.get.exporter.get.address.get.postcodeId.get mustEqual exporterDetails.details.address.get.postCode
      metadata.declaration.get.exporter.get.address.get.countryCode mustBe defined
      metadata.declaration.get.exporter.get.address.get.countryCode.get mustEqual countryCode
    }
  }

}

object ExporterDetailsSpec {
  import forms.declaration.EntityDetailsSpec._

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

  val exporterDetailsWithEmptyFullNameJSON: JsValue = JsObject(
    Map("details" -> EntityDetailsSpec.entityDetailsWithEmptyFullNameJSON)
  )
}
