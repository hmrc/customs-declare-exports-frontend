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

trait DocumentsProducedMessages {

  val documentProduced = "supplementary.addDocument"

  val title: String = documentProduced + ".title"
  val hint: String = documentProduced + ".hint"
  val documentTypeCode: String = documentProduced + ".documentTypeCode"
  val documentTypeCodeError: String = documentProduced + ".documentTypeCode.error"
  val documentIdentifier: String = documentProduced + ".documentIdentifier"
  val documentIdentifierError: String = documentProduced + ".documentIdentifier.error"
  val documentPart: String = documentProduced + ".documentPart"
  val documentPartError: String = documentProduced + ".documentPart.error"
  val documentStatus: String = documentProduced + ".documentStatus"
  val documentStatusError: String = documentProduced + ".documentStatus.error"
  val documentStatusReason: String = documentProduced + ".documentStatusReason"
  val documentStatusReasonError: String = documentProduced + ".documentStatusReason.error"
  val issuingAuthorityName: String = documentProduced + ".issuingAuthorityName"
  val issuingAuthorityNameLengthError: String = documentProduced + ".issuingAuthorityName.error.length"
  val dateOfValidity: String = documentProduced + ".dateOfValidity"
  val measurementUnit: String = documentProduced + ".measurementUnit"
  val measurementUnitLengthError: String = documentProduced + ".measurementUnit.error.length"
  val measurementUnitSpecialCharactersError: String = documentProduced + ".measurementUnit.error.specialCharacters"
  val documentQuantity: String = documentProduced + ".documentQuantity"
  val documentQuantityError: String = documentProduced + ".documentQuantity.error"
  val documentQuantityPrecisionError: String = documentProduced + ".documentQuantity.error.precision"
  val documentQuantityScaleError: String = documentProduced + ".documentQuantity.error.scale"

  val maximumAmountReachedError: String = documentProduced + ".error.maximumAmount"
  val duplicatedItemError: String = documentProduced + ".error.duplicated"
  val notDefinedError: String = documentProduced + ".error.notDefined"
  val documentIdentifierAndPartError: String = documentProduced + ".error.documentIdentifierAndPart"
  val measurementUnitAndQuantityError: String = documentProduced + ".error.measurementUnitAndQuantity"
}
