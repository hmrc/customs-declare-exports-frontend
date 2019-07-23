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

package services.mapping.goodsshipment.consignment
import forms.declaration.BorderTransport
import org.scalatest.{Matchers, WordSpec}
import play.api.libs.json.Json
import uk.gov.hmrc.http.cache.client.CacheMap

class DepartureTransportMeansBuilderSpec extends WordSpec with Matchers {
  "DepartureTransportMeansBuilder" should {
    "correctly map DepartureTransportMeans instance" in {
      implicit val cacheMap: CacheMap =
        CacheMap(
          "CacheID",
          Map(
            BorderTransport.formId ->
              Json.toJson(BorderTransport("3", "10", Some("123112yu78")))
          )
        )
      val departureTransportMeans = DepartureTransportMeansBuilder.build
      departureTransportMeans.getID.getValue should be("123112yu78")
      departureTransportMeans.getIdentificationTypeCode.getValue should be("10")
      departureTransportMeans.getName should be(null)
      departureTransportMeans.getTypeCode should be(null)
      departureTransportMeans.getModeCode should be(null)
    }

    "return null when  meansOfTransportOnDepartureIDNumber & meansOfTransportOnDepartureType are not set" in {
      implicit val cacheMap: CacheMap =
        CacheMap(
          "CacheID",
          Map(
            BorderTransport.formId ->
              Json.toJson(BorderTransport("3", "", None))
          )
        )
      val departureTransportMeans = DepartureTransportMeansBuilder.build
      departureTransportMeans should be(null)
    }
  }
}
