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

trait ItemTypeMessages {

  val itemType: String = "supplementary.itemType"

  val title: String = itemType + ".title"
  val cncHeader: String = itemType + ".combinedNomenclatureCode.header"
  val cncHeaderHint: String = itemType + ".combinedNomenclatureCode.header.hint"
  val cncErrorEmpty: String = itemType + ".combinedNomenclatureCode.error.empty"
  val cncErrorLength: String = itemType + ".combinedNomenclatureCode.error.length"
  val cncErrorSpecialCharacters: String = itemType + ".combinedNomenclatureCode.error.specialCharacters"

  val taricHeader: String = itemType + ".taricAdditionalCodes.header"
  val taricHeaderHint: String = itemType + ".taricAdditionalCodes.header.hint"
  val taricErrorLength: String = itemType + ".taricAdditionalCodes.error.length"
  val taricErrorSpecialCharacters: String = itemType + ".taricAdditionalCodes.error.specialCharacters"
  val taricErrorMaxAmount: String = itemType + ".taricAdditionalCodes.error.maxAmount"
  val taricErrorDuplicate: String = itemType + ".taricAdditionalCodes.error.duplicate"

  val nacHeader: String = itemType + ".nationalAdditionalCode.header"
  val nacHeaderHint: String = itemType + ".nationalAdditionalCode.header.hint"
  val nacErrorLength: String = itemType + ".nationalAdditionalCode.error.length"
  val nacErrorSpecialCharacters: String = itemType + ".nationalAdditionalCode.error.specialCharacters"
  val nacErrorMaxAmount: String = itemType + ".nationalAdditionalCode.error.maxAmount"
  val nacErrorDuplicate: String = itemType + ".nationalAdditionalCode.error.duplicate"

  val descriptionHeader: String = itemType + ".description.header"
  val descriptionHeaderHint: String = itemType + ".description.header.hint"
  val descriptionErrorEmpty: String = itemType + ".description.error.empty"
  val descriptionErrorLength: String = itemType + ".description.error.length"

  val cusCodeHeader: String = itemType + ".cusCode.header"
  val cusCodeHeaderHint: String = itemType + ".cusCode.header.hint"
  val cusCodeErrorLength: String = itemType + ".cusCode.error.length"
  val cusCodeErrorSpecialCharacters: String = itemType + ".cusCode.error.specialCharacters"

  val statisticalHeader: String = itemType + ".statisticalValue.header"
  val statisticalHeaderHint: String = itemType + ".statisticalValue.header.hint"
  val statisticalErrorEmpty: String = itemType + ".statisticalValue.error.empty"
  val statisticalErrorLength: String = itemType + ".statisticalValue.error.length"
  val statisticalErrorWrongFormat: String = itemType + ".statisticalValue.error.wrongFormat"
}
