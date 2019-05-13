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
import forms.declaration.{DeclarantDetails, DeclarantDetailsSpec}
import org.scalatest.mockito.MockitoSugar
import org.scalatest.{Matchers, WordSpec}
import play.api.libs.json.Json
import uk.gov.hmrc.http.cache.client.CacheMap

class DeclarantBuilderSpec extends WordSpec with Matchers with MockitoSugar {

  "DeclarantBuilder" should {
    "build wco declarant successfully " when {
      "only address has been supplied" in {
        implicit val cacheMap: CacheMap =
          CacheMap(
            "CacheID",
            Map(DeclarantDetails.id -> Json.toJson(DeclarantDetailsSpec.correctDeclarantDetailsAddressOnly))
          )

        val declarant = DeclarantBuilder.build(cacheMap)

        declarant.getID should be(null)
        declarant.getName.getValue should be("Full Name")
        declarant.getAddress.getLine.getValue should be("Address Line")
        declarant.getAddress.getCityName.getValue should be("Town or City")
        declarant.getAddress.getPostcodeID.getValue should be("AB12 34CD")
        declarant.getAddress.getCountryCode.getValue should be("PL")
      }

      "only eori has been supplied" in {
        implicit val cacheMap: CacheMap =
          CacheMap(
            "CacheID",
            Map(DeclarantDetails.id -> Json.toJson(DeclarantDetailsSpec.correctDeclarantDetailsEORIOnly))
          )

        val declarant = DeclarantBuilder.build(cacheMap)
        declarant.getID.getValue should be("9GB1234567ABCDEF")
        declarant.getAddress should be(null)
        declarant.getName should be(null)
      }
    }
  }
}
