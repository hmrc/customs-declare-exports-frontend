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

import base.UnitSpec
import org.scalatest.Inspectors.forAll
import services.DocumentType.{allDocuments, documentsExcludedFromDropdown, documentsForDropdown}

class DocumentTypeSpec extends UnitSpec {

  "Document Type" should {

    "return document type in the expected order with no exclusions" in {
      val ixs = List(0, 1, 2, 3, 4, 25, 26, 33, 34, allDocuments.size - 1)
      val codes = List("380", "325", "271", "703", "740", "952", "955", "T2M", "SDE", "CPD")
      forAll(ixs zip codes)(t => allDocuments(t._1).code mustBe t._2)
    }

    "return document type in the expected order with dropdown exclusions" in {
      val ixs = List(0, 1, 2, 3, 4, 25, 26, 33, 34, documentsForDropdown.size - 1)
      val codes = List("380", "325", "271", "703", "740", "952", "955", "SDE", "CSE", "CPD")
      forAll(ixs zip codes)(t => documentsForDropdown(t._1).code mustBe t._2)
      forAll(documentsForDropdown)(t => documentsExcludedFromDropdown.contains(t.code))
    }

    "return correct text for asText method" in {

      val documentType = DocumentType("description", "code")
      val expectedType = "description - code"

      documentType.asText mustBe expectedType
    }

    "return correct text for asTextWithBrackets method" in {

      val documentType = DocumentType("description", "code")
      val expectedType = "description (code)"

      documentType.asTextWithBrackets mustBe expectedType
    }
  }
}
