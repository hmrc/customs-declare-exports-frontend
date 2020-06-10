/*
 * Copyright 2020 HM Revenue & Customs
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

package forms.common

import base.TestHelper._
import forms.common.AddressSpec.addressWithEmptyFullname
import org.scalatest.{MustMatchers, WordSpec}

class AddressSpec extends WordSpec with MustMatchers {
  import AddressSpec._

  "Bound Form with Address mapping" should {

    "contain errors for fullName" when {

      "provided with empty input" in {

        val input = buildAddressInputMap()
        val form = Address.form().bind(input)

        val fullNameError = form.errors.find(_.key == "fullName")
        fullNameError must be(defined)
        fullNameError.get.message must equal("declaration.address.fullName.empty")
      }

      "provided with input longer than 70 characters" in {

        val input = buildAddressInputMap(fullName = createRandomAlphanumericString(71))
        val form = Address.form().bind(input)

        val fullNameError = form.errors.find(_.key == "fullName")
        fullNameError must be(defined)
        fullNameError.get.message must equal("declaration.address.fullName.error")
      }

      "provided with input containing special characters" in {
        val input = buildAddressInputMap(fullName = "FullName!@#")
        val form = Address.form().bind(input)

        val fullNameError = form.errors.find(_.key == "fullName")
        fullNameError must be(defined)
        fullNameError.get.message must equal("declaration.address.fullName.error")
      }
    }

    "contain errors for addressLine" when {
      "provided with empty input" in {
        val input = buildAddressInputMap()
        val form = Address.form().bind(input)

        val addressLineError = form.errors.find(_.key == "addressLine")
        addressLineError must be(defined)
        addressLineError.get.message must equal("declaration.address.addressLine.empty")
      }

      "provided with input longer than 70 characters" in {
        val input = buildAddressInputMap(addressLine = createRandomAlphanumericString(71))
        val form = Address.form().bind(input)

        val addressLineError = form.errors.find(_.key == "addressLine")
        addressLineError must be(defined)
        addressLineError.get.message must equal("declaration.address.addressLine.error")
      }

      "provided with input containing special characters" in {
        val input = buildAddressInputMap(addressLine = "Address!@#")
        val form = Address.form().bind(input)

        val addressLineError = form.errors.find(_.key == "addressLine")
        addressLineError must be(defined)
        addressLineError.get.message must equal("declaration.address.addressLine.error")
      }
    }

    "contain errors for townOrCity" when {
      "provided with empty input" in {
        val input = buildAddressInputMap()
        val form = Address.form().bind(input)

        val townOrCityError = form.errors.find(_.key == "townOrCity")
        townOrCityError must be(defined)
        townOrCityError.get.message must equal("declaration.address.townOrCity.empty")
      }

      "provided with input longer than 35 characters" in {
        val input = buildAddressInputMap(townOrCity = createRandomAlphanumericString(36))
        val form = Address.form().bind(input)

        val townOrCityError = form.errors.find(_.key == "townOrCity")
        townOrCityError must be(defined)
        townOrCityError.get.message must equal("declaration.address.townOrCity.error")
      }

      "provided with input containing special characters" in {
        val input = buildAddressInputMap(townOrCity = "City%$#")
        val form = Address.form().bind(input)

        val townOrCityError = form.errors.find(_.key == "townOrCity")
        townOrCityError must be(defined)
        townOrCityError.get.message must equal("declaration.address.townOrCity.error")
      }
    }

    "contain errors for postCode" when {
      "provided with empty input" in {
        val input = buildAddressInputMap()
        val form = Address.form().bind(input)

        val postCodeError = form.errors.find(_.key == "postCode")
        postCodeError must be(defined)
        postCodeError.get.message must equal("declaration.address.postCode.empty")
      }

      "provided with input in wrong format" in {
        val input = buildAddressInputMap(postCode = "AB 12 CD 345")
        val form = Address.form().bind(input)

        val postCodeError = form.errors.find(_.key == "postCode")
        postCodeError must be(defined)
        postCodeError.get.message must equal("declaration.address.postCode.error")
      }

      "provided with input containing special characters" in {
        val input = buildAddressInputMap(postCode = "AB*^ $%7")
        val form = Address.form().bind(input)

        val postCodeError = form.errors.find(_.key == "postCode")
        postCodeError must be(defined)
        postCodeError.get.message must equal("declaration.address.postCode.error")
      }
    }

    "contain errors for country" when {
      "provided with empty input" in {
        val input = buildAddressInputMap()
        val form = Address.form().bind(input)

        val countryError = form.errors.find(_.key == "country")
        countryError must be(defined)
        countryError.get.message must equal("declaration.address.country.empty")
      }

      "provided with non-existing country name" in {
        val input = buildAddressInputMap(country = "Non Existing Country")
        val form = Address.form().bind(input)

        val countryError = form.errors.find(_.key == "country")
        countryError must be(defined)
        countryError.get.message must equal("declaration.address.country.error")
      }

      "provided with input containing special characters" in {
        val input = buildAddressInputMap(country = "United King@#$")
        val form = Address.form().bind(input)

        val countryError = form.errors.find(_.key == "country")
        countryError must be(defined)
        countryError.get.message must equal("declaration.address.country.error")
      }
    }

    "contain all the data with no errors" when {
      "provided with full input" in {
        //val input = buildAddressInputMap(correctAddress)
        val expectedAddress = correctAddress

        val form = Address.form().bind(correctAddressJSON)

        form.errors must be(empty)
        form.value must be(defined)
        form.value.get must equal(expectedAddress)
      }

      "provided with full input, containing spaces in fields" in {
        val input = buildAddressInputMap(correctAddress)
        val expectedAddress = correctAddress

        val form = Address.form().bind(input)

        form.errors must be(empty)
        form.value must be(defined)
        form.value.get must equal(expectedAddress)
      }
    }
  }

}

object AddressSpec {

  import play.api.libs.json._

  val correctAddress =
    Address(fullName = "Full Name", addressLine = "Address Line", townOrCity = "Town or City", postCode = "AB12 34CD", country = "Poland")

  val incorrectAddress = Address(
    fullName = "nX9KuS2J6Ee1ATbgbcZFaFOCHI1t8RJnNfbU0dbPQAK4w0q6PdzuZIyxcXziSbUBzizlmi1",
    addressLine = "nX9KuS2J6Ee1ATbgbcZFaFOCHI1t8RJnNfbU0dbPQAK4w0q6PdzuZIyxcXziSbUBzizlmi1",
    townOrCity = "gDsSwbyP6cPkpgRZSHoH3JtEj8grDfjsVCmq",
    postCode = "TN0EHF96qN",
    country = "abc"
  )

  val addressWithEmptyFullname =
    Address(fullName = "", addressLine = "Address Line", townOrCity = "Town or City", postCode = "AB12 34CD", country = "Poland")

  val emptyAddress = Address("", "", "", "", "")

  val correctAddressJSON: JsValue = Json.toJson(correctAddress)
  val incorrectAddressJSON: JsValue = Json.toJson(incorrectAddress)
  val emptyAddressJSON: JsValue = Json.toJson(emptyAddress)
  val addressWithEmptyFullnameJSON: JsValue = Json.toJson(addressWithEmptyFullname)

  def buildAddressInputMap(address: Address): Map[String, String] = buildAddressInputMap(
    fullName = address.fullName,
    addressLine = address.addressLine,
    townOrCity = address.townOrCity,
    postCode = address.postCode,
    country = address.country
  )

  def buildAddressInputMap(
    fullName: String = "",
    addressLine: String = "",
    townOrCity: String = "",
    postCode: String = "",
    country: String = ""
  ): Map[String, String] =
    Map("fullName" -> fullName, "addressLine" -> addressLine, "townOrCity" -> townOrCity, "postCode" -> postCode, "country" -> country)
}
