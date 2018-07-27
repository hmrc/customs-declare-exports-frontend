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

package forms

import forms.behaviours.StringFieldBehaviours
import play.api.data.FormError

class RepresentativesAddressFormProviderSpec extends StringFieldBehaviours {

  val form = new RepresentativesAddressFormProvider()()

  "Representatives Address Form Provider" should {
    "bind valid data" in {
      val result = form.bind(
        Map(
          "fullName" -> "Full name",
          "building" -> "Building",
          "street" -> "Street",
          "townOrCity" -> "Town",
          "county" -> "County",
          "postcode" -> "Postcode",
          "country" -> "Country"
        )
      )

      result.apply("fullName").value.map(_ shouldBe "Full name")
      result.apply("building").value.map(_ shouldBe "Building")
      result.apply("street").value.map(_ shouldBe "Street")
      result.apply("townOrCity").value.map(_ shouldBe "Town")
      result.apply("county").value.map(_ shouldBe "County")
      result.apply("postcode").value.map(_ shouldBe "Postcode")
      result.apply("country").value.map(_ shouldBe "Country")
    }

    "return error if required fields are empty" in {
      val data = Map(
        "fullName" -> "",
        "building" -> "",
        "street" -> "",
        "townOrCity" -> "",
        "county" -> "",
        "postcode" -> "",
        "country" -> ""
      )

      val emptyFullNameError = FormError("fullName", "address.error.required.fullName")
      val emptyBuildingError = FormError("building", "address.error.required.building")
      val emptyStreetError = FormError("street", "address.error.required.street")
      val emptyTownOrCityError = FormError("townOrCity", "address.error.required.townOrCity")
      val emptyPostcodeError = FormError("postcode", "address.error.required.postcode")
      val emptyCountryError = FormError("country", "address.error.required.country")

      val expectedErrors = Seq(emptyFullNameError, emptyBuildingError, emptyStreetError, emptyTownOrCityError,
        emptyPostcodeError, emptyCountryError)

      checkForError(form, data, expectedErrors)
    }
  }
}
