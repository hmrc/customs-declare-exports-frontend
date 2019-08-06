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

import javax.xml.bind.JAXBElement
import javax.xml.namespace.QName
import wco.datamodel.wco.dec_dms._2.Declaration
import wco.datamodel.wco.documentmetadata_dms._2.MetaData

object MetaDataBuilder {

  def buildRequest(declaration: Declaration): MetaData = {
    val metaData = new MetaData

    val element: JAXBElement[Declaration] = new JAXBElement[Declaration](
      new QName("urn:wco:datamodel:WCO:DEC-DMS:2", "Declaration"),
      classOf[Declaration],
      declaration
    )
    metaData.setAny(element)

    metaData
  }
}
