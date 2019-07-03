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

package services.model

import services.{DocumentType, HolderOfAuthorisationCode, NationalAdditionalCode}

case class AutoCompleteItem(label: String, value: String)

object AutoCompleteItem {

  def fromCountry(countries: List[Country], value: Country => String = _.countryName): List[AutoCompleteItem] = {
    countries map (c => AutoCompleteItem(s"${c.countryName} - ${c.countryCode}", value(c)))
  }

  def fromPackageType(packageTypes: List[PackageType]): List[AutoCompleteItem] = {
    packageTypes map (c => AutoCompleteItem(s"${c.description} - ${c.code}", c.code))
  }

  def fromDocumentType(documents: List[DocumentType]): List[AutoCompleteItem] = {
    documents map (d => AutoCompleteItem(s"${d.description} - ${d.code}", d.code))
  }

  def fromNationalAdditionalCode(codes: List[NationalAdditionalCode]): List[AutoCompleteItem] = {
    codes map (c => AutoCompleteItem(c.value, c.value))
  }

  def fromHolderOfAuthorisationCode(codes: List[HolderOfAuthorisationCode]): List[AutoCompleteItem] = {
    codes map (c => AutoCompleteItem(c.value, c.value))
  }

  def fromOfficeOfExit(offices: List[OfficeOfExit]): List[AutoCompleteItem] = {
    offices map (d => AutoCompleteItem(s"${d.description} - ${d.code}", d.code))
  }
}
