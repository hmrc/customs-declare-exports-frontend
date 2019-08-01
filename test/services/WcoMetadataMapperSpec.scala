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

import base.ExportsTestData
import models.declaration.SupplementaryDeclarationData.SchemaMandatoryValues
import org.scalatest.{Matchers, WordSpec}
import org.scalatestplus.play.guice.GuiceOneAppPerSuite

import scala.io.Source

class WcoMetadataMapperSpec extends WordSpec with Matchers with SchemaValidation with GuiceOneAppPerSuite {

  private val mapper: WcoMetadataMapper = app.injector.asInstanceOf[WcoMetadataMapper]

  "WcoMetadataMapper" should {

    "produce metadata" in {

      val metaData = mapper.produceMetaData(ExportsTestData.exportsCacheModelFull)

      metaData.getWCOTypeName.getValue shouldBe SchemaMandatoryValues.wcoTypeName
      metaData.getWCODataModelVersionCode.getValue shouldBe SchemaMandatoryValues.wcoDataModelVersionCode
      metaData.getResponsibleAgencyName.getValue shouldBe SchemaMandatoryValues.responsibleAgencyName
      metaData.getResponsibleCountryCode.getValue shouldBe SchemaMandatoryValues.responsibleCountryCode
      metaData.getAgencyAssignedCustomizationCode.getValue shouldBe SchemaMandatoryValues.agencyAssignedCustomizationVersionCode
      metaData.getAny should not be (null)
    }

    "retrieve a DUCR based on the produced metadata" in {
      val metaData = mapper.produceMetaData(ExportsTestData.exportsCacheModelFull)

      mapper.declarationUcr(metaData) should be(Some("8GB123456789012-1234567890QWERTYUIO"))
    }

    "retrieve a LRN based on the produced metadata" in {
      val metaData = mapper.produceMetaData(ExportsTestData.exportsCacheModelFull)

      mapper.declarationLrn(metaData) should be(Some("123LRN"))
    }

    "marshall the metadata correctly" in {
      val metaData = mapper.produceMetaData(ExportsTestData.exportsCacheModelFull)

      mapper.toXml(metaData) should include(Source.fromURL(getClass.getResource("/wco_dec_metadata.xml")).mkString)

      validateXmlAgainstSchema(mapper.toXml(metaData))
    }
  }
}
