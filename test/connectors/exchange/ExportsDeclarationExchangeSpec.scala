/*
 * Copyright 2022 HM Revenue & Customs
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

package connectors.exchange

import java.time.Instant

import base.UnitWithMocksSpec
import forms.declaration._
import forms.declaration.additionaldeclarationtype.AdditionalDeclarationType.AdditionalDeclarationType
import forms.declaration.countries.Country
import forms.{Ducr, Lrn}
import models.declaration._
import models.{DeclarationStatus, DeclarationType, ExportsDeclaration}
import org.scalatest.OptionValues
import play.api.libs.json.{JsSuccess, Json}
import services.cache.ExportsDeclarationBuilder

class ExportsDeclarationExchangeSpec extends UnitWithMocksSpec with ExportsDeclarationBuilder with OptionValues {

  private val id = "id"
  private val sourceId = "source-id"
  private val status = DeclarationStatus.COMPLETE
  private val `type` = DeclarationType.STANDARD
  private val createdDate = Instant.MIN
  private val updatedDate = Instant.MAX
  private val additionalDeclarationType = mock[AdditionalDeclarationType]

  private val consignmentReferences = ConsignmentReferences(Ducr(""), Lrn(""))

  private val parties = Parties()
  private val locations = Locations()
  private val item = ExportItem("itemId")
  private val totalItemExchange = TotalItemsExchange(None, None, None, None)
  private val totalPackageQuantity = TotalPackageQuantity(None)
  private val previousDocuments = PreviousDocumentsData(Seq.empty)
  private val natureOfTransaction = NatureOfTransaction("")
  private val transport = Transport()

  private val request = ExportsDeclarationExchange(
    id = Some(id),
    status = status,
    createdDateTime = createdDate,
    updatedDateTime = updatedDate,
    sourceId = Some(sourceId),
    `type` = `type`,
    additionalDeclarationType = Some(additionalDeclarationType),
    consignmentReferences = Some(consignmentReferences),
    transport = transport,
    parties = parties,
    locations = locations,
    items = Seq(item),
    totalNumberOfItems = Some(totalItemExchange),
    previousDocuments = Some(previousDocuments),
    natureOfTransaction = Some(natureOfTransaction)
  )

  private val declaration = ExportsDeclaration(
    id = id,
    status = status,
    createdDateTime = createdDate,
    updatedDateTime = updatedDate,
    sourceId = Some(sourceId),
    `type` = `type`,
    additionalDeclarationType = Some(additionalDeclarationType),
    consignmentReferences = Some(consignmentReferences),
    transport = transport,
    parties = parties,
    locations = locations,
    items = Seq(item),
    totalNumberOfItems = None,
    totalPackageQuantity = Some(totalPackageQuantity),
    previousDocuments = Some(previousDocuments),
    natureOfTransaction = Some(natureOfTransaction)
  )

  "Request" should {

    "have originationCountry hard-coded to 'GB'" in {
      declaration.locations.originationCountry.value mustBe Country.GB
    }

    "map to ExportsDeclaration" in {
      request.toExportsDeclaration mustBe declaration
    }

    "map from ExportsDeclaration" in {
      ExportsDeclarationExchange(declaration) mustBe request
    }

    "bindable by json payload" in {
      Json
        .parse(ExportsDeclarationExchangeSpec.declarationJson)
        .validate[ExportsDeclarationExchange] mustBe a[JsSuccess[_]]
    }

    "serialize to form that could be bindable" in {
      val declaration = aDeclaration()
      Json.toJson(ExportsDeclarationExchange(declaration)).validate[ExportsDeclarationExchange] mustBe a[JsSuccess[_]]
    }
  }
}

object ExportsDeclarationExchangeSpec {

  val declarationJson: String =
    """{
      |  "id": "6f31582e-bfd5-4b27-90be-2dca6e236b20",
      |  "eori": "GB7172755078551",
      |  "status": "DRAFT",
      |  "createdDateTime": "2019-12-10T15:52:32.681Z",
      |  "updatedDateTime": "2019-12-10T15:53:13.697Z",
      |  "type": "STANDARD",
      |  "additionalDeclarationType": "D",
      |  "consignmentReferences": {
      |    "ducr": {
      |      "ducr": "8GB123451068100-101SHIP1"
      |    },
      |    "lrn": "QSLRN7285100"
      |  },
      |  "linkDucrToMucr": {
      |    "answer": "Yes"
      |  },
      |  "mucr": {
      |    "mucr": "CZYX123A"
      |  },
      |  "transport": {
      |    "expressConsignment": {
      |      "answer": "Yes"
      |    },
      |    "transportPayment": {
      |      "paymentMethod": "H"
      |    },
      |    "containers": [
      |      {
      |        "id": "123456",
      |        "seals": []
      |      }
      |    ],
      |    "inlandModeOfTransportCode": "1",
      |    "meansOfTransportCrossingTheBorderNationality": "United Kingdom",
      |    "meansOfTransportCrossingTheBorderType": "11",
      |    "meansOfTransportCrossingTheBorderIDNumber": "Boaty McBoatface",
      |    "borderModeOfTransportCode": {
      |       "code": "1"
      |    },
      |    "meansOfTransportOnDepartureType": "11",
      |    "meansOfTransportOnDepartureIDNumber": "SHIP1"
      |  },
      |  "parties": {
      |    "exporterDetails": {
      |      "details": {
      |        "eori": "GB717572504502801"
      |      }
      |    },
      |    "consigneeDetails": {
      |      "details": {
      |        "address": {
      |          "fullName": "Bags Export",
      |          "addressLine": "1 Bags Avenue",
      |          "townOrCity": "New York",
      |          "postCode": "NA",
      |          "country": "United States of America"
      |        }
      |      }
      |    },
      |    "declarantDetails": {
      |      "details": {
      |        "eori": "GB717572504502811"
      |      }
      |    },
      |    "representativeDetails": {
      |      "details": {
      |        "eori": "GB717572504502809"
      |      },
      |      "statusCode": "3"
      |    },
      |    "declarationHoldersData": {
      |      "holders": [
      |        {
      |          "authorisationTypeCode": "AEOC",
      |          "eori": "GB717572504502811"
      |        }
      |      ]
      |    },
      |    "carrierDetails": {
      |      "details": {
      |        "address": {
      |          "fullName": "XYZ Carrier",
      |          "addressLine": "School Road",
      |          "townOrCity": "London",
      |          "postCode": "WS1 2AB",
      |          "country": "United Kingdom"
      |        }
      |      }
      |    }
      |  },
      |  "locations": {
      |    "originationCountry": {
      |      "code": "GB"
      |    },
      |    "destinationCountry": {
      |      "code": "DE"
      |    },
      |    "hasRoutingCountries": true,
      |    "routingCountries": [
      |      {
      |        "code": "FR"
      |      }
      |    ],
      |    "goodsLocation": {
      |      "country": "United Kingdom",
      |      "typeOfLocation": "A",
      |      "qualifierOfIdentification": "U",
      |      "identificationOfLocation": "FXTFXTFXT"
      |    },
      |    "officeOfExit": {
      |      "officeId": "GB000054",
      |      "answer": "Yes"
      |    },
      |    "supervisingCustomsOffice": {
      |      "supervisingCustomsOffice": "GBLBA001"
      |    },
      |    "warehouseIdentification": {}
      |  },
      |  "items": [
      |    {
      |      "id": "b12agbfd",
      |      "sequenceId": 1,
      |      "procedureCodes": {
      |        "procedureCode": "1040",
      |        "additionalProcedureCodes": [
      |          "000"
      |        ]
      |      },
      |      "fiscalInformation": {
      |        "onwardSupplyRelief": "No"
      |      },
      |      "statisticalValue": {
      |        "statisticalValue": "1000"
      |      },
      |      "commodityDetails": {
      |        "combinedNomenclatureCode": "4602191000",
      |        "descriptionOfGoods": "Straw for bottles"
      |      },
      |      "dangerousGoodsCode": {
      |        "dangerousGoodsCode": "1234"
      |      },
      |      "cusCode": {
      |        "cusCode": "12345678"
      |      },
      |      "taricCodes": [],
      |      "nactCodes": [],
      |      "packageInformation": [
      |        {
      |          "id": "3131231",
      |          "typesOfPackages": "PK",
      |          "numberOfPackages": 10,
      |          "shippingMarks": "RICH123"
      |        }
      |      ],
      |      "commodityMeasure": {
      |        "supplementaryUnits": "10",
      |        "netMass": "500",
      |        "grossMass": "700"
      |      },
      |      "additionalInformation": {
      |        "items": [
      |          {
      |            "code": "00400",
      |            "description": "EXPORTER"
      |          }
      |        ]
      |      },
      |      "additionalDocuments": {
      |        "isRequired": {
      |          "answer": "Yes"
      |        },
      |        "documents": [
      |          {
      |            "documentTypeCode": "C501",
      |            "documentIdentifier": "GBAEOC717572504502811"
      |          }
      |        ]
      |      }
      |    }
      |  ],
      |  "totalNumberOfItems": {
      |    "totalAmountInvoiced": "56764",
      |    "exchangeRate": "1.49",
      |    "totalPackage": "1"
      |  },
      |  "previousDocuments": {
      |    "documents": [
      |      {
      |        "documentType": "IF3",
      |        "documentReference": "101SHIP2"
      |      }
      |    ]
      |  },
      |  "natureOfTransaction": {
      |    "natureType": "1"
      |  }
      |}""".stripMargin
}
