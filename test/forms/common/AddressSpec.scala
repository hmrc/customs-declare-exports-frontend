/*
 * Copyright 2024 HM Revenue & Customs
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

import base.{TestHelper, UnitSpec}
import connectors.CodeListConnector
import models.codes.Country
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, when}
import org.scalatest.{Assertion, BeforeAndAfterEach}
import org.scalatestplus.mockito.MockitoSugar
import play.api.i18n.{Lang, Messages}
import play.api.test.Helpers._

import java.util.Locale
import scala.collection.immutable.ListMap

class AddressSpec extends UnitSpec with MockitoSugar with BeforeAndAfterEach {
  import AddressSpec._

  implicit val mockCodeListConnector: CodeListConnector = mock[CodeListConnector]
  implicit val messages: Messages = stubMessagesApi().preferred(Seq(Lang(Locale.ENGLISH)))

  override def beforeEach(): Unit = {
    super.beforeEach()

    when(mockCodeListConnector.getCountryCodes(any())).thenReturn(ListMap("GB" -> Country(validAddress.country, "GB")))
  }

  override protected def afterEach(): Unit = {
    reset(mockCodeListConnector)
    super.afterEach()
  }

  "Bound Form with Address mapping" should {

    "contain errors for fullName" when {
      "provided with empty input" in {
        verifyError(buildAddressInputMap(), "fullName", "empty")
      }

      "provided with input longer than 35 characters" in {
        verifyError(buildAddressInputMap(fullName = fieldWithLengthOver35), "fullName", "length")
      }

      "contains non-allowed characters" in {
        verifyError(buildAddressInputMap(fullName = illegalField), "fullName", "error")
      }
    }

    "contain errors for addressLine" when {
      "provided with empty input" in {
        verifyError(buildAddressInputMap(), "addressLine", "empty")
      }

      "provided with input longer than 70 characters" in {
        verifyError(buildAddressInputMap(addressLine = fieldWithLengthOver70), "addressLine", "length")
      }

      "contains non-allowed characters" in {
        verifyError(buildAddressInputMap(addressLine = illegalField), "addressLine", "error")
      }
    }

    "contain errors for townOrCity" when {
      "provided with empty input" in {
        verifyError(buildAddressInputMap(), "townOrCity", "empty")
      }

      "provided with input longer than 35 characters" in {
        verifyError(buildAddressInputMap(townOrCity = fieldWithLengthOver35), "townOrCity", "length")
      }

      "contains non-allowed characters" in {
        verifyError(buildAddressInputMap(townOrCity = illegalField), "townOrCity", "error")
      }
    }

    "contain errors for postCode" when {
      "provided with empty input" in {
        verifyError(buildAddressInputMap(), "postCode", "empty")
      }

      "provided with input longer than 9 characters" in {
        verifyError(buildAddressInputMap(postCode = fieldWithLengthOver35), "postCode", "length")
      }

      "contains non-allowed characters" in {
        verifyError(buildAddressInputMap(postCode = illegalField), "postCode", "error")
      }
    }

    "contain errors for country" when {
      "provided with empty input" in {
        verifyError(buildAddressInputMap(), "country", "empty")
      }

      "provided with input containing special characters" in {
        verifyError(buildAddressInputMap(country = illegalField), "country", "error")
      }

      "provided with non-existing country name" in {
        val input = buildAddressInputMap(country = "Non Existing Country")
        val form = Address.form.bind(input)

        val countryError = form.errors.find(_.key == "country")
        countryError must be(defined)
        countryError.get.message must equal("declaration.address.country.error")
      }
    }

    "contain all the data with no errors" when {

      "provided with full input" in {
        val expectedAddress = validAddress

        val form = Address.form.bind(correctAddressJSON, JsonBindMaxChars)

        form.errors must be(empty)
        form.value must be(defined)
        form.value.get must equal(expectedAddress)
      }

      "provided with full input, containing spaces in fields" in {
        val input: Map[String, String] = buildAddressInputMap(validAddress)
        val expectedAddress = validAddress

        val form = Address.form.bind(input)

        form.errors must be(empty)
        form.value must be(defined)
        form.value.get must equal(expectedAddress)
      }
    }
  }

  private def verifyError(input: Map[String, String], field: String, errorKey: String): Assertion = {
    val form = Address.form.bind(input)
    val fullNameError = form.errors.find(_.key == field)
    fullNameError must be(defined)
    fullNameError.get.message must equal(s"declaration.address.$field.$errorKey")
  }
}

object AddressSpec {

  import play.api.libs.json._

  val illegalField = "abcd#@"
  val fieldWithLengthOver35 = TestHelper.createRandomAlphanumericString(36)
  val fieldWithLengthOver70 = TestHelper.createRandomAlphanumericString(71)
  val fieldWithLengthOver9 = TestHelper.createRandomAlphanumericString(10)

  val validAddress =
    Address(fullName = "Some Name,'-&", addressLine = "(Test Street,'-& . , /)", townOrCity = "Leeds,'-&", postCode = "LS18 BN", country = "GB")

  val invalidAddress =
    Address(fullName = illegalField, addressLine = illegalField, townOrCity = illegalField, postCode = illegalField, country = "Barcelona")

  val addressWithIllegalLengths =
    Address(
      fullName = fieldWithLengthOver35,
      addressLine = fieldWithLengthOver70,
      townOrCity = fieldWithLengthOver35,
      postCode = fieldWithLengthOver9,
      country = "United Kingdom"
    )

  val emptyAddress = Address("", "", "", "", "")
  val partialEmptyAddress = Address("foo", "", "", "", "")

  val correctAddressJSON: JsValue = Json.toJson(validAddress)
  val wrongLengthAddressJSON: JsValue = Json.toJson(addressWithIllegalLengths)
  val incorrectAddressJSON: JsValue = Json.toJson(invalidAddress)
  val emptyAddressJSON: JsValue = Json.toJson(emptyAddress)

  def buildAddressInputMap(address: Address): Map[String, String] =
    buildAddressInputMap(
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
