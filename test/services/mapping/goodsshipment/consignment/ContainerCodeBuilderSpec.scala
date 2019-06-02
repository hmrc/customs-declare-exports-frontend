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
import forms.declaration._
import org.scalatest.{Matchers, WordSpec}
import play.api.libs.json.Json
import uk.gov.hmrc.http.cache.client.CacheMap

class ContainerCodeBuilderSpec extends WordSpec with Matchers {

  "ContainerCodeBuilder" should {
    "correctly map ContainerCode instance" when {
      "there are containers" in {
        implicit val cacheMap: CacheMap =
          CacheMap(
            "CacheID",
            Map(
              TransportDetails.formId ->
                Json.toJson(TransportDetails(Some("Portugal"), true, "40", Some("1234567878ui"), Some("A")))
            )
          )
        val containerCodeType = ContainerCodeBuilder.build
        containerCodeType.getValue should be("1")
      }

      "there are no containers" in {
        implicit val cacheMap: CacheMap =
          CacheMap(
            "CacheID",
            Map(
              TransportDetails.formId ->
                Json.toJson(TransportDetails(Some("Portugal"), false, "40", Some("1234567878ui"), Some("A")))
            )
          )
        val containerCodeType = ContainerCodeBuilder.build
        containerCodeType.getValue should be("0")
      }

    }
  }
}
