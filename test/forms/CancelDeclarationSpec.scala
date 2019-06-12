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

package forms

import forms.CancelDeclarationSpec.correctCancelDeclaration
import forms.cancellation.CancellationChangeReason.NoLongerRequired
import org.scalatest.{MustMatchers, WordSpec}
import play.api.libs.json.{JsValue, Json}

class CancelDeclarationSpec extends WordSpec with MustMatchers {

  "CancelDeclaration" should {
    "convert itself to cancellation properties" in {
      val cancellationDeclaration = correctCancelDeclaration
      val metadata = cancellationDeclaration.createCancellationMetadata("N4tur380y")
      metadata.declaration mustBe defined
      metadata.declaration.get.functionCode mustBe defined
      metadata.declaration.get.functionCode.get must be(13)
      metadata.declaration.get.functionalReferenceId mustBe defined
      metadata.declaration.get.functionalReferenceId.get must be("1SA123456789012-1FSA1234567")
      metadata.declaration.get.id mustBe defined
      metadata.declaration.get.id.get must be("87654321")
      metadata.declaration.get.typeCode mustBe defined
      metadata.declaration.get.typeCode.get must be("INV")
      metadata.declaration.get.submitter mustBe defined
      metadata.declaration.get.submitter.get.id mustBe defined
      metadata.declaration.get.submitter.get.id.get must be("N4tur380y")
      metadata.declaration.get.additionalInformations.size must be(1)
      metadata.declaration.get.additionalInformations.head.statementDescription mustBe defined
      metadata.declaration.get.additionalInformations.head.statementDescription.get must be("Some description")
      metadata.declaration.get.additionalInformations.head.statementTypeCode mustBe defined
      metadata.declaration.get.additionalInformations.head.statementTypeCode.get must be("AES")
      metadata.declaration.get.additionalInformations.head.pointers.size must be(2)
      metadata.declaration.get.additionalInformations.head.pointers.head.sequenceNumeric mustBe defined
      metadata.declaration.get.additionalInformations.head.pointers.head.sequenceNumeric.get must be(1)
      metadata.declaration.get.additionalInformations.head.pointers.head.documentSectionCode mustBe defined
      metadata.declaration.get.additionalInformations.head.pointers.head.documentSectionCode.get must be("42A")
      metadata.declaration.get.additionalInformations.head.pointers(1).documentSectionCode mustBe defined
      metadata.declaration.get.additionalInformations.head.pointers(1).documentSectionCode.get must be("06A")
      metadata.declaration.get.amendments.size must be(1)
      metadata.declaration.get.amendments.head.changeReasonCode mustBe defined
      metadata.declaration.get.amendments.head.changeReasonCode.get must be(NoLongerRequired.toString)
    }
  }

}

object CancelDeclarationSpec {
  val correctCancelDeclaration =
    CancelDeclaration(
      functionalReferenceId = "1SA123456789012-1FSA1234567",
      declarationId = "87654321",
      statementDescription = "Some description",
      changeReason = NoLongerRequired.toString
    )

  val emptyRepresentativeDetails =
    CancelDeclaration(
      functionalReferenceId = "",
      declarationId = "",
      statementDescription = "",
      changeReason = NoLongerRequired.toString
    )

  val correctCancelDeclarationJSON: JsValue = Json.toJson(correctCancelDeclaration)

  val emptyCancelDeclarationJSON: JsValue = Json.toJson(emptyRepresentativeDetails)
}
