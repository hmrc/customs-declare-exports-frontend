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

package helpers.views.declaration

trait CommonMessages {

  val common: String = "supplementary"
  val limit: String = common + ".limit"
  val duplication: String = common + ".duplication"
  val continueError: String = common + ".continue.error"
  val continueMandatory: String = common + ".continue.mandatory"
  val eori: String = common + ".eori"
  val eoriHint: String = common + ".eori.hint"
  val eoriEmpty: String = common + ".eori.empty"
  val eoriError: String = common + ".eori.error"
  val eoriOrAddressEmpty: String = common + ".namedEntityDetails.error"
  val partyType: String = common + ".partyType"

  val errorPrefix: String = "error"
  val ucrError: String = errorPrefix + ".ducr"
  val errorSummaryTitle: String = errorPrefix + ".summary.title"
  val errorSummaryText: String = errorPrefix + ".summary.text"

  val address: String = "supplementary.address"
  val fullName: String = address + ".fullName"
  val fullNameEmpty: String = address + ".fullName.empty"
  val fullNameError: String = address + ".fullName.error"
  val addressLine: String = address + ".addressLine"
  val addressLineEmpty: String = address + ".addressLine.empty"
  val addressLineError: String = address + ".addressLine.error"
  val townOrCity: String = address + ".townOrCity"
  val townOrCityEmpty: String = address + ".townOrCity.empty"
  val townOrCityError: String = address + ".townOrCity.error"
  val postCode: String = address + ".postCode"
  val postCodeEmpty: String = address + ".postCode.empty"
  val postCodeError: String = address + ".postCode.error"
  val country: String = address + ".country"
  val countryEmpty: String = address + ".country.empty"
  val countryError: String = address + ".country.error"

  val site: String = "site"
  val backCaption: String = site + ".back"
  val removeCaption: String = site + ".remove"
  val addCaption: String = site + ".add"
  val saveAndContinueCaption: String = site + ".save_and_continue"

  val party: String = "supplementary.partyType"
  val consolidator: String = party + ".CS"
  val manufacturer: String = party + ".MF"
  val freightForwarder: String = party + ".FW"
  val warehouseKeeper: String = party + ".WH"
  val partyTypeEmpty: String = party + ".empty"
  val partyTypeError: String = party + ".error"

  val globalError: String = "global.error"
  val globalErrorTitle: String = globalError + ".title"
  val globalErrorHeading: String = globalError + ".heading"
  val globalErrorMessage: String = globalError + ".message"
}
