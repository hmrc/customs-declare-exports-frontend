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
import javax.xml.bind.JAXBElement
import org.scalatest.{MustMatchers, WordSpec}
import play.api.libs.json.{JsValue, Json}
import wco.datamodel.wco.dec_dms._2.Declaration

class CancelDeclarationSpec extends WordSpec with MustMatchers {

  "CancelDeclaration" should {
    "convert itself to cancellation properties" in {
      val cancellationDeclaration = correctCancelDeclaration
      val metadata = cancellationDeclaration.createCancellationMetadata("N4tur380y")
      val declaration = metadata.getAny.asInstanceOf[JAXBElement[Declaration]].getValue
      declaration.getFunctionCode.getValue must be("13")
      declaration.getFunctionalReferenceID.getValue must be("1SA123456789012-1FSA1234567")
      declaration.getID.getValue must be("87654321")
      declaration.getTypeCode.getValue must be("INV")
      declaration.getSubmitter.getID.getValue must be("N4tur380y")
      declaration.getAdditionalInformation.size must be(1)
      declaration.getAdditionalInformation.get(0).getStatementDescription.getValue must be("Some description")
      declaration.getAdditionalInformation.get(0).getStatementTypeCode.getValue must be("AES")
      declaration.getAdditionalInformation.get(0).getPointer.size must be(2)
      declaration.getAdditionalInformation.get(0).getPointer.get(0).getSequenceNumeric.intValue() must be(1)
      declaration.getAdditionalInformation.get(0).getPointer.get(0).getDocumentSectionCode.getValue must be("42A")
      declaration.getAdditionalInformation.get(0).getPointer.get(1).getDocumentSectionCode.getValue must be("06A")
      declaration.getAmendment.size must be(1)
      declaration.getAmendment.get(0).getChangeReasonCode.getValue must be(NoLongerRequired.toString)
    }
  }
}

object CancelDeclarationSpec {
  val correctCancelDeclaration =
    CancelDeclaration(
      functionalReferenceId = "1SA123456789012-1FSA1234567",
      mrn = "87654321",
      statementDescription = "Some description",
      changeReason = NoLongerRequired.toString
    )

  val emptyRepresentativeDetails =
    CancelDeclaration(
      functionalReferenceId = "",
      mrn = "",
      statementDescription = "",
      changeReason = NoLongerRequired.toString
    )

  val correctCancelDeclarationJSON: JsValue = Json.toJson(correctCancelDeclaration)

  val emptyCancelDeclarationJSON: JsValue = Json.toJson(emptyRepresentativeDetails)
}
