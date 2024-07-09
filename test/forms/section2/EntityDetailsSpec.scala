/*
 * Copyright 2023 HM Revenue & Customs
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

package forms.section2

import forms.common.{AddressSpec, Eori}
import org.scalatest.Assertion
import org.scalatest.matchers.must.Matchers.convertToAnyMustWrapper
import play.api.data.FormError
import play.api.libs.json.{JsObject, JsString, JsValue}

object EntityDetailsSpec {
  val correctEntityDetails = EntityDetails(eori = Some(Eori("GB12345678912345")), address = Some(AddressSpec.validAddress))
  val correctEntityDetailsEORIOnly = EntityDetails(eori = Some(Eori("GB12345678912345")), address = None)

  val correctEntityDetailsEORIOnlyJSON: JsValue = JsObject(Map("eori" -> JsString("GB12345678912345"), "address" -> AddressSpec.emptyAddressJSON))
  val correctEntityDetailsAddressOnlyJSON: JsValue = JsObject(Map("eori" -> JsString(""), "address" -> AddressSpec.correctAddressJSON))
  val incorrectEntityDetailsJSON: JsValue = JsObject(
    Map("eori" -> JsString("gIeC1xyOPmgpZSVGT1nFmGxPd3tS7yvj7CKgsZfq2BYfXPB0tKM6GISKwvuqn0g14TwN6e"), "address" -> AddressSpec.wrongLengthAddressJSON)
  )
  val emptyEntityDetailsJSON: JsValue = JsObject(Map("eori" -> JsString(""), "address" -> AddressSpec.emptyAddressJSON))

  private val fields = List("fullName", "addressLine", "townOrCity", "postCode", "country")

  def assertEmptyDetails(errors: Seq[FormError]): Assertion = {
    errors.size mustBe fields.size
    errors.zip(fields).forall(p => p._1.key == s"details.address.${p._2}") mustBe true
  }
}
