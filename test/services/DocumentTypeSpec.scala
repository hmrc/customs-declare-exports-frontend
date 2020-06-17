/*
 * Copyright 2020 HM Revenue & Customs
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

import org.scalatest.{MustMatchers, WordSpec}
import services.DocumentType.allDocuments

class DocumentTypeSpec extends WordSpec with MustMatchers {

  "Document Type" should {

    "return document type in correct order" in {

      val threeTypes =
        allDocuments.filter(d => d.description == "MUCR" || d.description == "Information Sheet INF3" || d.description == "Other")
      val expectedResult =
        List(DocumentType("Information Sheet INF3", "IF3"), DocumentType("MUCR", "MCR"), DocumentType("Other", "ZZZ"))
      threeTypes must be(expectedResult)
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
