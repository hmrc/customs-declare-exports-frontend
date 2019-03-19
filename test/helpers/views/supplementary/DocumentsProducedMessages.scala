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

package helpers.views.supplementary

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
  val documentQuantity: String = documentProduced + ".documentQuantity"
  val documentQuantityError: String = documentProduced + ".documentQuantity.error"
  val maximumAmountReached: String = documentProduced + ".maximumAmount.error"
  val duplicatedItem: String = documentProduced + ".duplicated"
  val notDefined: String = documentProduced + ".isNotDefined"
}
