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

package services.view

import models.codes.{AdditionalProcedureCode, Country, ProcedureCode}
import services.DocumentType
import services.model.{CustomsOffice, OfficeOfExit, PackageType}

case class AutoCompleteItem(label: String, value: String)

object AutoCompleteItem {

  def formatProcedureCode(pc: ProcedureCode) = s"${pc.code} - ${pc.description}"

  def fromCountry(countries: List[Country], value: Country => String = _.countryName): List[AutoCompleteItem] =
    countries map (c => AutoCompleteItem(c.asString(), value(c)))

  def fromPackageType(packageTypes: List[PackageType]): List[AutoCompleteItem] =
    packageTypes map (c => AutoCompleteItem(c.asText(), c.code))

  def fromDocumentType(documents: List[DocumentType]): List[AutoCompleteItem] =
    documents map (d => AutoCompleteItem(s"${d.description} - ${d.code}", d.code))

  def fromOfficeOfExit(offices: List[OfficeOfExit]): List[AutoCompleteItem] =
    offices map (d => AutoCompleteItem(s"${d.description} - ${d.code}", d.code))

  def fromSupervisingCustomsOffice(offices: List[CustomsOffice]): List[AutoCompleteItem] =
    offices map (d => AutoCompleteItem(s"${d.description} - ${d.code}", d.code))

  def fromProcedureCodes(procedureCodes: List[ProcedureCode]): List[AutoCompleteItem] =
    procedureCodes map (pc => AutoCompleteItem(formatProcedureCode(pc), pc.code))

  def fromAdditionalProcedureCodes(additionalProcedureCodes: List[AdditionalProcedureCode]): List[AutoCompleteItem] =
    additionalProcedureCodes map (apc => AutoCompleteItem(s"${apc.code} - ${apc.description}", apc.code))
}
