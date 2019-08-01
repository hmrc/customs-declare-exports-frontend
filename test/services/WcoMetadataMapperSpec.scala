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
import org.mockito.ArgumentMatchers.{any, refEq}
import org.mockito.BDDMockito._
import org.mockito.Mockito.verify
import org.scalatest.mockito.MockitoSugar
import org.scalatest.{Matchers, WordSpec}
import services.cache.ExportsCacheModel
import services.cache.mapping.SubmissionMetaDataBuilder
import wco.datamodel.wco.dec_dms._2.Declaration.GoodsShipment
import wco.datamodel.wco.dec_dms._2.{Declaration, ObjectFactory}
import wco.datamodel.wco.declaration_ds.dms._2.{DeclarationFunctionalReferenceIDType, UCRTraderAssignedReferenceIDType}
import wco.datamodel.wco.documentmetadata_dms._2.MetaData

class WcoMetadataMapperSpec extends WordSpec with Matchers with SchemaValidation with MockitoSugar {

  private val submissionMetaDataBuiler = mock[SubmissionMetaDataBuilder]
  private val mapper: WcoMetadataMapper = new WcoMetadataMapper(submissionMetaDataBuiler)

  "WcoMetadataMapper" should {

    "produce metadata" in {
      val metaData = new MetaData()

      given(submissionMetaDataBuiler.build(any[ExportsCacheModel])) willReturn (metaData)

      val result = mapper.produceMetaData(ExportsTestData.exportsCacheModelFull)

      result shouldBe metaData
      verify(submissionMetaDataBuiler).build(refEq(ExportsTestData.exportsCacheModelFull))

    }

    "retrieve a DUCR based on the produced metadata" in {
      mapper.declarationUcr(createMetadata) should be(Some("8GB123456789012-1234567890QWERTYUIO"))
    }

    "retrieve a LRN based on the produced metadata" in {
      mapper.declarationLrn(createMetadata) should be(Some("123LRN"))
    }

  }

  private def createMetadata = {
    val metaData = new MetaData()
    val declaration = new Declaration()
    val goodsShipment = new GoodsShipment()

    val ucr = new GoodsShipment.UCR
    val traderReferenceIdType = new UCRTraderAssignedReferenceIDType
    traderReferenceIdType.setValue("8GB123456789012-1234567890QWERTYUIO")
    ucr.setTraderAssignedReferenceID(traderReferenceIdType)
    goodsShipment.setUCR(ucr)

    val idType = new DeclarationFunctionalReferenceIDType
    idType.setValue("123LRN")

    declaration.setFunctionalReferenceID(idType)
    declaration.setGoodsShipment(goodsShipment)

    val objectFactory = new ObjectFactory
    val jaxbDeclaration = objectFactory.createDeclaration(declaration)
    metaData.setAny(jaxbDeclaration)
    metaData
  }
}
