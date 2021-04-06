/*
 * Copyright 2021 HM Revenue & Customs
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
import org.scalatest.{Assertion, MustMatchers, WordSpec}

class AddressSpec extends WordSpec with MustMatchers {
  import AddressSpec._

  "Bound Form with Address mapping" should {

    "contain errors for fullName" when {
      "provided with empty input" in {
        verifyError(buildAddressInputMap(), "fullName", "empty")
      }

      "provided with input longer than 35 characters" in {
        verifyError(buildAddressInputMap(fullName = createRandomAlphanumericString(36)), "fullName", "length")
      }

      "contains non-allowed characters" in {
        verifyError(buildAddressInputMap(fullName = "abc%"), "fullName", "error")
      }
    }

    "contain errors for addressLine" when {
      "provided with empty input" in {
        verifyError(buildAddressInputMap(), "addressLine", "empty")
      }

      "provided with input longer than 35 characters" in {
        verifyError(buildAddressInputMap(addressLine = createRandomAlphanumericString(36)), "addressLine", "length")
      }

      "contains non-allowed characters" in {
        verifyError(buildAddressInputMap(addressLine = "abc%"), "addressLine", "error")
      }
    }

    "contain errors for townOrCity" when {
      "provided with empty input" in {
        verifyError(buildAddressInputMap(), "townOrCity", "empty")
      }

      "provided with input longer than 35 characters" in {
        verifyError(buildAddressInputMap(townOrCity = createRandomAlphanumericString(36)), "townOrCity", "length")
      }

      "contains non-allowed characters" in {
        verifyError(buildAddressInputMap(townOrCity = "abc%"), "townOrCity", "error")
      }
    }

    "contain errors for postCode" when {
      "provided with empty input" in {
        verifyError(buildAddressInputMap(), "postCode", "empty")
      }

      "provided with input longer than 9 characters" in {
        verifyError(buildAddressInputMap(postCode = createRandomAlphanumericString(10)), "postCode", "length")
      }

      "contains non-allowed characters" in {
        verifyError(buildAddressInputMap(postCode = "abc%"), "postCode", "error")
      }
    }

    "contain errors for country" when {
      "provided with empty input" in {
        verifyError(buildAddressInputMap(), "country", "empty")
      }

      "provided with input containing special characters" in {
        verifyError(buildAddressInputMap(country = "abc%"), "country", "error")
      }

      "provided with non-existing country name" in {
        val input = buildAddressInputMap(country = "Non Existing Country")
        val form = Address.form().bind(input)

        val countryError = form.errors.find(_.key == "country")
        countryError must be(defined)
        countryError.get.message must equal("declaration.address.country.error")
      }
    }

    "contain all the data with no errors" when {
      "provided with full input" in {
        val expectedAddress = correctAddress

        val form = Address.form().bind(correctAddressJSON)

        form.errors must be(empty)
        form.value must be(defined)
        form.value.get must equal(expectedAddress)
      }

      "provided with full input, containing spaces in fields" in {
        val input: Map[String, String] = buildAddressInputMap(correctAddress)
        val expectedAddress = correctAddress

        val form = Address.form().bind(input)

        form.errors must be(empty)
        form.value must be(defined)
        form.value.get must equal(expectedAddress)
      }
    }
  }

  private def verifyError(input: Map[String, String], field: String, errorKey: String): Assertion = {
    val form = Address.form().bind(input)
    val fullNameError = form.errors.find(_.key == field)
    fullNameError must be(defined)
    fullNameError.get.message must equal(s"declaration.address.$field.$errorKey")
  }
}

object AddressSpec {

  import play.api.libs.json._

  val thirtySixChars = "0123456789012345678901234567890123456"
  val tenChars = "01234567890"

  val correctAddress =
    Address(
      fullName = "Full Name abcABC123-'",
      addressLine = "Address Line abcABC123-'",
      townOrCity = "Town-'",
      postCode = "AB12 34C",
      country = "Poland"
    )

  val wrongLengthAddress =
    Address(fullName = thirtySixChars, addressLine = thirtySixChars, townOrCity = thirtySixChars, postCode = tenChars, country = "abc")

  val incorrectAddress =
    Address(fullName = "abc%", addressLine = "abc*", townOrCity = "abc+", postCode = "BN^", country = "abc")

  val emptyAddress = Address("", "", "", "", "")

  val correctAddressJSON: JsValue = Json.toJson(correctAddress)
  val wrongLengthAddressJSON: JsValue = Json.toJson(wrongLengthAddress)
  val incorrectAddressJSON: JsValue = Json.toJson(incorrectAddress)
  val emptyAddressJSON: JsValue = Json.toJson(emptyAddress)

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
