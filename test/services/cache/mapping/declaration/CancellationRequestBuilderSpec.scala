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

package services.cache.mapping.declaration

import javax.xml.bind.JAXBElement
import models.declaration.SupplementaryDeclarationData.SchemaMandatoryValues
import org.scalatest.{Matchers, WordSpec}
import services.mapping.declaration.{
  AdditionalInformationBuilder,
  AmendmentBuilder,
  FunctionCodeBuilder,
  FunctionalReferenceIdBuilder,
  IdentificationBuilder,
  SubmitterBuilder,
  TypeCodeBuilder
}
import wco.datamodel.wco.dec_dms._2.Declaration

class CancellationRequestBuilderSpec extends WordSpec with Matchers {

  private val functionalReferenceId = "6677798899"
  private val declarationId = "jhyuyuutuit"
  private val eori = "GB56565656"
  private val changeReason = "03"
  private val statementDescription = "duplicate declaration"

  "CancellationBuilder" should {
    "build wco MetaData with correct defaultValues when empty/ default model is passed in" in {
      val metaData = CancellationRequestBuilder.buildCancellationRequest(buildDeclaration())

      metaData.getWCODataModelVersionCode.getValue shouldBe SchemaMandatoryValues.wcoDataModelVersionCode
      metaData.getResponsibleAgencyName.getValue shouldBe SchemaMandatoryValues.responsibleAgencyName
      metaData.getResponsibleCountryCode.getValue shouldBe SchemaMandatoryValues.responsibleCountryCode
      metaData.getAgencyAssignedCustomizationCode.getValue shouldBe SchemaMandatoryValues.agencyAssignedCustomizationVersionCode
      val builtDeclaration = metaData.getAny.asInstanceOf[JAXBElement[Declaration]].getValue
      builtDeclaration should not be (null)
      validateDeclaration(builtDeclaration)

    }

    "build declaration cancellation Payload when passed a declarations object with values" in {
      val declaration = CancellationRequestBuilder.buildCancellationRequestDeclarationPayload(
        functionalReferenceId,
        declarationId,
        statementDescription,
        changeReason,
        eori
      )
      validateDeclaration(declaration)

    }
  }

  private def validateDeclaration(declaration: Declaration): Unit = {
    declaration.getFunctionCode.getValue should be("13")
    declaration.getTypeCode.getValue should be("INV")
    declaration.getFunctionalReferenceID.getValue should be(functionalReferenceId)
    declaration.getID.getValue should be(declarationId)
    declaration.getSubmitter.getID.getValue should be(eori)
    declaration.getAmendment.get(0).getChangeReasonCode.getValue should be(changeReason)
    declaration.getAdditionalInformation.get(0).getStatementDescription.getValue should be(statementDescription)
  }

  private def buildDeclaration(): Declaration = {
    val declaration = new Declaration()
    declaration.setFunctionCode(FunctionCodeBuilder.build("13"))
    declaration.setTypeCode(TypeCodeBuilder.build("INV"))
    declaration.setFunctionalReferenceID(FunctionalReferenceIdBuilder.build(functionalReferenceId))
    declaration.setID(IdentificationBuilder.build(declarationId))
    declaration.setSubmitter(SubmitterBuilder.build(eori))
    declaration.getAmendment.add(AmendmentBuilder.build(changeReason))
    declaration.getAdditionalInformation.add(AdditionalInformationBuilder.build(statementDescription))
    declaration
  }
}
