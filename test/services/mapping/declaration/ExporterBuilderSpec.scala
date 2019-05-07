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

package services.mapping.declaration

import forms.declaration.{ExporterDetails, ExporterDetailsSpec}
import org.scalatest.{Matchers, WordSpec}
import uk.gov.hmrc.http.cache.client.CacheMap

class ExporterBuilderSpec extends WordSpec with Matchers {

  "ExporterBuilder" should {
    "correctly map to the WCO-DEC Exporter instance" when {
      "all data is supplied" in {
        implicit val cacheMap: CacheMap =
          CacheMap("CacheID", Map(ExporterDetails.id -> ExporterDetailsSpec.correctExporterDetailsJSON))
        val exporter = ExporterBuilder.build(cacheMap)
        exporter.getID.getValue should be("9GB1234567ABCDEF")
        exporter.getName.getValue should be("Full Name")
        exporter.getAddress.getLine.getValue should be("Address Line")
        exporter.getAddress.getCityName.getValue should be("Town or City")
        exporter.getAddress.getCountryCode.getValue should be("PL")
        exporter.getAddress.getPostcodeID.getValue should be("AB12 34CD")
      }
      "fullname is not supplied" in {
        implicit val cacheMap: CacheMap =
          CacheMap("CacheID", Map(ExporterDetails.id -> ExporterDetailsSpec.exporterDetailsWithEmptyFullNameJSON))
        val exporter = ExporterBuilder.build(cacheMap)
        exporter.getID.getValue should be("9GB1234567ABCDEF")
        exporter.getName should be(null)
        exporter.getAddress.getLine.getValue should be("Address Line")
        exporter.getAddress.getCityName.getValue should be("Town or City")
        exporter.getAddress.getCountryCode.getValue should be("PL")
        exporter.getAddress.getPostcodeID.getValue should be("AB12 34CD")
      }
    }
  }
}
