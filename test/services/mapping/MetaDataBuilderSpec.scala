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

package services.mapping
import forms.ChoiceSpec.supplementaryChoice
import javax.xml.bind.JAXBElement
import models.declaration.SupplementaryDeclarationData.SchemaMandatoryValues
import models.declaration.SupplementaryDeclarationTestData
import org.scalatest.{Matchers, WordSpec}
import wco.datamodel.wco.dec_dms._2.Declaration

class MetaDataBuilderSpec extends WordSpec with Matchers {

  "MetaDataBuilder" should {
    "build wco MetaData with correct defaultValues" in {
      val metaData = MetaDataBuilder.build(SupplementaryDeclarationTestData.cacheMapAllRecords, supplementaryChoice)
      metaData.getWCOTypeName.getValue shouldBe SchemaMandatoryValues.wcoTypeName
      metaData.getWCODataModelVersionCode.getValue shouldBe SchemaMandatoryValues.wcoDataModelVersionCode
      metaData.getResponsibleAgencyName.getValue shouldBe SchemaMandatoryValues.responsibleAgencyName
      metaData.getResponsibleCountryCode.getValue shouldBe SchemaMandatoryValues.responsibleCountryCode
      metaData.getAgencyAssignedCustomizationCode.getValue shouldBe SchemaMandatoryValues.agencyAssignedCustomizationVersionCode
      metaData.getAny.asInstanceOf[JAXBElement[Declaration]].getValue should not be (null)
    }
  }
}
