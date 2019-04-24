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
import org.scalatest.mockito.MockitoSugar
import org.scalatest.{Matchers, WordSpec}
import wco.datamodel.wco.dec_dms._2.Declaration
import wco.datamodel.wco.documentmetadata_dms._2.MetaData

class XmlMarshallingSpec extends WordSpec with Matchers with MockitoSugar {

  val expectedPayload = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n" +
    "<MetaData xmlns=\"urn:wco:datamodel:WCO:DocumentMetaData-DMS:2\">\n" +
    "    <WCODataModelVersionCode>3.6</WCODataModelVersionCode>\n" +
    "    <WCOTypeName>DEC</WCOTypeName>\n" +
    "    <ResponsibleCountryCode>GB</ResponsibleCountryCode>\n" +
    "    <ResponsibleAgencyName>HMRC</ResponsibleAgencyName>\n" +
    "    <AgencyAssignedCustomizationCode>v2.1</AgencyAssignedCustomizationCode>\n" +
    "</MetaData>\n"

  "XmlMarshalling" should {
    "generate correct xml payload for empty Metadata with defaults when marshalled" in {
      val metaData = MetaDataBuilder.build()

      import java.io.StringWriter

      import javax.xml.bind.{JAXBContext, JAXBException, Marshaller}
      try { //Create JAXB Context
        val jaxbContext = JAXBContext.newInstance(classOf[MetaData])
        //Create Marshaller
        val jaxbMarshaller = jaxbContext.createMarshaller
        //Required formatting??
        jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true)
        //Print XML String to Console
        val sw = new StringWriter
        //Write XML to StringWriter
        jaxbMarshaller.marshal(metaData, sw)
        //Verify XML Content
        val xmlContent = sw.toString
        xmlContent should be(expectedPayload)
      } catch {
        case e: JAXBException =>
          e.printStackTrace()
      }
    }
  }
}
