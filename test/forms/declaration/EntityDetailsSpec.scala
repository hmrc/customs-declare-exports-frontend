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

package forms.declaration

import forms.common.{Address, AddressSpec}
import org.scalatest.{MustMatchers, WordSpec}
import play.api.libs.json.{JsObject, JsString, JsValue}

class EntityDetailsSpec extends WordSpec with MustMatchers {
  import EntityDetailsSpec._

  "Bound form with NamedEntityDetails mapping" should {

    "contain errors for entity details only" when {
      "both EORI and Address are empty" in {
        val input = buildEntityInputMap()

        val form = EntityDetails.form().bind(input)

        form.errors mustNot be(empty)
        val wholeFormErrorName = ""
        val wholeFormError = form.errors.find(_.key == wholeFormErrorName)
        wholeFormError must be(defined)
        wholeFormError.get.message must equal("supplementary.namedEntityDetails.error")
      }
    }

    "contain errors for EORI only" when {
      "Address is empty & EORI is longer than 17 characters" in {
        val eori = "123456789012345678"
        val input = buildEntityInputMap(eori = eori)

        val form = EntityDetails.form().bind(input)

        form.errors mustNot be(empty)
        val eoriError = form.errors.find(_.key == "eori")
        eoriError must be(defined)
        eoriError.get.message must equal("supplementary.eori.error")
      }

      "Address is empty & EORI contains special characters" in {
        val eori = "12!@#$"
        val input = buildEntityInputMap(eori = eori)

        val form = EntityDetails.form().bind(input)

        form.errors mustNot be(empty)
        val eoriError = form.errors.find(_.key == "eori")
        eoriError must be(defined)
        eoriError.get.message must equal("supplementary.eori.error")
      }

      "Address is correct but EORI is wrong" in {
        val eori = "12345ABCD!@#$"
        val address =
          Address(fullName = "Full Name", addressLine = "Address Line", townOrCity = "City", postCode = "AB12 CD3", country = "United Kingdom")
        val input = buildEntityInputMap(eori, address)

        val form = EntityDetails.form().bind(input)

        form.errors mustNot be(empty)
        val eoriError = form.errors.find(_.key == "eori")
        eoriError must be(defined)
        eoriError.get.message must equal("supplementary.eori.error")
      }
    }

    "contain errors for Address only" when {
      "EORI is empty & Address has error in a single field" in {
        val address =
          Address(fullName = "!@#$%^&*", addressLine = "Address Line", townOrCity = "City", postCode = "AB12 CD3", country = "United Kingdom")
        val input = buildEntityInputMap(address = address)

        val form = EntityDetails.form().bind(input)

        form.errors mustNot be(empty)
        val addressError = form.errors.find(_.key.contains("address."))
        addressError must be(defined)
        addressError.get.messages.size must equal(1)
        addressError.get.message must equal("supplementary.address.fullName.error")
      }

      "EORI is empty & Address has errors in all fields" in {
        val address = Address(
          fullName = "!@#$%^&*",
          addressLine = "",
          townOrCity = "City!@$",
          postCode = "AB12 CD34 1235 1346",
          country = "Any Country you can imagine"
        )
        val input = buildEntityInputMap(address = address)

        val form = EntityDetails.form().bind(input)

        form.errors mustNot be(empty)
        val addressErrors = form.errors.filter(_.key.contains("address."))
        addressErrors.size must equal(5)
        addressErrors.map(_.message) must contain("supplementary.address.fullName.error")
        addressErrors.map(_.message) must contain("supplementary.address.addressLine.empty")
        addressErrors.map(_.message) must contain("supplementary.address.townOrCity.error")
        addressErrors.map(_.message) must contain("supplementary.address.postCode.error")
        addressErrors.map(_.message) must contain("supplementary.address.country.error")
      }

      "EORI is correct but Address has errors" in {
        val eori = "9GB1234567ABCDEF"
        val address = Address(
          fullName = "!@#$%^&*",
          addressLine = "",
          townOrCity = "City!@$",
          postCode = "AB12 CD34 1235 1346",
          country = "Any Country you can imagine"
        )
        val input = buildEntityInputMap(eori, address)

        val form = EntityDetails.form().bind(input)

        form.errors mustNot be(empty)
        val addressErrors = form.errors.filter(_.key.contains("address."))
        addressErrors.size must equal(5)
        addressErrors.map(_.message) must contain("supplementary.address.fullName.error")
        addressErrors.map(_.message) must contain("supplementary.address.addressLine.empty")
        addressErrors.map(_.message) must contain("supplementary.address.townOrCity.error")
        addressErrors.map(_.message) must contain("supplementary.address.postCode.error")
        addressErrors.map(_.message) must contain("supplementary.address.country.error")
      }
    }

    "contain errors for both EORI & Address" when {
      "both EORI & Address elements are wrong" in {
        val eori = "9GB!@#$%^&*"
        val address = Address(
          fullName = "!@#$%^&*",
          addressLine = "",
          townOrCity = "City!@$",
          postCode = "AB12 CD34 1235 1346",
          country = "Any Country you can imagine"
        )
        val input = buildEntityInputMap(eori, address)

        val form = EntityDetails.form().bind(input)

        form.errors mustNot be(empty)
        val eoriError = form.errors.find(_.key == "eori")
        eoriError must be(defined)
        eoriError.get.messages.size must equal(1)
        eoriError.get.message must equal("supplementary.eori.error")

        val addressErrors = form.errors.filter(_.key.contains("address."))
        addressErrors.size must equal(5)
        addressErrors.map(_.message) must contain("supplementary.address.fullName.error")
        addressErrors.map(_.message) must contain("supplementary.address.addressLine.empty")
        addressErrors.map(_.message) must contain("supplementary.address.townOrCity.error")
        addressErrors.map(_.message) must contain("supplementary.address.postCode.error")
        addressErrors.map(_.message) must contain("supplementary.address.country.error")
      }
    }

    "contain all the data with no errors" when {
      "EORI is correct & Address is empty" in {
        val input = buildEntityInputMap(correctEntityDetails)

        val form = EntityDetails.form().bind(input)

        form.errors must be(empty)
        form.value must be(defined)
        form.value.get.eori must be(defined)
        form.value.get.eori.get must equal(correctEntityDetails.eori.get)
      }

      "EORI is empty & Address is correct" in {
        val input = buildEntityInputMap(correctEntityDetails)

        val form = EntityDetails.form().bind(input)

        form.errors must be(empty)
        form.value must be(defined)
        form.value.get.address must be(defined)
        form.value.get.address.get must equal(correctEntityDetails.address.get)
      }

      "Both EORI & Address are correct" in {
        val input = buildEntityInputMap(correctEntityDetails)

        val form = EntityDetails.form().bind(input)

        form.errors must be(empty)
        form.value must be(defined)
        form.value.get.eori must be(defined)
        form.value.get.eori.get must equal(correctEntityDetails.eori.get)
        form.value.get.address must be(defined)
        form.value.get.address.get must equal(correctEntityDetails.address.get)
      }
    }
  }

}

object EntityDetailsSpec {
  val correctEntityDetails = EntityDetails(eori = Some("9GB1234567ABCDEF"), address = Some(AddressSpec.correctAddress))
  val correctEntityDetailsEORIOnly = EntityDetails(eori = Some("9GB1234567ABCDEF"), address = None)
  val correctEntityDetailsAddressOnly = EntityDetails(eori = None, address = Some(AddressSpec.correctAddress))
  val incorrectEntityDetails =
    EntityDetails(eori = Some("gIeC1xyOPmgpZSVGT1nFmGxPd3tS7yvj7CKgsZfq2BYfXPB0tKM6GISKwvuqn0g14TwN6e"), address = Some(AddressSpec.incorrectAddress))
  val emptyEntityDetails = EntityDetails(None, None)

  val correctEntityDetailsJSON: JsValue = JsObject(Map("eori" -> JsString("9GB1234567ABCDEF"), "address" -> AddressSpec.correctAddressJSON))
  val entityDetailsWithEmptyFullNameJSON: JsValue = JsObject(
    Map("eori" -> JsString("9GB1234567ABCDEF"), "address" -> AddressSpec.addressWithEmptyFullnameJSON)
  )
  val correctEntityDetailsEORIOnlyJSON: JsValue = JsObject(Map("eori" -> JsString("9GB1234567ABCDEF"), "address" -> AddressSpec.emptyAddressJSON))
  val correctEntityDetailsAddressOnlyJSON: JsValue = JsObject(Map("eori" -> JsString(""), "address" -> AddressSpec.correctAddressJSON))
  val incorrectEntityDetailsJSON: JsValue = JsObject(
    Map("eori" -> JsString("gIeC1xyOPmgpZSVGT1nFmGxPd3tS7yvj7CKgsZfq2BYfXPB0tKM6GISKwvuqn0g14TwN6e"), "address" -> AddressSpec.incorrectAddressJSON)
  )
  val emptyEntityDetailsJSON: JsValue = JsObject(Map("eori" -> JsString(""), "address" -> AddressSpec.emptyAddressJSON))

  def buildEntityInputMap(entityDetails: EntityDetails): Map[String, String] =
    buildEntityInputMap(eori = entityDetails.eori.getOrElse(""), address = entityDetails.address.getOrElse(buildAddress()))

  def buildEntityInputMap(eori: String = "", address: Address = buildAddress()): Map[String, String] = Map(
    "eori" -> eori,
    "address.fullName" -> address.fullName,
    "address.addressLine" -> address.addressLine,
    "address.townOrCity" -> address.townOrCity,
    "address.postCode" -> address.postCode,
    "address.country" -> address.country
  )

  private def buildAddress(
    fullName: String = "",
    addressLine: String = "",
    townOrCity: String = "",
    postCode: String = "",
    country: String = ""
  ): Address = Address(fullName = fullName, addressLine = addressLine, townOrCity = townOrCity, postCode = postCode, country = country)

}
