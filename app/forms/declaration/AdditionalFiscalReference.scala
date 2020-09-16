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

package forms.declaration

import forms.DeclarationPage
import forms.common.YesNoAnswer
import forms.common.YesNoAnswer.YesNoAnswers
import play.api.data.{Form, Forms}
import play.api.data.Forms.text
import play.api.libs.json.Json
import services.Countries.{allCountries, countryCodeMap}
import utils.validators.forms.FieldValidator._

case class AdditionalFiscalReference(country: String, reference: String) {
  val asString: String = country + reference

  val countryName = countryCodeMap(country).asString()
}

object AdditionalFiscalReference extends DeclarationPage {
  def build(country: String, reference: String): AdditionalFiscalReference = new AdditionalFiscalReference(country, reference.toUpperCase)
  implicit val format = Json.format[AdditionalFiscalReference]

  val mapping = Forms.mapping(
    "country" -> text()
      .verifying("declaration.additionalFiscalReferences.country.empty", _.trim.nonEmpty)
      .verifying("declaration.additionalFiscalReferences.country.error", input => input.isEmpty || allCountries.exists(_.countryCode == input)),
    "reference" -> text()
      .verifying("declaration.additionalFiscalReferences.reference.empty", _.trim.nonEmpty)
      .verifying("declaration.additionalFiscalReferences.reference.error", isAlphanumeric and noLongerThan(15))
  )(AdditionalFiscalReference.build)(AdditionalFiscalReference.unapply)

  def form(): Form[AdditionalFiscalReference] = Form(mapping)
}

case class AdditionalFiscalReferencesData(references: Seq[AdditionalFiscalReference]) {
  def removeReferences(values: Seq[String]): AdditionalFiscalReferencesData = {
    val patterns = values.toSet
    copy(references = references.filterNot(reference => patterns.contains(reference.asString)))
  }

  def removeReference(value: String): AdditionalFiscalReferencesData =
    removeReferences(Seq(value))
}

object AdditionalFiscalReferencesData {
  implicit val format = Json.format[AdditionalFiscalReferencesData]

  def apply(references: Seq[AdditionalFiscalReference]): AdditionalFiscalReferencesData =
    new AdditionalFiscalReferencesData(references)

  def default: AdditionalFiscalReferencesData = AdditionalFiscalReferencesData(Seq.empty)

  val formId = "AdditionalFiscalReferences"

  val limit = 99
}

object AdditionalFiscalReferencesSummary extends DeclarationPage
