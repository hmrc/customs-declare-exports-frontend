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

import connectors.CodeListConnector
import play.api.i18n.Messages

import javax.inject.{Inject, Singleton}

@Singleton
class DocumentTypeService @Inject() (codeListConnector: CodeListConnector) {

  def documentCodesMap()(implicit messages: Messages): Map[String, DocumentType] =
    codeListConnector.getDocumentTypes(messages.lang.toLocale)

  def allDocuments()(implicit messages: Messages): List[DocumentType] =
    documentCodesMap().map(_._2).toList

  def findByCode(code: String)(implicit messages: Messages): DocumentType =
    documentCodesMap()(messages)(code)
}

object DocumentTypeService {
  val exclusionKey = "ExcludeFromDropdown"
}
