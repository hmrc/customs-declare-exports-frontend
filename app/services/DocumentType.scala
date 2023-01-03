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

package services

import utils.JsonFile

case class DocumentType(description: String, code: String) {

  def asText: String = description + " - " + code

  def asTextWithBrackets: String = description + " (" + code + ")"
}

object DocumentType {

  val documentsExcludedFromDropdown = Seq("MCR", "CLE")
  private val deserialiser: (String, String) => DocumentType = (a: String, b: String) => DocumentType(a, b)

  // CEDS-3132: the resulting tariff list should be rendered in the same order as shown at:
  // https://assets.publishing.service.gov.uk/government/uploads/system/uploads/attachment_data/file/915216/Previous_document_codes_for_Data_Element_2-1_of_the_Customs_Declaration_Service_-_v2.csv/preview
  val allDocuments: List[DocumentType] = JsonFile
    .readFromJsonFile("/code-lists/document-type-autocomplete-list.json", deserialiser)

  val documentsForDropdown: List[DocumentType] = allDocuments.filterNot(docType => documentsExcludedFromDropdown.contains(docType.code))

  val documentCodesMap: Map[String, DocumentType] =
    allDocuments.map(documentType => (documentType.code, documentType)).toMap

  def findByCode(code: String): DocumentType = documentCodesMap(code)

  def findByCodes(codes: Seq[String]): Seq[DocumentType] = codes.map(documentCodesMap(_))
}
