/*
 * Copyright 2023 HM Revenue & Customs
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

package models.dis

import scala.xml.Elem

object MrnStatusTestData {

  def mrnStatusWithAllData(mrn: String): Elem = <p:DeclarationStatusResponse
  xsi:schemaLocation="http://gov.uk/customs/declarationInformationRetrieval/status/v2"
  xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
  xmlns:p4="urn:un:unece:uncefact:data:standard:UnqualifiedDataType:6"
  xmlns:p3="urn:wco:datamodel:WCO:Declaration_DS:DMS:2"
  xmlns:p2="urn:wco:datamodel:WCO:DEC-DMS:2"
  xmlns:p1="urn:wco:datamodel:WCO:Response_DS:DMS:2"
  xmlns:p="http://gov.uk/customs/declarationInformationRetrieval/status/v2">
    <p:DeclarationStatusDetails>
      <p:Declaration>
        <p:AcceptanceDateTime>
          <p1:DateTimeString formatCode="304">20190702110757Z</p1:DateTimeString>
        </p:AcceptanceDateTime>
        <p:ID>{mrn}</p:ID>
        <p:VersionID>1</p:VersionID>
        <p:ReceivedDateTime>
          <p:DateTimeString formatCode="304">20190702110857Z</p:DateTimeString>
        </p:ReceivedDateTime>
        <p:GoodsReleasedDateTime>
          <p:DateTimeString formatCode="304">20190702110957Z</p:DateTimeString>
        </p:GoodsReleasedDateTime>
        <p:ROE>6</p:ROE>
        <p:ICS>15</p:ICS>
        <p:IRC>000</p:IRC>
      </p:Declaration>
      <p2:Declaration>
        <p2:FunctionCode>9</p2:FunctionCode>
        <p2:TypeCode>IMZ</p2:TypeCode>
        <p2:GoodsItemQuantity unitCode="NPR">100</p2:GoodsItemQuantity>
        <p2:TotalPackageQuantity>10</p2:TotalPackageQuantity>
        <p2:Submitter>
          <p2:ID>GB123456789012000</p2:ID>
        </p2:Submitter>
        <p2:GoodsShipment>
          <p2:PreviousDocument>
            <p2:ID>18GBAKZ81EQJ2FGVR</p2:ID>
            <p2:TypeCode>DCR</p2:TypeCode>
          </p2:PreviousDocument>
          <p2:PreviousDocument>
            <p2:ID>18GBAKZ81EQJ2FGVA</p2:ID>
            <p2:TypeCode>MCR</p2:TypeCode>
          </p2:PreviousDocument>
          <p2:PreviousDocument>
            <p2:ID>18GBAKZ81EQJ2FGVB</p2:ID>
            <p2:TypeCode>MCR</p2:TypeCode>
          </p2:PreviousDocument>
          <p2:PreviousDocument>
            <p2:ID>18GBAKZ81EQJ2FGVC</p2:ID>
            <p2:TypeCode>DCR</p2:TypeCode>
          </p2:PreviousDocument>
          <p2:PreviousDocument>
            <p2:ID>18GBAKZ81EQJ2FGVD</p2:ID>
            <p2:TypeCode>MCR</p2:TypeCode>
          </p2:PreviousDocument>
          <p2:PreviousDocument>
            <p2:ID>18GBAKZ81EQJ2FGVE</p2:ID>
            <p2:TypeCode>MCR</p2:TypeCode>
          </p2:PreviousDocument>
          <p2:UCR>
            <p2:TraderAssignedReferenceID>20GBAKZ81EQJ2WXYZ</p2:TraderAssignedReferenceID>
          </p2:UCR>
        </p2:GoodsShipment>
      </p2:Declaration>
    </p:DeclarationStatusDetails>
  </p:DeclarationStatusResponse>

  def mrnStatusWithSelectedFields(mrn: String): Elem = <p:DeclarationStatusResponse
  xsi:schemaLocation="http://gov.uk/customs/declarationInformationRetrieval/status/v2"
  xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
  xmlns:p4="urn:un:unece:uncefact:data:standard:UnqualifiedDataType:6"
  xmlns:p3="urn:wco:datamodel:WCO:Declaration_DS:DMS:2"
  xmlns:p2="urn:wco:datamodel:WCO:DEC-DMS:2"
  xmlns:p1="urn:wco:datamodel:WCO:Response_DS:DMS:2" xmlns:p="http://gov.uk/customs/declarationInformationRetrieval/status/v2">
    <p:DeclarationStatusDetails>
      <p:Declaration>
        <p:ID>{mrn}</p:ID>
        <p:VersionID>1</p:VersionID>
        <p:ReceivedDateTime>
          <p:DateTimeString formatCode="304">20200227114305Z</p:DateTimeString>
        </p:ReceivedDateTime>
        <p:ROE>H</p:ROE>
        <p:ICS>14</p:ICS>
      </p:Declaration>
      <p2:Declaration>
        <p2:FunctionCode>9</p2:FunctionCode>
        <p2:TypeCode>EXD</p2:TypeCode>
        <p2:GoodsItemQuantity>1</p2:GoodsItemQuantity>
        <p2:TotalPackageQuantity>1.0</p2:TotalPackageQuantity>
        <p2:Submitter>
          <p2:ID>GB7172755049242</p2:ID>
        </p2:Submitter>
        <p2:GoodsShipment>
          <p2:PreviousDocument>
            <p2:ID>8GB123456765080-101SHIP1</p2:ID>
            <p2:TypeCode>DCR</p2:TypeCode>
          </p2:PreviousDocument>
        </p2:GoodsShipment>
      </p2:Declaration>
    </p:DeclarationStatusDetails>
  </p:DeclarationStatusResponse>

  def mrnStatusWithNoPreviousDocuments(mrn: String): Elem = <p:DeclarationStatusResponse
  xsi:schemaLocation="http://gov.uk/customs/declarationInformationRetrieval/status/v2"
  xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
  xmlns:p4="urn:un:unece:uncefact:data:standard:UnqualifiedDataType:6"
  xmlns:p3="urn:wco:datamodel:WCO:Declaration_DS:DMS:2"
  xmlns:p2="urn:wco:datamodel:WCO:DEC-DMS:2"
  xmlns:p1="urn:wco:datamodel:WCO:Response_DS:DMS:2" xmlns:p="http://gov.uk/customs/declarationInformationRetrieval/status/v2">
    <p:DeclarationStatusDetails>
      <p:Declaration>
        <p:ID>{mrn}</p:ID>
        <p:VersionID>1</p:VersionID>
        <p:ReceivedDateTime>
          <p:DateTimeString formatCode="304">20200227114305Z</p:DateTimeString>
        </p:ReceivedDateTime>
        <p:ROE>H</p:ROE>
        <p:ICS>14</p:ICS>
      </p:Declaration>
      <p2:Declaration>
        <p2:FunctionCode>9</p2:FunctionCode>
        <p2:TypeCode>EXD</p2:TypeCode>
        <p2:GoodsItemQuantity>1</p2:GoodsItemQuantity>
        <p2:TotalPackageQuantity>1.0</p2:TotalPackageQuantity>
        <p2:Submitter>
          <p2:ID>GB7172755049242</p2:ID>
        </p2:Submitter>
      </p2:Declaration>
    </p:DeclarationStatusDetails>
  </p:DeclarationStatusResponse>
}
