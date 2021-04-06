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

package views.declaration.spec

import base.TestHelper
import forms.common.Address
import models.requests.JourneyRequest
import org.jsoup.nodes.Document
import org.scalatest.Assertion
import org.scalatest.Inspectors.forAll

trait AddressViewSpec extends UnitViewSpec {

  val illegalField = "abcd#@"
  val fieldWithIllegalLength = TestHelper.createRandomAlphanumericString(36)

  val validAddress = Address("Some Name", "Test Street", "Leeds", "LS18BN", "England")
  val invalidAddress = Address("abc*", "abc*", "abc#*", "#+@", "Barcelona")
  val emptyAddress = Address("", "", "", "", "")
  val addressWithIllegalLengths = Address(fieldWithIllegalLength, fieldWithIllegalLength, fieldWithIllegalLength, fieldWithIllegalLength, "England")

  def assertIncorrectView(document: Document, field: String, errorKey: String)(implicit request: JourneyRequest[_]): Assertion = {
    document must haveGovukGlobalErrorSummary
    assertIncorrectElement(document, field: String, errorKey: String)
  }

  def assertIncorrectElements(document: Document, fields: List[String], errorKey: String)(implicit request: JourneyRequest[_]): Assertion = {
    document must haveGovukGlobalErrorSummary
    forAll(fields)(assertIncorrectElement(document, _, errorKey))
  }

  def assertIncorrectElement(document: Document, field: String, errorKey: String)(implicit request: JourneyRequest[_]): Assertion = {
    document must containErrorElementWithTagAndHref("a", s"#details_address_$field")
    val id = if (field == "country") "error-message-details.address.country-input" else s"details_address_$field-error"
    document.getElementById(id) must containMessage(s"declaration.address.$field.$errorKey")
  }
}
