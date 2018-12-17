/*
 * Copyright 2018 HM Revenue & Customs
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

class RepresentativeDetailsSpec extends WordSpec with MustMatchers {

  private val eori = "GB111222333444"
  private val fullName = "Full name"
  private val addressLine = "Address line"
  private val townOrCity = "Town or City"
  private val postCode = "Postcode"
  private val country = "UK"
  private val statusCode = "2"

  private val representativeAddress: RepresentativeDetails = RepresentativeDetails(
    address = Address(
      eori = eori,
      fullName = fullName,
      addressLine = addressLine,
      townOrCity = townOrCity,
      postCode = postCode,
      country = country
    ),
    statusCode = statusCode
  )

  private val expectedRepresentativeAddressProperties: Map[String, String] = Map(
    "declaration.agent.id" -> eori,
    "declaration.agent.name" -> fullName,
    "declaration.agent.address.line" -> addressLine,
    "declaration.agent.address.cityName" -> townOrCity,
    "declaration.agent.address.postcodeId" -> postCode,
    "declaration.agent.address.countryCode" -> country,
    "declaration.agent.functionCode" -> statusCode
  )


  "RepresentativeAddress" should {
    "convert itself to representative address properties" in {
      representativeAddress.toMetadataProperties() must equal(expectedRepresentativeAddressProperties)
    }
  }
}
