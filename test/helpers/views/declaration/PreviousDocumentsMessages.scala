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

trait PreviousDocumentsMessages {

  val previousDocuments: String = "supplementary.previousDocuments"

  val title: String = previousDocuments + ".title"
  val hint: String = previousDocuments + ".hint"
  val documentCategoryEmpty: String = previousDocuments + ".documentCategory.error.empty"
  val documentCategoryError: String = previousDocuments + ".documentCategory.error.incorrect"
  val documentX: String = previousDocuments + ".X"
  val documentY: String = previousDocuments + ".Y"
  val documentZ: String = previousDocuments + ".Z"
  val documentType: String = previousDocuments + ".documentType"
  val documentTypeError: String = previousDocuments + ".documentType.error"
  val documentTypeEmpty: String = previousDocuments + ".documentType.empty"
  val documentReference: String = previousDocuments + ".documentReference"
  val documentReferenceError: String = previousDocuments + ".documentReference.error"
  val documentReferenceEmpty: String = previousDocuments + ".documentReference.empty"
  val documentGoodsIdentifier: String = previousDocuments + ".goodsItemIdentifier"
  val documentGoodsIdentifierError: String = previousDocuments + ".goodsItemIdentifier.error"
  val documentCategoryLabel: String = previousDocuments + ".documentCategory.label"
  val documentTypeLabel: String = previousDocuments + ".documentType.label"
  val documentReferenceLabel: String = previousDocuments + ".documentReference.label"
  val documentGoodsIdentifierLabel: String = previousDocuments + ".goodsItemIdentifier.label"

  // TODO: message from other screen
  val removePackageInformation: String = "supplementary.packageInformation.remove"
}
