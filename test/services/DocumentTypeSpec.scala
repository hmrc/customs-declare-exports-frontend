/*
 * Copyright 2022 HM Revenue & Customs
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
import services.DocumentType.allDocuments

class DocumentTypeSpec extends UnitSpec {

  "Document Type" should {

    "return document type in the expected order (no-order, as loaded)" in {
      val ixs = List(0, 1, 2, 3, 4, 25, 26, 33, 34, allDocuments.size - 1)
      val codes = List("235", "270", "271", "325", "337", "955", "CLE", "SDE", "CSE", "CPD")
      forAll(ixs zip codes)(t => allDocuments(t._1).code mustBe t._2)
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
