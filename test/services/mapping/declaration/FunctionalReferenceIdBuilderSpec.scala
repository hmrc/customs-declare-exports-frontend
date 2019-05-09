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
import forms.declaration.{ConsignmentReferences, ConsignmentReferencesSpec}
import org.scalatest.{Matchers, WordSpec}
import uk.gov.hmrc.http.cache.client.CacheMap

class FunctionalReferenceIdBuilderSpec extends WordSpec with Matchers {

  "FunctionalReferenceIdBuilder" should {
    "correctly map to the WCO-DEC FunctionalReferenceId instance" in {
      implicit val cacheMap: CacheMap =
        CacheMap("CacheID", Map(ConsignmentReferences.id -> ConsignmentReferencesSpec.correctConsignmentReferencesJSON))
      val referenceIDType = FunctionalReferenceIdBuilder.build(cacheMap)
      referenceIDType.getValue should be("123LRN")
    }
  }
}
