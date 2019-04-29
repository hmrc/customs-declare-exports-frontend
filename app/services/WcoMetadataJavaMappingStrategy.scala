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

import javax.xml.bind.JAXBElement
import services.mapping.MetaDataBuilder
import uk.gov.hmrc.http.cache.client.CacheMap
import wco.datamodel.wco.dec_dms._2.Declaration
import wco.datamodel.wco.documentmetadata_dms._2.MetaData

trait WcoMetadataJavaMappingStrategy extends WcoMetadataMappingStrategy {

  override def produceMetaData(cacheMap: CacheMap): MetaData =
    MetaDataBuilder.build(cacheMap)

  override def declarationUcr(metaData: Any): Option[String] =
    Option(
      metaData
        .asInstanceOf[MetaData]
        .getAny
        .asInstanceOf[JAXBElement[Declaration]]
        .getValue
        .getGoodsShipment
        .getUCR
        .getTraderAssignedReferenceID
        .getValue
    ).orElse(Some(""))

  override def declarationLrn(metaData: Any): Option[String] =
    Option(
      metaData
        .asInstanceOf[MetaData]
        .getAny
        .asInstanceOf[JAXBElement[Declaration]]
        .getValue
        .getFunctionalReferenceID
        .getValue
    ).orElse(Some(""))

  override def toXml(metaData: Any): String = {
    import java.io.StringWriter

    import javax.xml.bind.{JAXBContext, Marshaller}

    val jaxbMarshaller = JAXBContext.newInstance(classOf[MetaData]).createMarshaller
    jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true)

    val sw = new StringWriter
    jaxbMarshaller.marshal(metaData.asInstanceOf[MetaData], sw)
    sw.toString
  }
}
