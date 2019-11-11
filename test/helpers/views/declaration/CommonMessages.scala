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

  val limit: String = "supplementary.limit"
  val duplication: String = "supplementary.duplication"
  val continueError: String = "supplementary.continue.error"
  val continueMandatory: String = "supplementary.continue.mandatory"
  val eori: String = "supplementary.eori"
  val eoriEmpty: String = "supplementary.eori.empty"
  val eoriError: String = "supplementary.eori.error"
  val eoriOrAddressEmpty: String = "supplementary.namedEntityDetails.error"
  val partyType: String = "supplementary.partyType"

  val ducrError: String = "error.ducr"
  val ucrError: String = "error.ucr"
  val errorSummaryTitle: String = "error.summary.title"
  val errorSummaryText: String = "error.summary.text"

  val fullName: String = "supplementary.address.fullName"
  val fullNameEmpty: String = "supplementary.address.fullName.empty"
  val fullNameError: String = "supplementary.address.fullName.error"
  val addressLine: String = "supplementary.address.addressLine"
  val addressLineEmpty: String = "supplementary.address.addressLine.empty"
  val addressLineError: String = "supplementary.address.addressLine.error"
  val townOrCity: String = "supplementary.address.townOrCity"
  val townOrCityEmpty: String = "supplementary.address.townOrCity.empty"
  val townOrCityError: String = "supplementary.address.townOrCity.error"
  val postCode: String = "supplementary.address.postCode"
  val postCodeEmpty: String = "supplementary.address.postCode.empty"
  val postCodeError: String = "supplementary.address.postCode.error"
  val country: String = "supplementary.address.country"
  val countryEmpty: String = "supplementary.address.country.empty"
  val countryError: String = "supplementary.address.country.error"

  val backCaption: String = "site.back"
  val removeCaption: String = "site.remove"
  val removeHint: String = "site.remove.hint"
  val addCaption: String = "site.add"
  val saveAndContinueCaption: String = "site.save_and_continue"
  val saveAndReturnCaption: String = "site.save_and_come_back_later"

  val consolidator: String = "supplementary.partyType.CS"
  val manufacturer: String = "supplementary.partyType.MF"
  val freightForwarder: String = "supplementary.partyType.FW"
  val warehouseKeeper: String = "supplementary.partyType.WH"
  val partyTypeEmpty: String = "supplementary.partyType.empty"
  val partyTypeError: String = "supplementary.partyType.error"

  val globalErrorTitle: String = "global.error.title"
  val globalErrorHeading: String = "global.error.heading"
  val globalErrorMessage: String = "global.error.message"
}
