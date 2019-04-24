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
import forms.common.Address
import forms.declaration.{DeclarantDetails, EntityDetails}
import org.mockito.ArgumentMatchers.{any, eq => eqTo}
import org.mockito.Mockito.when
import org.scalatest.mockito.MockitoSugar
import org.scalatest.{Matchers, WordSpec}
import uk.gov.hmrc.http.cache.client.CacheMap

class DeclarantBuilderSpec extends WordSpec with Matchers with MockitoSugar {

  "DeclarantBuilder" should {
    "build wco declarant successfully given Declarant Details" in {
      implicit val cacheMap = mock[CacheMap]

      val declarantName = "Long Distance Clara"
      val declarantAddressCity = "townOrCity"
      val declarantAddressLine = "addressLine"
      val declarantAddressPostCode = "postCode"

      val declarantAddress = new Address(declarantName, declarantAddressLine, declarantAddressCity, declarantAddressPostCode, "United Kingdom")
      val entityDetails = new EntityDetails(eori = Some("GB12767562756"), Some(declarantAddress))
      val declarantDetails = new DeclarantDetails(entityDetails)

      when(cacheMap.getEntry[DeclarantDetails](eqTo(DeclarantDetails.id))(any()))
        .thenReturn(Some(declarantDetails))

      val mappedDeclarant = DeclarantBuilder.build
      mappedDeclarant.getName.getValue shouldBe declarantName
      val mappedAddress = mappedDeclarant.getAddress
      mappedAddress.getCityName.getValue shouldBe declarantAddressCity
      mappedAddress.getLine.getValue shouldBe declarantAddressLine
      mappedAddress.getPostcodeID.getValue shouldBe declarantAddressPostCode
      mappedAddress.getCountryCode.getValue shouldBe "GB"
    }
  }
}
