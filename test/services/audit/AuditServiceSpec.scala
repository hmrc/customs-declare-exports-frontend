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

package services.audit

import base.{ExportsTestData, Injector, TestHelper, UnitWithMocksSpec}
import config.AppConfig
import config.featureFlags.SecureMessagingConfig
import models.AuthKey.enrolment
import models.ExportsDeclaration
import models.declaration.ExportDeclarationTestData.{allRecordsXmlMarshallingTest, cancellationDeclarationTest}
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, verify, when}
import org.mockito.stubbing.OngoingStubbing
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{Assertion, BeforeAndAfterEach}
import play.api.libs.json.{JsObject, JsString, Json}
import services.audit.AuditTypes.SubmissionPayload
import services.cache.ExportsDeclarationBuilder
import uk.gov.hmrc.http.{Authorization, HeaderCarrier}
import uk.gov.hmrc.play.audit.AuditExtensions
import uk.gov.hmrc.play.audit.http.connector.AuditResult.{Disabled, Failure, Success}
import uk.gov.hmrc.play.audit.http.connector.{AuditConnector, AuditResult}
import uk.gov.hmrc.play.audit.model.{DataEvent, ExtendedDataEvent}

import scala.concurrent.ExecutionContext.global
import scala.concurrent.{ExecutionContext, Future}

class AuditServiceSpec extends AuditTestSupport with BeforeAndAfterEach {

  override def afterEach(): Unit = {
    reset(auditConnector)
    super.afterEach()
  }

  "AuditService" should {

    "audit an event" in {
      mockDataEvent()
      auditService.audit(AuditTypes.Submission, auditData)(hc)
      verifyDataEvent(event)
    }

    "audit full payload" in {
      mockExtendedDataEvent
      auditService.auditAllPagesUserInput(SubmissionPayload, allRecordsXmlMarshallingTest)(hc)
      verifyExtendedDataEvent(extendedSubmissionEvent)
    }

    "audit Amendment payload" in {
      mockExtendedDataEvent
      auditService.auditAmendmentSent(AuditTypes.Amendment, amendmentJson)(hc)
      verifyExtendedDataEvent(extendedAmendmentEvent)
    }

    "audit Cancellation payload" in {
      mockExtendedDataEvent
      auditService.auditAllPagesDeclarationCancellation(cancellationDeclarationTest)(hc)
      verifyExtendedDataEvent(extendedCancellationEvent)
    }

    "audit the successful retrieval of the message inbox partial" in {
      mockExtendedDataEvent
      auditService.auditMessageInboxPartialRetrieved(ExportsTestData.eori, secureMessagingConfig.notificationType, secureMessagingConfig.fetchInbox)(
        hc
      )
      verifyExtendedDataEvent(eventForMessageInboxPartialRetrieved)
    }

    "audit full payload success" in {
      mockExtendedDataEvent
      val res = auditService.auditAllPagesUserInput(SubmissionPayload, allRecordsXmlMarshallingTest)(hc).futureValue
      res mustBe AuditResult.Success
    }

    "audit with a success" in {
      mockDataEvent()
      auditService.audit(AuditTypes.Submission, auditData)(hc).futureValue mustBe AuditResult.Success
    }

    "handle audit failure" in {
      mockDataEvent(auditFailure)
      auditService.audit(AuditTypes.Submission, auditData)(hc).futureValue mustBe auditFailure
    }

    "handled audit disabled" in {
      mockDataEvent(Disabled)
      auditService.audit(AuditTypes.Submission, auditData)(hc).futureValue mustBe AuditResult.Disabled
    }

    "remove empty Json Arrays, and Json Objects, from an ExtendedDataEvent's payload" in {
      mockExtendedDataEvent
      auditService.auditAllPagesUserInput(SubmissionPayload, declarationWithEmptyValues)(hc).futureValue
      sentExtendedDataEvent.detail mustBe Json.parse(declarationWithoutEmptyValues)
    }
  }
}

trait AuditTestSupport extends UnitWithMocksSpec with ExportsDeclarationBuilder with ScalaFutures with Injector {

  val auditConnector = mock[AuditConnector]

  val auditData = Map(
    EventData.eori.toString -> "eori1",
    EventData.lrn.toString -> "lrn1",
    EventData.ducr.toString -> "ducr1",
    EventData.submissionResult.toString -> "Success"
  )

  val appConfig = instanceOf[AppConfig]
  val secureMessagingConfig = instanceOf[SecureMessagingConfig]
  val hc: HeaderCarrier = HeaderCarrier(authorization = Some(Authorization(TestHelper.createRandomString(255))))

  private val auditCarrierDetails: Map[String, String] = AuditExtensions.auditHeaderCarrier(hc).toAuditDetails()

  val event = DataEvent(
    auditSource = appConfig.appName,
    auditType = AuditTypes.Submission.toString,
    tags = AuditExtensions
      .auditHeaderCarrier(hc)
      .toAuditTags(
        transactionName = s"export-declaration-${AuditTypes.Submission.toString.toLowerCase}-request",
        path = s"customs-declare-exports/${AuditTypes.Submission.toString}"
      ),
    detail = auditCarrierDetails ++ auditData
  )

  private val declarationAsJson: JsObject = Json.toJson(allRecordsXmlMarshallingTest).as[JsObject]

  val extendedSubmissionEvent = ExtendedDataEvent(
    auditSource = appConfig.appName,
    auditType = SubmissionPayload.toString,
    tags = AuditExtensions
      .auditHeaderCarrier(hc)
      .toAuditTags(
        transactionName = s"export-declaration-${SubmissionPayload.toString.toLowerCase}-payload-request",
        path = s"customs-declare-exports/${SubmissionPayload.toString}/full-payload"
      ),
    detail = Json
      .toJson(auditCarrierDetails)
      .as[JsObject]
      .deepMerge(declarationAsJson)
  )

  private val cancelDeclarationAsJson: JsObject = Json.toJson(cancellationDeclarationTest).as[JsObject]

  val extendedCancellationEvent = ExtendedDataEvent(
    auditSource = appConfig.appName,
    auditType = AuditTypes.CancellationPayload.toString,
    tags = AuditExtensions
      .auditHeaderCarrier(hc)
      .toAuditTags(
        transactionName = s"export-declaration-${AuditTypes.CancellationPayload.toString.toLowerCase}-payload-request",
        path = s"customs-declare-exports/${AuditTypes.CancellationPayload.toString}/full-payload"
      ),
    detail = Json
      .toJson(auditCarrierDetails)
      .as[JsObject]
      .deepMerge(cancelDeclarationAsJson)
  )

  val eventForMessageInboxPartialRetrieved = ExtendedDataEvent(
    auditSource = appConfig.appName,
    auditType = AuditTypes.NavigateToMessages.toString,
    tags = AuditExtensions
      .auditHeaderCarrier(hc)
      .toAuditTags(transactionName = "callExportPartial", path = secureMessagingConfig.fetchInbox),
    detail = Json.obj(
      "enrolment" -> enrolment,
      "eoriNumber" -> ExportsTestData.eori,
      "tags" -> Json.obj("notificationType" -> secureMessagingConfig.notificationType)
    )
  )

  val amendmentJson = new JsObject(
    Map(EventData.preAmendmentDeclaration.toString -> JsString("someDec"), EventData.postAmendmentDeclaration.toString -> JsString("otherDec"))
  )

  val extendedAmendmentEvent = ExtendedDataEvent(
    auditSource = appConfig.appName,
    auditType = AuditTypes.Amendment.toString,
    tags = AuditExtensions
      .auditHeaderCarrier(hc)
      .toAuditTags(
        transactionName = s"export-declaration-${AuditTypes.Amendment.toString.toLowerCase}-request",
        path = s"customs-declare-exports/${AuditTypes.Amendment.toString}"
      ),
    detail = Json
      .toJson(auditCarrierDetails)
      .as[JsObject]
      .deepMerge(amendmentJson)
  )

  val auditFailure = Failure("Event sending failed")

  val auditService = new AuditService(auditConnector, appConfig)(global)

  def mockDataEvent(result: AuditResult = Success): OngoingStubbing[Future[AuditResult]] =
    when(auditConnector.sendEvent(any[DataEvent])(any[HeaderCarrier], any[ExecutionContext]))
      .thenReturn(Future.successful(result))

  def mockExtendedDataEvent: OngoingStubbing[Future[AuditResult]] =
    when(auditConnector.sendExtendedEvent(any[ExtendedDataEvent])(any[HeaderCarrier], any[ExecutionContext]))
      .thenReturn(Future.successful(Success))

  def sentExtendedDataEvent: ExtendedDataEvent = {
    val captor: ArgumentCaptor[ExtendedDataEvent] = ArgumentCaptor.forClass(classOf[ExtendedDataEvent])
    verify(auditConnector).sendExtendedEvent(captor.capture())(any(), any())
    captor.getValue
  }

  def verifyDataEvent(expected: DataEvent): Assertion = {
    val captor = ArgumentCaptor.forClass(classOf[DataEvent])
    verify(auditConnector).sendEvent(captor.capture())(any(), any())

    val actual: DataEvent = captor.getValue
    actual.auditSource mustBe expected.auditSource
    actual.auditType mustBe expected.auditType
    actual.tags mustBe expected.tags
    actual.detail mustBe expected.detail
  }

  def verifyExtendedDataEvent(expected: ExtendedDataEvent): Assertion = {
    val captor = ArgumentCaptor.forClass(classOf[ExtendedDataEvent])
    verify(auditConnector).sendExtendedEvent(captor.capture())(any(), any())

    val actual: ExtendedDataEvent = captor.getValue
    actual.auditSource mustBe expected.auditSource
    actual.auditType mustBe expected.auditType
    actual.tags mustBe expected.tags
    actual.detail mustBe expected.detail
  }

  val declarationWithEmptyValues: ExportsDeclaration =
    Json
      .parse("""{
      |  "id" : "5f059e5d-389a-4f73-85d8-f577300e4a46",
      |  "declarationMeta" : {
      |      "status" : "DRAFT",
      |      "createdDateTime" : "2024-04-30T08:56:51.387Z",
      |      "updatedDateTime" : "2024-05-02T14:25:34.310Z",
      |      "summaryWasVisited" : true,
      |      "readyForSubmission" : true,
      |      "maxSequenceIds" : {
      |          "dummy" : -1,
      |          "ExportItems" : 1,
      |          "PackageInformation" : 1,
      |          "Containers" : 1
      |      }
      |  },
      |  "type" : "STANDARD",
      |  "additionalDeclarationType" : "D",
      |  "consignmentReferences" : {
      |      "ducr" : {
      |          "ducr" : "8GB123456802352-101SHIP1"
      |      },
      |      "lrn" : "DSLRN695100"
      |  },
      |  "linkDucrToMucr" : {
      |      "answer" : "Yes"
      |  },
      |  "mucr" : {
      |      "mucr" : "GB/AZ09-B12345"
      |  },
      |  "transport" : {
      |      "expressConsignment" : {
      |          "answer" : "Yes"
      |      },
      |      "transportPayment" : {
      |          "paymentMethod" : "H"
      |      },
      |      "containers" : [
      |          {
      |              "sequenceId" : 1,
      |              "id" : "123456",
      |              "seals" : []
      |          }
      |      ],
      |      "borderModeOfTransportCode" : {
      |          "code" : "6"
      |      },
      |      "meansOfTransportOnDepartureType" : "10",
      |      "meansOfTransportOnDepartureIDNumber" : "8888",
      |      "transportCrossingTheBorderNationality" : {
      |          "countryCode" : "IT"
      |      },
      |      "meansOfTransportCrossingTheBorderType" : "11",
      |      "meansOfTransportCrossingTheBorderIDNumber" : "EXTERNAL AMEND"
      |  },
      |  "parties" : {
      |      "exporterDetails" : {
      |          "details" : {
      |              "address" : {
      |                  "fullName" : "Agdtry",
      |                  "addressLine" : "AAAAAAAAAAAAA",
      |                  "townOrCity" : "ITAAA",
      |                  "postCode" : "BN11",
      |                  "country" : "AE"
      |              }
      |          }
      |      },
      |      "consigneeDetails" : {
      |          "details" : {
      |              "address" : {
      |                  "fullName" : "Bags Export",
      |                  "addressLine" : "1 Bags Avenue",
      |                  "townOrCity" : "New York",
      |                  "postCode" : "10001",
      |                  "country" : "US"
      |              }
      |          }
      |      },
      |      "declarantDetails" : {
      |          "details" : {
      |              "eori" : "GB7172755067703"
      |          }
      |      },
      |      "declarantIsExporter" : {
      |          "answer" : "No"
      |      },
      |      "declarationAdditionalActorsData" : {
      |          "actors" : []
      |      },
      |      "declarationHoldersData" : {
      |          "holders" : [
      |              {
      |                  "authorisationTypeCode" : "AEOC",
      |                  "eori" : "GB717572504502801",
      |                  "eoriSource" : "OtherEori"
      |              }
      |          ],
      |          "isRequired" : {
      |              "answer" : "Yes"
      |          }
      |      },
      |      "authorisationProcedureCodeChoice" : {
      |          "code" : "Code1040"
      |      },
      |      "carrierDetails" : {
      |          "details" : {
      |              "address" : {
      |                  "fullName" : "XYZ Carrier",
      |                  "addressLine" : "School Road",
      |                  "townOrCity" : "London",
      |                  "postCode" : "WS1 2AB",
      |                  "country" : "GB"
      |              }
      |          }
      |      }
      |  },
      |  "locations" : {
      |      "originationCountry" : {
      |          "code" : "GB"
      |      },
      |      "destinationCountry" : {
      |          "code" : "US"
      |      },
      |      "hasRoutingCountries" : false,
      |      "routingCountries" : [],
      |      "goodsLocation" : {
      |          "country" : "GB",
      |          "typeOfLocation" : "A",
      |          "qualifierOfIdentification" : "U",
      |          "identificationOfLocation" : "ABDABDABDGVM"
      |      },
      |      "officeOfExit" : {
      |          "officeId" : "GB000434"
      |      },
      |      "inlandModeOfTransportCode" : {
      |          "inlandModeOfTransportCode" : "3"
      |      }
      |  },
      |  "items" : [
      |      {
      |          "id" : "75e51382",
      |          "sequenceId" : 1,
      |          "procedureCodes" : {
      |              "procedureCode" : "1040",
      |              "additionalProcedureCodes" : [
      |                  "000"
      |              ]
      |          },
      |          "statisticalValue" : {
      |              "statisticalValue" : "1000"
      |          },
      |          "commodityDetails" : {
      |              "combinedNomenclatureCode" : "4106920000",
      |              "descriptionOfGoods" : "Straw for bottles"
      |          },
      |          "dangerousGoodsCode" : {},
      |          "nactCodes" : [],
      |          "nactExemptionCode" : {
      |              "nactCode" : "VATZ"
      |          },
      |          "packageInformation" : [
      |              {
      |                  "sequenceId" : 1,
      |                  "id" : "hufrmqsx",
      |                  "typesOfPackages" : "XD",
      |                  "numberOfPackages" : 10,
      |                  "shippingMarks" : "Shipping description"
      |              }
      |          ],
      |          "commodityMeasure" : {
      |              "supplementaryUnits" : "10",
      |              "supplementaryUnitsNotRequired" : false,
      |              "netMass" : "500",
      |              "grossMass" : "700"
      |          },
      |          "additionalInformation" : {
      |              "isRequired" : {
      |                  "answer" : "No"
      |              },
      |              "items" : []
      |          },
      |          "additionalDocuments" : {
      |              "isRequired" : {
      |                  "answer" : "Yes"
      |              },
      |              "documents" : [
      |                  {
      |                      "documentTypeCode" : "C501",
      |                      "documentIdentifier" : "GBAEOC717572504502801"
      |                  }
      |              ]
      |          },
      |          "isLicenceRequired" : true
      |      }
      |  ],
      |  "totalNumberOfItems" : {
      |      "totalAmountInvoiced" : "567640",
      |      "totalAmountInvoicedCurrency" : "GBP",
      |      "agreedExchangeRate" : "Yes",
      |      "exchangeRate" : "1.49",
      |      "totalPackage" : "1"
      |  },
      |  "previousDocuments" : {
      |      "documents" : [
      |          {
      |              "documentType" : "DCS",
      |              "documentReference" : "9GB123456782317-BH1433A61"
      |          }
      |      ]
      |  },
      |  "natureOfTransaction" : {
      |      "natureType" : "1"
      |  }
      |}""".stripMargin)
      .as[ExportsDeclaration]

  val declarationWithoutEmptyValues: String =
    """{
      |  "id" : "5f059e5d-389a-4f73-85d8-f577300e4a46",
      |  "declarationMeta" : {
      |      "status" : "DRAFT",
      |      "createdDateTime" : "2024-04-30T08:56:51.387Z",
      |      "updatedDateTime" : "2024-05-02T14:25:34.310Z",
      |      "summaryWasVisited" : true,
      |      "readyForSubmission" : true,
      |      "maxSequenceIds" : {
      |          "dummy" : -1,
      |          "ExportItems" : 1,
      |          "PackageInformation" : 1,
      |          "Containers" : 1
      |      }
      |  },
      |  "type" : "STANDARD",
      |  "additionalDeclarationType" : "D",
      |  "consignmentReferences" : {
      |      "ducr" : {
      |          "ducr" : "8GB123456802352-101SHIP1"
      |      },
      |      "lrn" : "DSLRN695100"
      |  },
      |  "linkDucrToMucr" : {
      |      "answer" : "Yes"
      |  },
      |  "mucr" : {
      |      "mucr" : "GB/AZ09-B12345"
      |  },
      |  "transport" : {
      |      "expressConsignment" : {
      |          "answer" : "Yes"
      |      },
      |      "transportPayment" : {
      |          "paymentMethod" : "H"
      |      },
      |      "containers" : [
      |          {
      |              "sequenceId" : 1,
      |              "id" : "123456"
      |          }
      |      ],
      |      "borderModeOfTransportCode" : {
      |          "code" : "6"
      |      },
      |      "meansOfTransportOnDepartureType" : "10",
      |      "meansOfTransportOnDepartureIDNumber" : "8888",
      |      "transportCrossingTheBorderNationality" : {
      |          "countryCode" : "IT"
      |      },
      |      "meansOfTransportCrossingTheBorderType" : "11",
      |      "meansOfTransportCrossingTheBorderIDNumber" : "EXTERNAL AMEND"
      |  },
      |  "parties" : {
      |      "exporterDetails" : {
      |          "details" : {
      |              "address" : {
      |                  "fullName" : "Agdtry",
      |                  "addressLine" : "AAAAAAAAAAAAA",
      |                  "townOrCity" : "ITAAA",
      |                  "postCode" : "BN11",
      |                  "country" : "AE"
      |              }
      |          }
      |      },
      |      "consigneeDetails" : {
      |          "details" : {
      |              "address" : {
      |                  "fullName" : "Bags Export",
      |                  "addressLine" : "1 Bags Avenue",
      |                  "townOrCity" : "New York",
      |                  "postCode" : "10001",
      |                  "country" : "US"
      |              }
      |          }
      |      },
      |      "declarantDetails" : {
      |          "details" : {
      |              "eori" : "GB7172755067703"
      |          }
      |      },
      |      "declarantIsExporter" : {
      |          "answer" : "No"
      |      },
      |      "declarationHoldersData" : {
      |          "holders" : [
      |              {
      |                  "authorisationTypeCode" : "AEOC",
      |                  "eori" : "GB717572504502801",
      |                  "eoriSource" : "OtherEori"
      |              }
      |          ],
      |          "isRequired" : {
      |              "answer" : "Yes"
      |          }
      |      },
      |      "authorisationProcedureCodeChoice" : {
      |          "code" : "Code1040"
      |      },
      |      "carrierDetails" : {
      |          "details" : {
      |              "address" : {
      |                  "fullName" : "XYZ Carrier",
      |                  "addressLine" : "School Road",
      |                  "townOrCity" : "London",
      |                  "postCode" : "WS1 2AB",
      |                  "country" : "GB"
      |              }
      |          }
      |      }
      |  },
      |  "locations" : {
      |      "originationCountry" : {
      |          "code" : "GB"
      |      },
      |      "destinationCountry" : {
      |          "code" : "US"
      |      },
      |      "hasRoutingCountries" : false,
      |      "goodsLocation" : {
      |          "country" : "GB",
      |          "typeOfLocation" : "A",
      |          "qualifierOfIdentification" : "U",
      |          "identificationOfLocation" : "ABDABDABDGVM"
      |      },
      |      "officeOfExit" : {
      |          "officeId" : "GB000434"
      |      },
      |      "inlandModeOfTransportCode" : {
      |          "inlandModeOfTransportCode" : "3"
      |      }
      |  },
      |  "items" : [
      |      {
      |          "id" : "75e51382",
      |          "sequenceId" : 1,
      |          "procedureCodes" : {
      |              "procedureCode" : "1040",
      |              "additionalProcedureCodes" : [
      |                  "000"
      |              ]
      |          },
      |          "statisticalValue" : {
      |              "statisticalValue" : "1000"
      |          },
      |          "commodityDetails" : {
      |              "combinedNomenclatureCode" : "4106920000",
      |              "descriptionOfGoods" : "Straw for bottles"
      |          },
      |          "nactExemptionCode" : {
      |              "nactCode" : "VATZ"
      |          },
      |          "packageInformation" : [
      |              {
      |                  "sequenceId" : 1,
      |                  "id" : "hufrmqsx",
      |                  "typesOfPackages" : "XD",
      |                  "numberOfPackages" : 10,
      |                  "shippingMarks" : "Shipping description"
      |              }
      |          ],
      |          "commodityMeasure" : {
      |              "supplementaryUnits" : "10",
      |              "supplementaryUnitsNotRequired" : false,
      |              "netMass" : "500",
      |              "grossMass" : "700"
      |          },
      |          "additionalInformation" : {
      |              "isRequired" : {
      |                  "answer" : "No"
      |              }
      |          },
      |          "additionalDocuments" : {
      |              "isRequired" : {
      |                  "answer" : "Yes"
      |              },
      |              "documents" : [
      |                  {
      |                      "documentTypeCode" : "C501",
      |                      "documentIdentifier" : "GBAEOC717572504502801"
      |                  }
      |              ]
      |          },
      |          "isLicenceRequired" : true
      |      }
      |  ],
      |  "totalNumberOfItems" : {
      |      "totalAmountInvoiced" : "567640",
      |      "totalAmountInvoicedCurrency" : "GBP",
      |      "agreedExchangeRate" : "Yes",
      |      "exchangeRate" : "1.49",
      |      "totalPackage" : "1"
      |  },
      |  "previousDocuments" : {
      |      "documents" : [
      |          {
      |              "documentType" : "DCS",
      |              "documentReference" : "9GB123456782317-BH1433A61"
      |          }
      |      ]
      |  },
      |  "natureOfTransaction" : {
      |      "natureType" : "1"
      |  }
      |}""".stripMargin
}
