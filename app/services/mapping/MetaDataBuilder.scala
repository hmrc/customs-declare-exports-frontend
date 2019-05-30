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
import forms.Choice
import javax.xml.bind.JAXBElement
import javax.xml.namespace.QName
import models.declaration.SupplementaryDeclarationData.SchemaMandatoryValues
import services.mapping.declaration.DeclarationBuilder
import uk.gov.hmrc.http.cache.client.CacheMap
import wco.datamodel.wco.dec_dms._2.Declaration
import wco.datamodel.wco.documentmetadata_dms._2.MetaData
import wco.datamodel.wco.metadata_ds_dms._2._

object MetaDataBuilder {

  def build(cacheMap: CacheMap, choice: Choice): MetaData = {
    val metaData = new MetaData

    val agencyAssignedCustomizationCodeType = new MetaDataAgencyAssignedCustomizationCodeType
    agencyAssignedCustomizationCodeType.setValue(SchemaMandatoryValues.agencyAssignedCustomizationVersionCode)

    val metaDataResponsibleAgencyNameTextType = new MetaDataResponsibleAgencyNameTextType
    metaDataResponsibleAgencyNameTextType.setValue(SchemaMandatoryValues.responsibleAgencyName)

    val metaDataResponsibleCountryCodeType = new MetaDataResponsibleCountryCodeType
    metaDataResponsibleCountryCodeType.setValue(SchemaMandatoryValues.responsibleCountryCode)

    val metaDataWCODataModelVersionCodeType = new MetaDataWCODataModelVersionCodeType
    metaDataWCODataModelVersionCodeType.setValue(SchemaMandatoryValues.wcoDataModelVersionCode)

    val metaDataWCOTypeNameTextType = new MetaDataWCOTypeNameTextType
    metaDataWCOTypeNameTextType.setValue(SchemaMandatoryValues.wcoTypeName)

    metaData.setAgencyAssignedCustomizationCode(agencyAssignedCustomizationCodeType)
    metaData.setResponsibleAgencyName(metaDataResponsibleAgencyNameTextType)
    metaData.setResponsibleCountryCode(metaDataResponsibleCountryCodeType)
    metaData.setWCODataModelVersionCode(metaDataWCODataModelVersionCodeType)
    metaData.setWCOTypeName(metaDataWCOTypeNameTextType)

    val element: JAXBElement[Declaration] = new JAXBElement[Declaration](
      new QName("urn:wco:datamodel:WCO:DEC-DMS:2", "Declaration"),
      classOf[Declaration],
      DeclarationBuilder.build(cacheMap, choice)
    )
    metaData.setAny(element)

    metaData
  }
}
