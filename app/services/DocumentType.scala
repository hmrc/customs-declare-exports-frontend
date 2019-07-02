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

package services

import utils.JsonFile

case class DocumentType(description: String, code: String)

object DocumentType {

  private val deserialiser: (String, String) => DocumentType = (a: String, b: String) => DocumentType(a, b)

  val allDocuments: List[DocumentType] = JsonFile
    .readFromJsonFile("/code-lists/document-type-autocomplete-list.json", deserialiser)
    .sortBy(_.description.toLowerCase)
}
