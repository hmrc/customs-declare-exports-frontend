/*
 * Copyright 2018 HM Revenue & Customs
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

package models.wco

import java.io.StringWriter

import com.fasterxml.jackson.annotation.{JsonIgnoreProperties, JsonInclude}
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.{DeserializationContext, DeserializationFeature, JsonNode}
import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import com.fasterxml.jackson.databind.node.{ObjectNode, TextNode}
import com.fasterxml.jackson.dataformat.xml.annotation.{JacksonXmlProperty, JacksonXmlRootElement, JacksonXmlText}
import com.fasterxml.jackson.dataformat.xml.{JacksonXmlModule, XmlMapper}
import com.fasterxml.jackson.module.scala.DefaultScalaModule

/*
MetaData and Declaration schema generally consists of xsd:sequence definitions the order of which is reflected in the
field order of the case classes.

DO NOT CHANGE the field order on any of the case classes unless the XSD requires it.
 */

private[wco] object NS {
  final val dms = "urn:wco:datamodel:WCO:DocumentMetaData-DMS:2"
  final val dec = "urn:wco:datamodel:WCO:DEC-DMS:2"
  final val ds = "urn:wco:datamodel:WCO:Declaration_DS:DMS:2"
}

trait JacksonMapper {

  private val _module = new JacksonXmlModule()
  _module.setDefaultUseWrapper(false)
  protected val _mapper = new XmlMapper(_module)
  _mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
  _mapper.setSerializationInclusion(JsonInclude.Include.NON_EMPTY)
  _mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL)
  _mapper.setSerializationInclusion(JsonInclude.Include.NON_ABSENT)
  _mapper.registerModule(DefaultScalaModule)

}

@JsonIgnoreProperties(Array("_mapper"))
@JacksonXmlRootElement(namespace = NS.dms, localName = "MetaData")
case class MetaData(@JacksonXmlProperty(localName = "WCODataModelVersionCode", namespace = NS.dms)
                    wcoDataModelVersionCode: Option[String] = None, // max 6 chars

                    @JacksonXmlProperty(localName = "WCOTypeName", namespace = NS.dms)
                    wcoTypeName: Option[String] = None, // no constraint

                    @JacksonXmlProperty(localName = "ResponsibleCountryCode", namespace = NS.dms)
                    responsibleCountryCode: Option[String] = None, // max 2 chars - ISO 3166-1 alpha2 code

                    @JacksonXmlProperty(localName = "ResponsibleAgencyName", namespace = NS.dms)
                    responsibleAgencyName: Option[String] = None, // max 70 chars

                    @JacksonXmlProperty(localName = "AgencyAssignedCustomizationCode", namespace = NS.dms)
                    agencyAssignedCustomizationCode: Option[String] = None, // max 6 chars

                    @JacksonXmlProperty(localName = "AgencyAssignedCustomizationVersionCode", namespace = NS.dms)
                    agencyAssignedCustomizationVersionCode: Option[String] = None, // max 3 chars

                    @JacksonXmlProperty(localName = "Declaration", namespace = NS.dec)
                    declaration: Declaration = Declaration()) extends JacksonMapper {

  def toXml: String = {
    val sw = new StringWriter()
    _mapper.writeValue(sw, this)
    sw.toString
  }

}

object MetaData extends JacksonMapper {

  def fromXml(xml: String): MetaData = _mapper.readValue(xml, classOf[MetaData])

}

case class Declaration(@JacksonXmlProperty(localName = "AcceptanceDateTime", namespace = NS.dec)
                       acceptanceDateTime: Option[DateTimeElement] = None,

                       @JacksonXmlProperty(localName = "FunctionCode", namespace = NS.dec)
                       functionCode: Option[Int] = None, // unsigned int in enumeration of [9, 13, 14]

                       @JacksonXmlProperty(localName = "FunctionalReferenceID", namespace = NS.dec)
                       functionalReferenceId: Option[String] = None, // max 35 chars

                       @JacksonXmlProperty(localName = "ID", namespace = NS.dec)
                       id: Option[String] = None, // max 70 chars

                       @JacksonXmlProperty(localName = "IssueDateTime", namespace = NS.dec)
                       issueDateTime: Option[DateTimeElement] = None,

                       @JacksonXmlProperty(localName = "IssueLocationID", namespace = NS.dec)
                       issueLocationId: Option[String] = None, // max 5 chars

                       @JacksonXmlProperty(localName = "TypeCode", namespace = NS.dec)
                       typeCode: Option[String] = None, // max 3 chars; MUST be "INV" in cancellation use case

                       @JacksonXmlProperty(localName = "GoodsItemQuantity", namespace = NS.dec)
                       goodsItemQuantity: Option[Int] = None, // unsigned int max 99999

                       @JacksonXmlProperty(localName = "DeclarationOfficeID", namespace = NS.dec)
                       declarationOfficeId: Option[String] = None, // max 17 chars

                       @JacksonXmlProperty(localName = "InvoiceAmount", namespace = NS.dec)
                       invoiceAmount: Option[Amount] = None,

                       @JacksonXmlProperty(localName = "LoadingListQuantity", namespace = NS.dec)
                       loadingListQuantity: Option[Int] = None, // unsigned int max 99999

                       @JacksonXmlProperty(localName = "TotalGrossMassMeasure", namespace = NS.dec)
                       totalGrossMassMeasure: Option[Measure] = None,

                       @JacksonXmlProperty(localName = "TotalPackageQuantity", namespace = NS.dec)
                       totalPackageQuantity: Option[Int] = None, // unsigned int max 99999999

                       @JacksonXmlProperty(localName = "SpecificCircumstancesCodeCode", namespace = NS.dec)
                       specificCircumstancesCode: Option[String] = None, // max 3 chars

                       @JacksonXmlProperty(localName = "Authentication", namespace = NS.dec)
                       authentication: Option[Authentication] = None,

                       @JacksonXmlProperty(localName = "Submitter", namespace = NS.dec)
                       submitter: Option[NamedEntityWithAddress] = None,

                       @JacksonXmlProperty(localName = "AdditionalDocument", namespace = NS.dec)
                       additionalDocuments: Seq[AdditionalDocument] = Seq.empty,

                       @JacksonXmlProperty(localName = "AdditionalInformation", namespace = NS.dec)
                       additionalInformations: Seq[AdditionalInformation] = Seq.empty,

                       @JacksonXmlProperty(localName = "Agent", namespace = NS.dec)
                       agent: Option[Agent] = None,

                       @JacksonXmlProperty(localName = "Amendment", namespace = NS.dec)
                       amendments: Seq[Amendment] = Seq.empty,

                       @JacksonXmlProperty(localName = "AuthorisationHolder", namespace = NS.dec)
                       authorisationHolders: Seq[AuthorisationHolder] = Seq.empty,

                       @JacksonXmlProperty(localName = "BorderTransportMeans", namespace = NS.dec)
                       borderTransportMeans: Option[BorderTransportMeans] = None,

                       @JacksonXmlProperty(localName = "CurrencyExchange", namespace = NS.dec)
                       currencyExchanges: Seq[CurrencyExchange] = Seq.empty,

                       @JacksonXmlProperty(localName = "Declarant", namespace = NS.dec)
                       declarant: Option[ImportExportParty] = None,

                       @JacksonXmlProperty(localName = "ExitOffice", namespace = NS.dec)
                       exitOffice: Option[Office] = None,

                       @JacksonXmlProperty(localName = "Exporter", namespace = NS.dec)
                       exporter: Option[ImportExportParty] = None,

                       @JacksonXmlProperty(localName = "GoodsShipment", namespace = NS.dec)
                       goodsShipment: Option[GoodsShipment] = None,

                       @JacksonXmlProperty(localName = "ObligationGuarantee", namespace = NS.dec)
                       obligationGuarantees: Seq[ObligationGuarantee] = Seq.empty,

                       @JacksonXmlProperty(localName = "PresentationOffice", namespace = NS.dec)
                       presentationOffice: Option[Office] = None,

                       @JacksonXmlProperty(localName = "SupervisingOffice", namespace = NS.dec)
                       supervisingOffice: Option[Office] = None)

case class Amendment(@JacksonXmlProperty(localName = "ChangeReasonCode", namespace = NS.dec)
                     changeReasonCode: Option[String] = None, // max 3 chars

                     @JacksonXmlProperty(localName = "Pointer", namespace = NS.dec)
                     pointers: Seq[Pointer] = Seq.empty)

case class CurrencyExchange(@JacksonXmlProperty(localName = "CurrencyTypeCode", namespace = NS.dec)
                            currencyTypeCode: Option[String] = None, // max 3 chars [a-zA-Z] ISO 4217 3-alpha code

                            @JacksonXmlProperty(localName = "RateNumeric", namespace = NS.dec)
                            rateNumeric: Option[BigDecimal] = None) // decimal with scale of 12 and precision of 5

case class Agent(@JacksonXmlProperty(localName = "Name", namespace = NS.dec)
                 name: Option[String] = None, // max 70 chars

                 @JacksonXmlProperty(localName = "ID", namespace = NS.dec)
                 id: Option[String] = None, // max 17 chars

                 @JacksonXmlProperty(localName = "FunctionCode", namespace = NS.dec)
                 functionCode: Option[String] = None, // max 3 chars

                 @JacksonXmlProperty(localName = "Address", namespace = NS.dec)
                 address: Option[Address] = None)

case class Authentication(@JacksonXmlProperty(localName = "Authentication", namespace = NS.dec)
                          authentication: Option[String] = None, // max 256 chars

                          @JacksonXmlProperty(localName = "Authenticator", namespace = NS.dec)
                          authenticator: Option[Authenticator] = None)

case class Authenticator(@JacksonXmlProperty(localName = "Name", namespace = NS.dec)
                         name: Option[String] = None) // max 70 chars

case class AdditionalDocument(@JacksonXmlProperty(localName = "ID", namespace = NS.dec)
                              id: Option[String] = None, // max 70 chars

                              @JacksonXmlProperty(localName = "CategoryCode", namespace = NS.dec)
                              categoryCode: Option[String] = None, // max 3 chars

                              @JacksonXmlProperty(localName = "TypeCode", namespace = NS.dec)
                              typeCode: Option[String] = None) // max 3 chars

case class AuthorisationHolder(@JacksonXmlProperty(localName = "ID", namespace = NS.dec)
                               id: Option[String] = None, // max 17 chars

                               @JacksonXmlProperty(localName = "CategoryCode", namespace = NS.dec)
                               categoryCode: Option[String] = None) // max 4 chars

case class BorderTransportMeans(@JacksonXmlProperty(localName = "Name", namespace = NS.dec)
                                name: Option[String] = None, // max 35 chars,

                                @JacksonXmlProperty(localName = "ID", namespace = NS.dec)
                                id: Option[String] = None, // max 35 chars,

                                @JacksonXmlProperty(localName = "IdentificationTypeCode", namespace = NS.dec)
                                identificationTypeCode: Option[String] = None, // max 17 chars

                                @JacksonXmlProperty(localName = "TypeCode", namespace = NS.dec)
                                typeCode: Option[String] = None, // max 4 chars

                                @JacksonXmlProperty(localName = "RegistrationNationalityCode", namespace = NS.dec)
                                registrationNationalityCode: Option[String] = None, // 2 chars [a-zA-Z] when present; presumably ISO 3166-1 alpha2

                                @JacksonXmlProperty(localName = "ModeCode", namespace = NS.dec)
                                modeCode: Option[Int] = None) // 0-9

case class GoodsShipment(@JacksonXmlProperty(localName = "ExitDateTime", namespace = NS.dec)
                         exitDateTime: Option[DateTimeElement] = None,

                         @JacksonXmlProperty(localName = "TransactionNatureCode", namespace = NS.dec)
                         transactionNatureCode: Option[Int] = None, // unsigned int max 99

                         @JacksonXmlProperty(localName = "AEOMutualRecognitionParty", namespace = NS.dec)
                         aeoMutualRecognitionParties: Seq[RoleBasedParty] = Seq.empty,

                         @JacksonXmlProperty(localName = "Buyer", namespace = NS.dec)
                         buyer: Option[ImportExportParty] = None,

                         @JacksonXmlProperty(localName = "Consignee", namespace = NS.dec)
                         consignee: Option[NamedEntityWithAddress] = None,

                         @JacksonXmlProperty(localName = "Consignment", namespace = NS.dec)
                         consignment: Option[Consignment] = None,

                         @JacksonXmlProperty(localName = "Consignor", namespace = NS.dec)
                         consignor: Option[NamedEntityWithAddress] = None,

                         @JacksonXmlProperty(localName = "CustomsValuation", namespace = NS.dec)
                         customsValuation: Option[CustomsValuation] = None,

                         @JacksonXmlProperty(localName = "Destination", namespace = NS.dec)
                         destination: Option[Destination] = None,

                         @JacksonXmlProperty(localName = "DomesticDutyTaxParty", namespace = NS.dec)
                         domesticDutyTaxParties: Seq[RoleBasedParty] = Seq.empty,

                         @JacksonXmlProperty(localName = "ExportCountry", namespace = NS.dec)
                         exportCountry: Option[ExportCountry] = None,

                         @JacksonXmlProperty(localName = "GovernmentAgencyGoodsItem", namespace = NS.dec)
                         governmentAgencyGoodsItems: Seq[GovernmentAgencyGoodsItem] = Seq.empty,

                         @JacksonXmlProperty(localName = "Importer", namespace = NS.dec)
                         importer: Option[ImportExportParty] = None,

                         @JacksonXmlProperty(localName = "Invoice", namespace = NS.dec)
                         invoice: Option[Invoice] = None,

                         @JacksonXmlProperty(localName = "Payer", namespace = NS.dec)
                         payers: Seq[NamedEntityWithAddress] = Seq.empty,

                         @JacksonXmlProperty(localName = "PreviousDocument", namespace = NS.dec)
                         previousDocuments: Seq[PreviousDocument] = Seq.empty,

                         @JacksonXmlProperty(localName = "Seller", namespace = NS.dec)
                         seller: Option[ImportExportParty] = None,

                         @JacksonXmlProperty(localName = "Surety", namespace = NS.dec)
                         sureties: Seq[NamedEntityWithAddress] = Seq.empty,

                         @JacksonXmlProperty(localName = "TradeTerms", namespace = NS.dec)
                         tradeTerms: Option[TradeTerms] = None,

                         @JacksonXmlProperty(localName = "UCR", namespace = NS.dec)
                         ucr: Option[Ucr] = None,

                         @JacksonXmlProperty(localName = "Warehouse", namespace = NS.dec)
                         warehouse: Option[Warehouse] = None)

case class Invoice(@JacksonXmlProperty(localName = "ID", namespace = NS.dec)
                   id: Option[String] = None,

                   @JacksonXmlProperty(localName = "IssueDateTime", namespace = NS.dec)
                   issueDateTime: Option[DateTimeElement] = None)

case class Warehouse(@JacksonXmlProperty(localName = "ID", namespace = NS.dec)
                     id: Option[String] = None, // max 35 chars

                     @JacksonXmlProperty(localName = "TypeCode", namespace = NS.dec)
                     typeCode: String) // max 3 chars

case class TradeTerms(@JacksonXmlProperty(localName = "ConditionCode", namespace = NS.dec)
                      conditionCode: Option[String] = None, // max 3 chars, [a-zA-Z]

                      @JacksonXmlProperty(localName = "CountryRelationshipCode", namespace = NS.dec)
                      countryRelationshipCode: Option[String] = None, // max 3 chars

                      @JacksonXmlProperty(localName = "Description", namespace = NS.dec)
                      description: Option[String] = None, // max 70 chars

                      @JacksonXmlProperty(localName = "LocationID", namespace = NS.dec)
                      locationId: Option[String] = None, // max 17 chars

                      @JacksonXmlProperty(localName = "LocationName", namespace = NS.dec)
                      locationName: Option[String] = None) // max 256 chars

case class Ucr(@JacksonXmlProperty(localName = "ID", namespace = NS.dec)
               id: Option[String] = None, // max 35 chars

               @JacksonXmlProperty(localName = "TraderAssignedReferenceID", namespace = NS.dec)
               traderAssignedReferenceId: Option[String] = None) // max 35 chars

case class Consignment(@JacksonXmlProperty(localName = "ContainerCode", namespace = NS.dec)
                       containerCode: Option[String] = None, // max 3 chars; must be "0" or "1"; modeled as String rather than Int *only* to match schema

                       @JacksonXmlProperty(localName = "ArrivalTransportMeans", namespace = NS.dec)
                       arrivalTransportMeans: Option[TransportMeans] = None,

                       @JacksonXmlProperty(localName = "DepartureTransportMeans", namespace = NS.dec)
                       departureTransportMeans: Option[TransportMeans] = None,

                       @JacksonXmlProperty(localName = "Freight", namespace = NS.dec)
                       freight: Option[Freight] = None,

                       @JacksonXmlProperty(localName = "GoodsLocation", namespace = NS.dec)
                       goodsLocation: Option[GoodsLocation] = None,

                       @JacksonXmlProperty(localName = "Itinerary", namespace = NS.dec)
                       itineraries: Seq[Itinerary] = Seq.empty,

                       @JacksonXmlProperty(localName = "LoadingLocation", namespace = NS.dec)
                       loadingLocation: Option[LoadingLocation] = None,

                       @JacksonXmlProperty(localName = "TransportEquipment", namespace = NS.dec)
                       transportEquipments: Seq[TransportEquipment] = Seq.empty)

case class LoadingLocation(@JacksonXmlProperty(localName = "Name", namespace = NS.dec)
                           name: Option[String] = None, // max 256 chars

                           @JacksonXmlProperty(localName = "ID", namespace = NS.dec)
                           id: Option[String] = None) // max 17 chars

case class Itinerary(@JacksonXmlProperty(localName = "SequenceNumeric", namespace = NS.dec)
                     sequenceNumeric: Int, // unsigned int max 99999

                     @JacksonXmlProperty(localName = "RoutingCountryCode")
                     routingCountryCode: Option[String] = None) // 2 chars ISO-3166-1 alpha2 code

case class Freight(@JacksonXmlProperty(localName = "PaymentMethodCode", namespace = NS.dec)
                   paymentMethodCode: Option[String] = None) // max 3 chars

case class TransportMeans(@JacksonXmlProperty(localName = "Name", namespace = NS.dec)
                          name: Option[String] = None, // max 35 chars

                          @JacksonXmlProperty(localName = "ID", namespace = NS.dec)
                          id: Option[String] = None, // max 35 chars

                          @JacksonXmlProperty(localName = "IdentificationTypeCode", namespace = NS.dec)
                          identificationTypeCode: Option[String] = None, // max 17 chars

                          @JacksonXmlProperty(localName = "TypeCode", namespace = NS.dec)
                          typeCode: Option[String] = None, // max 4 chars

                          @JacksonXmlProperty(localName = "ModeCode", namespace = NS.dec)
                          modeCode: Option[Int] = None) // unsigned int max 9

case class GoodsLocation(@JacksonXmlProperty(localName = "Name", namespace = NS.dec)
                         name: Option[String] = None, // max 256 chars

                         @JacksonXmlProperty(localName = "ID", namespace = NS.dec)
                         id: String, // max 17 chars

                         @JacksonXmlProperty(localName = "TypeCode", namespace = NS.dec)
                         typeCode: Option[String], // max 3 chars

                         @JacksonXmlProperty(localName = "Address", namespace = NS.dec)
                         address: Option[GoodsLocationAddress] = None)

case class GoodsLocationAddress(@JacksonXmlProperty(localName = "TypeCode", namespace = NS.dec)
                                typeCode: Option[String] = None, // max 3 chars

                                @JacksonXmlProperty(localName = "CityName", namespace = NS.dec)
                                cityName: Option[String] = None, // max 35 chars

                                @JacksonXmlProperty(localName = "CountryCode", namespace = NS.dec)
                                countryCode: Option[String] = None, // max 2 chars - an ISO 3166-1 alpha 2 code

                                @JacksonXmlProperty(localName = "Line", namespace = NS.dec)
                                line: Option[String] = None, // max 70 chars

                                @JacksonXmlProperty(localName = "PostcodeID", namespace = NS.dec)
                                postcodeId: Option[String] = None) // max 9 chars

case class TransportEquipment(@JacksonXmlProperty(localName = "SequenceNumeric", namespace = NS.dec)
                              sequenceNumeric: Int, // unsigned max 99999

                              @JacksonXmlProperty(localName = "ID", namespace = NS.dec)
                              id: Option[String] = None, // max 17 chars

                              @JacksonXmlProperty(localName = "Seal", namespace = NS.dec)
                              seals: Seq[Seal] = Seq.empty) // not valid when TransportEquipment defined as part of Commodity

case class Seal(@JacksonXmlProperty(localName = "SequenceNumeric", namespace = NS.dec)
                sequenceNumeric: Int, // unsigned max 99999

                @JacksonXmlProperty(localName = "ID", namespace = NS.dec)
                id: Option[String] = None) // max 35 chars

case class ExportCountry(@JacksonXmlProperty(localName = "ID", namespace = NS.dec)
                         id: String) // max 2 chars; an ISO-3166-1 alpha2 code

case class GovernmentAgencyGoodsItem(@JacksonXmlProperty(localName = "CustomsValueAmount", namespace = NS.dec)
                                     customsValueAmount: Option[BigDecimal] = None, // scale 16 precision 3

                                     @JacksonXmlProperty(localName = "SequenceNumeric", namespace = NS.dec)
                                     sequenceNumeric: Int, // unsigned max 99999

                                     @JacksonXmlProperty(localName = "StatisticalValueAmount", namespace = NS.dec)
                                     statisticalValueAmount: Option[Amount] = None,

                                     @JacksonXmlProperty(localName = "TransactionNatureCode", namespace = NS.dec)
                                     transactionNatureCode: Option[Int] = None, // unsigned max 99

                                     @JacksonXmlProperty(localName = "AdditionalDocument", namespace = NS.dec)
                                     additionalDocuments: Seq[GovernmentAgencyGoodsItemAdditionalDocument] = Seq.empty,

                                     @JacksonXmlProperty(localName = "AdditionalInformation")
                                     additionalInformations: Seq[AdditionalInformation] = Seq.empty,

                                     @JacksonXmlProperty(localName = "AEOMutualRecognitionParty", namespace = NS.dec)
                                     aeoMutualRecognitionParties: Seq[RoleBasedParty] = Seq.empty,

                                     @JacksonXmlProperty(localName = "Buyer", namespace = NS.dec)
                                     buyer: Option[ImportExportParty] = None,

                                     @JacksonXmlProperty(localName = "Commodity", namespace = NS.dec)
                                     commodity: Option[Commodity] = None,

                                     @JacksonXmlProperty(localName = "Consignee", namespace = NS.dec)
                                     consignee: Option[NamedEntityWithAddress] = None,

                                     @JacksonXmlProperty(localName = "Consignor", namespace = NS.dec)
                                     consignor: Option[NamedEntityWithAddress] = None,

                                     @JacksonXmlProperty(localName = "CustomsValuation", namespace = NS.dec)
                                     customsValuation: Option[CustomsValuation] = None,

                                     @JacksonXmlProperty(localName = "Destination", namespace = NS.dec)
                                     destination: Option[Destination] = None,

                                     @JacksonXmlProperty(localName = "DomesticDutyTaxParty", namespace = NS.dec)
                                     domesticDutyTaxParties: Seq[RoleBasedParty] = Seq.empty,

                                     @JacksonXmlProperty(localName = "ExportCountry", namespace = NS.dec)
                                     exportCountry: Option[ExportCountry] = None,

                                     @JacksonXmlProperty(localName = "GovernmentProcedure", namespace = NS.dec)
                                     governmentProcedures: Seq[GovernmentProcedure] = Seq.empty,

                                     @JacksonXmlProperty(localName = "Manufacturer", namespace = NS.dec)
                                     manufacturers: Seq[NamedEntityWithAddress] = Seq.empty,

                                     @JacksonXmlProperty(localName = "Origin", namespace = NS.dec)
                                     origins: Seq[Origin] = Seq.empty,

                                     @JacksonXmlProperty(localName = "Packaging", namespace = NS.dec)
                                     packagings: Seq[Packaging] = Seq.empty,

                                     @JacksonXmlProperty(localName = "PreviousDocument", namespace = NS.dec)
                                     previousDocuments: Seq[PreviousDocument] = Seq.empty,

                                     @JacksonXmlProperty(localName = "RefundRecipientParty", namespace = NS.dec)
                                     refundRecipientParties: Seq[NamedEntityWithAddress] = Seq.empty,

                                     @JacksonXmlProperty(localName = "Seller", namespace = NS.dec)
                                     seller: Option[ImportExportParty] = None,

                                     @JacksonXmlProperty(localName = "UCR", namespace = NS.dec)
                                     ucr: Option[Ucr] = None,

                                     @JacksonXmlProperty(localName = "ValuationAdjustment", namespace = NS.dec)
                                     valuationAdjustment: Option[ValuationAdjustment] = None)

case class GovernmentAgencyGoodsItemAdditionalDocument(@JacksonXmlProperty(localName = "CategoryCode", namespace = NS.dec)
                                                       categoryCode: Option[String] = None, // max 3 chars

                                                       @JacksonXmlProperty(localName = "EffectiveDateTime", namespace = NS.dec)
                                                       effectiveDateTime: Option[DateTimeElement] = None,

                                                       @JacksonXmlProperty(localName = "ID", namespace = NS.dec)
                                                       id: Option[String] = None, // max 70 chars

                                                       @JacksonXmlProperty(localName = "Name", namespace = NS.dec)
                                                       name: Option[String] = None, // max 35 chars

                                                       @JacksonXmlProperty(localName = "TypeCode", namespace = NS.dec)
                                                       typeCode: Option[String] = None, // max 3 chars

                                                       @JacksonXmlProperty(localName = "LPCOExemptionCode", namespace = NS.dec)
                                                       lpcoExemptionCode: Option[String] = None, // max 3 chars

                                                       @JacksonXmlProperty(localName = "Submitter", namespace = NS.dec)
                                                       submitter: Option[GovernmentAgencyGoodsItemAdditionalDocumentSubmitter] = None,

                                                       @JacksonXmlProperty(localName = "WriteOff", namespace = NS.dec)
                                                       writeOff: Option[WriteOff] = None)

case class WriteOff(@JacksonXmlProperty(localName = "QuantityQuantity", namespace = NS.dec)
                    quantity: Option[BigDecimal] = None, // scale 16 precision 6

                    @JacksonXmlProperty(localName = "AmountAmount", namespace = NS.dec)
                    amount: Option[Amount] = None)

case class GovernmentAgencyGoodsItemAdditionalDocumentSubmitter(@JacksonXmlProperty(localName = "Name", namespace = NS.dec)
                                                                name: Option[String] = None, // max 70 chars

                                                                @JacksonXmlProperty(localName = "RoleCode", namespace = NS.dec)
                                                                roleCode: Option[String] = None) // max 3 chars

case class ValuationAdjustment(@JacksonXmlProperty(localName = "AdditionCode", namespace = NS.dec)
                               additionCode: Option[String] = None) // max 4 chars; subset of EDIFACT codes: one of 145, 146, 147, 148, 149

case class PreviousDocument(@JacksonXmlProperty(localName = "CategoryCode", namespace = NS.dec)
                            categoryCode: Option[String] = None, // max 3 chars

                            @JacksonXmlProperty(localName = "ID", namespace = NS.dec)
                            id: Option[String] = None, // max 70 chars

                            @JacksonXmlProperty(localName = "TypeCode", namespace = NS.dec)
                            typeCode: Option[String] = None, // max 3 chars

                            @JacksonXmlProperty(localName = "LineNumeric", namespace = NS.dec)
                            lineNumeric: Option[Int] = None) // unsigned int max 99999

case class Packaging(@JacksonXmlProperty(localName = "SequenceNumeric", namespace = NS.dec)
                     sequenceNumeric: Option[Int] = None, // unsigned max 99999

                     @JacksonXmlProperty(localName = "MarksNumberID", namespace = NS.dec)
                     marksNumbersId: Option[String] = None, // max 512 chars

                     @JacksonXmlProperty(localName = "QuantityQuantity", namespace = NS.dec)
                     quantity: Option[Int] = None, // max 99999999

                     @JacksonXmlProperty(localName = "TypeCode", namespace = NS.dec)
                     typeCode: Option[String] = None, // max 2 chars

                     @JacksonXmlProperty(localName = "PackingMaterialDescription", namespace = NS.dec)
                     packingMaterialDescription: Option[String] = None, // max 256 chars

                     // TODO can probably reuse Measure type here as length and width take unitCode attribute? Although it would need to support constraint to int/long types
                     @JacksonXmlProperty(localName = "LengthMeasure", namespace = NS.dec)
                     lengthMeasure: Option[Long] = None, // unsigned int max 999999999999999

                     @JacksonXmlProperty(localName = "WidthMeasure", namespace = NS.dec)
                     widthMeasure: Option[Long] = None, // unsigned int max 999999999999999

                     @JacksonXmlProperty(localName = "HeightMeasure", namespace = NS.dec)
                     heightMeasure: Option[Long] = None, // unsigned int max 999999999999999

                     @JacksonXmlProperty(localName = "VolumeMeasure", namespace = NS.dec)
                     volumeMeasure: Option[Measure] = None)

case class AdditionalInformation(@JacksonXmlProperty(localName = "StatementCode", namespace = NS.dec)
                                 statementCode: Option[String] = None, // max 17 chars

                                 @JacksonXmlProperty(localName = "StatementDescription", namespace = NS.dec)
                                 statementDescription: Option[String] = None, // max 512 chars

                                 @JacksonXmlProperty(localName = "StatementTypeCode", namespace = NS.dec)
                                 statementTypeCode: Option[String] = None, // max 3 chars

                                 @JacksonXmlProperty(localName = "Pointer", namespace = NS.dec)
                                 pointers: Seq[Pointer] = Seq.empty) // pointers not permitted when used as part of GovernmentAgencyGoodsItem

case class Commodity(@JacksonXmlProperty(localName = "Description", namespace = NS.dec)
                     description: Option[String] = None, // max 512 chars

                     @JacksonXmlProperty(localName = "Classification", namespace = NS.dec)
                     classifications: Seq[Classification] = Seq.empty,

                     @JacksonXmlProperty(localName = "DangerousGoods")
                     dangerousGoods: Seq[DangerousGoods] = Seq.empty,

                     @JacksonXmlProperty(localName = "DutyTaxFee", namespace = NS.dec)
                     dutyTaxFees: Seq[DutyTaxFee] = Seq.empty,

                     @JacksonXmlProperty(localName = "GoodsMeasure", namespace = NS.dec)
                     goodsMeasure: Option[GoodsMeasure] = None,

                     @JacksonXmlProperty(localName = "InvoiceLine", namespace = NS.dec)
                     invoiceLine: Option[InvoiceLine] = None,

                     @JacksonXmlProperty(localName = "TransportEquipment", namespace = NS.dec)
                     transportEquipments: Seq[TransportEquipment] = Seq.empty)

case class DangerousGoods(@JacksonXmlProperty(localName = "UNDGID", namespace = NS.dec)
                          undgid: Option[String] = None) // max 4 chars; an UN Dangerous Goods Identifier

case class Classification(@JacksonXmlProperty(localName = "ID", namespace = NS.dec)
                          id: Option[String] = None, // max 18 chars

                          @JacksonXmlProperty(localName = "NameCode", namespace = NS.dec)
                          nameCode: Option[String] = None, // max 35 chars

                          @JacksonXmlProperty(localName = "IdentificationTypeCode", namespace = NS.dec)
                          identificationTypeCode: Option[String] = None, // max 3 chars

                          @JacksonXmlProperty(localName = "BindingTariffReferenceID", namespace = NS.dec)
                          bindingTariffReferenceId: Option[String] = None) // max 35 chars

case class DutyTaxFee(@JacksonXmlProperty(localName = "AdValoremTaxBaseAmount", namespace = NS.dec)
                      adValoremTaxBaseAmount: Option[Amount] = None,

                      @JacksonXmlProperty(localName = "DeductAmount", namespace = NS.dec)
                      deductAmount: Option[Amount] = None,

                      @JacksonXmlProperty(localName = "DutyRegimeCode", namespace = NS.dec)
                      dutyRegimeCode: Option[String],

                      @JacksonXmlProperty(localName = "SpecificTaxBaseQuantity", namespace = NS.dec)
                      specificTaxBaseQuantity: Option[Measure] = None,

                      @JacksonXmlProperty(localName = "TaxRateNumeric", namespace = NS.dec)
                      taxRateNumeric: Option[BigDecimal] = None, // scale 17 precision 3

                      @JacksonXmlProperty(localName = "TypeCode", namespace = NS.dec)
                      typeCode: Option[String] = None, // max 3 chars

                      @JacksonXmlProperty(localName = "QuotaOrderID", namespace = NS.dec)
                      quotaOrderId: Option[String] = None, // max 17 chars

                      @JacksonXmlProperty(localName = "Payment", namespace = NS.dec)
                      payment: Option[Payment] = None)

case class Payment(@JacksonXmlProperty(localName = "MethodCode", namespace = NS.dec)
                   methodCode: Option[String] = None,

                   @JacksonXmlProperty(localName = "TaxAssessedAmount", namespace = NS.dec)
                   taxAssessedAmount: Option[Amount] = None,

                   @JacksonXmlProperty(localName = "PaymentAmount", namespace = NS.dec)
                   paymentAmount: Option[Amount] = None)

case class GoodsMeasure(@JacksonXmlProperty(localName = "GrossMassMeasure", namespace = NS.dec)
                        grossMassMeasure: Option[Measure] = None,

                        @JacksonXmlProperty(localName = "NetWeightMeasure", namespace = NS.dec)
                        netNetWeightMeasure: Option[Measure] = None,

                        @JacksonXmlProperty(localName = "TariffQuantity", namespace = NS.dec)
                        tariffQuantity: Option[Measure] = None)

case class InvoiceLine(@JacksonXmlProperty(localName = "ItemChargeAmount", namespace = NS.dec)
                       itemChargeAmount: Option[Amount] = None)

case class CustomsValuation(@JacksonXmlProperty(localName = "MethodCode", namespace = NS.dec)
                            methodCode: Option[String] = None, // max 3 chars; not valid outside GovernmentAgencyGoodsItem

                            @JacksonXmlProperty(localName = "FreightChargeAmount", namespace = NS.dec)
                            freightChargeAmount: Option[BigDecimal] = None, // scale 16 precision 3; not valid when used as child of GovernmentAgencyGoodsItem

                            @JacksonXmlProperty(localName = "ChargeDeduction", namespace = NS.dec)
                            chargeDeductions: Seq[ChargeDeduction] = Seq.empty)

case class ChargeDeduction(@JacksonXmlProperty(localName = "ChargesTypeCode", namespace = NS.dec)
                           chargesTypeCode: Option[String] = None, // max 3 chars

                           @JacksonXmlProperty(localName = "OtherChargeDeductionAmount", namespace = NS.dec)
                           otherChargeDeductionAmount: Option[Amount] = None) // scale 16 precision 3

case class Destination(@JacksonXmlProperty(localName = "CountryCode", namespace = NS.dec)
                       countryCode: Option[String] = None, // max 3 chars; an ISO 3166-1 alpha2 code

                       @JacksonXmlProperty(localName = "RegionID", namespace = NS.dec)
                       regionId: Option[String] = None) // max 9 chars

case class GovernmentProcedure(@JacksonXmlProperty(localName = "CurrentCode", namespace = NS.dec)
                               currentCode: Option[String] = None, // max 7 chars

                               @JacksonXmlProperty(localName = "PreviousCode", namespace = NS.dec)
                               previousCode: Option[String] = None) // max 7 chars

case class Origin(@JacksonXmlProperty(localName = "CountryCode", namespace = NS.dec)
                  countryCode: Option[String] = None, // max 4 chars; expects ISO-3166-1 alpha2 code

                  @JacksonXmlProperty(localName = "RegionID", namespace = NS.dec)
                  regionId: Option[String] = None, // max 9 chars

                  @JacksonXmlProperty(localName = "TypeCode", namespace = NS.dec)
                  typeCode: Option[String] = None) // max 3 chars

case class ObligationGuarantee(@JacksonXmlProperty(localName = "AmountAmount", namespace = NS.dec)
                               amount: Option[BigDecimal] = None, // scale 16 precision 3

                               @JacksonXmlProperty(localName = "ID", namespace = NS.dec)
                               id: Option[String] = None, // max 70 chars

                               @JacksonXmlProperty(localName = "ReferenceID", namespace = NS.dec)
                               referenceId: Option[String] = None, // max 35 chars

                               @JacksonXmlProperty(localName = "SecurityDetailsCode", namespace = NS.dec)
                               securityDetailsCode: Option[String] = None, // max 3 chars

                               @JacksonXmlProperty(localName = "AccessCode", namespace = NS.dec)
                               accessCode: Option[String] = None, // max 4 chars

                               @JacksonXmlProperty(localName = "GuaranteeOffice", namespace = NS.dec)
                               guaranteeOffice: Option[Office] = None)

/* --------------------- PLACE THE MORE GENERIC, RE-USABLE TYPES BELOW -------------------- */

case class Address(@JacksonXmlProperty(localName = "CityName", namespace = NS.dec)
                   cityName: Option[String] = None, // max length 35

                   @JacksonXmlProperty(localName = "CountryCode", namespace = NS.dec)
                   countryCode: Option[String] = None, // 2 chars [a-zA-Z] ISO 3166-1 2-alpha

                   @JacksonXmlProperty(localName = "CountrySubDivisionCode", namespace = NS.dec)
                   countrySubDivisionCode: Option[String] = None, // max 9 chars

                   @JacksonXmlProperty(localName = "CountrySubDivisionName", namespace = NS.dec)
                   countrySubDivisionName: Option[String] = None, // max 35 chars

                   @JacksonXmlProperty(localName = "Line", namespace = NS.dec)
                   line: Option[String] = None, // max 70 chars

                   @JacksonXmlProperty(localName = "PostcodeID", namespace = NS.dec)
                   postcodeId: Option[String] = None) // max 9 chars

@JsonDeserialize(using = classOf[AmountDeserializer])
case class Amount(@JacksonXmlProperty(localName = "currencyID", isAttribute = true)
                  currencyId: Option[String] = None, // and ISO 4217 3 char currency code (i.e. "GBP")

                  @JacksonXmlText
                  value: Option[BigDecimal] = None) // scale of 16 and precision of 3

class AmountDeserializer extends StdAttributeAndTextDeserializer[Amount]("currencyID", classOf[Amount]) {

  override def newInstanceFromTuple(values: (Option[String], Option[String])): Amount = Amount(values._1, values._2.map(BigDecimal(_)))

}

case class Communication(@JacksonXmlProperty(localName = "ID", namespace = NS.dec)
                         id: Option[String] = None, // max 50 chars

                         @JacksonXmlProperty(localName = "TypeCode", namespace = NS.dec)
                         typeCode: Option[String] = None) // max 3 chars

case class Contact(@JacksonXmlProperty(localName = "Name", namespace = NS.dec)
                   name: Option[String] = None) // max 70 chars

case class DateTimeElement(@JacksonXmlProperty(localName = "DateTimeString", namespace = NS.ds)
                           dateTimeString: DateTimeString)

@JsonDeserialize(using = classOf[DateTimeStringDeserializer])
case class DateTimeString(@JacksonXmlProperty(localName = "formatCode", isAttribute = true)
                          formatCode: String, // either "102" or "304"

                          @JacksonXmlText
                          value: String) // max 35 chars

class DateTimeStringDeserializer extends StdAttributeAndTextDeserializer[DateTimeString]("formatCode", classOf[DateTimeString]) {

  override def newInstanceFromTuple(values: (Option[String], Option[String])): DateTimeString = DateTimeString(values._1.getOrElse(""), values._2.getOrElse(""))

}

case class ImportExportParty(@JacksonXmlProperty(localName = "Name", namespace = NS.dec)
                             name: Option[String] = None, // max 70 chars

                             @JacksonXmlProperty(localName = "ID", namespace = NS.dec)
                             id: Option[String] = None, // max 17 chars

                             @JacksonXmlProperty(localName = "Address", namespace = NS.dec)
                             address: Option[Address] = None,

                             @JacksonXmlProperty(localName = "Contact", namespace = NS.dec)
                             contacts: Seq[Contact] = Seq.empty,

                             @JacksonXmlProperty(localName = "Communication", namespace = NS.dec)
                             communications: Seq[Communication] = Seq.empty)

@JsonDeserialize(using = classOf[MeasureDeserializer])
case class Measure(@JacksonXmlProperty(localName = "unitCode", isAttribute = true)
                   unitCode: Option[String] = None, // min 1 max 5 chars when specified

                   @JacksonXmlText
                   value: Option[BigDecimal] = None) // scale 16 precision 6

class MeasureDeserializer extends StdAttributeAndTextDeserializer[Measure]("unitCode", classOf[Measure]) {

  override def newInstanceFromTuple(values: (Option[String], Option[String])): Measure = Measure(values._1, values._2.map(BigDecimal(_)))

}

case class NamedEntityWithAddress(@JacksonXmlProperty(localName = "Name", namespace = NS.dec)
                                  name: Option[String] = None, // max 70 chars

                                  @JacksonXmlProperty(localName = "ID", namespace = NS.dec)
                                  id: Option[String] = None, // max 17 chars

                                  @JacksonXmlProperty(localName = "Address", namespace = NS.dec)
                                  address: Option[Address] = None)

case class Office(@JacksonXmlProperty(localName = "ID", namespace = NS.dec)
                  id: Option[String]) // max 17 chars

case class Pointer(@JacksonXmlProperty(localName = "SequenceNumeric", namespace = NS.dec)
                   sequenceNumeric: Option[Int] = None, // min 0 max 99999

                   @JacksonXmlProperty(localName = "DocumentSectionCode", namespace = NS.dec)
                   documentSectionCode: Option[String] = None, // max 3 chars

                   @JacksonXmlProperty(localName = "TagID", namespace = NS.dec)
                   tagId: Option[String] = None) // max 4 chars


case class RoleBasedParty(@JacksonXmlProperty(localName = "ID", namespace = NS.dec)
                          id: Option[String] = None, // max 17 chars

                          @JacksonXmlProperty(localName = "RoleCode", namespace = NS.dec)
                          roleCode: Option[String] = None) // max 3 chars

abstract class StdAttributeAndTextDeserializer[T](attributeName: String, t: Class[T]) extends StdDeserializer[T](t) {

  def newInstanceFromTuple(values: (Option[String], Option[String])): T

  override final def deserialize(p: JsonParser, ctx: DeserializationContext): T = {
    val n: JsonNode = p.getCodec.readTree(p)
    n match {
      case o: ObjectNode => newInstanceFromTuple((nonEmptyOrNone(o.get(attributeName)), nonEmptyOrNone(o.get(""))))
      case t: TextNode => newInstanceFromTuple((None, nonEmptyOrNone(t)))
    }
  }

  private def nonEmptyOrNone(n: JsonNode): Option[String] = if (n == null || n.asText() == null || n.asText().trim.isEmpty) None else Some(n.asText())

}
