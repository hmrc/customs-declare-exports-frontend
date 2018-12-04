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

package base

import forms.MovementFormsAndIds._
import forms.{ChoiceForm, GoodsDateForm}
import play.api.libs.json._
import uk.gov.hmrc.http.cache.client.CacheMap
import uk.gov.hmrc.wco.dec.inventorylinking.common.{AgentDetails, TransportDetails, UcrBlock}
import uk.gov.hmrc.wco.dec.inventorylinking.movement.request.InventoryLinkingMovementRequest

import scala.util.Random

object ExportsTestData {

  val addressJson: JsValue = JsObject(
    Map(
      "fullName" -> JsString("Full name"),
      "building" -> JsString("Building"),
      "street" -> JsString("Street"),
      "townOrCity" -> JsString("Town or City"),
      "postcode" -> JsString("Postcode"),
      "country" -> JsString("Country")
    )
  )

  val goodsPackageJson: JsValue = JsObject(
    Map(
      "commodityCode" -> JsString("Commodity code"),
      "isDescriptionOfYourGoodsCorrect" -> JsBoolean(false),
      "isItemOnUNDGList" -> JsBoolean(false),
      "addLicenceForItem" -> JsBoolean(false),
      "noOfPackages" -> JsString("Number of packages"),
      "packageType" -> JsString("Package type"),
      "goodsInContainer" -> JsBoolean(false),
      "addAnotherPackage" -> JsBoolean(false)
    )
  )

  val jsonBody: JsValue = JsObject(
    Map(
      "ducr" -> JsString("5GB123456789000-123ABC456DEFIIIIIII"),
      "isConsolidateDucrtoWiderShipment" -> JsBoolean(false),
      "mucr" -> JsString(""),
      "isDeclarationForSomeoneElse" -> JsBoolean(false),
      "isAddressAndEORICorrect" -> JsBoolean(false),
      "haveRepresentative" -> JsBoolean(false),
      "isConsignorAddressAndEORICorrect" -> JsBoolean(false),
      "address" -> addressJson,
      "isFinalDestination" -> JsBoolean(false),
      "goodsPackage" -> goodsPackageJson,
      "doYouKnowCustomsProcedureCode" -> JsBoolean(false),
      "customsProcedure" -> JsString("Custom procedure"),
      "wasPreviousCustomsProcedure" -> JsBoolean(false),
      "additionalCustomsProcedure" -> JsString("AdditionalCustomProcedure"),
      "doYouWantAddAdditionalInformation" -> JsBoolean(false),
      "addAnotherItem" -> JsBoolean(false),
      "officeOfExit" -> JsString("Office of exit"),
      "knowConsignmentDispatchCountry" -> JsBoolean(false)
    )
  )

  val cancelJsonBody: JsValue = JsObject(
    Map(
      "wcoDataModelVersionCode" -> JsString("werwr"),
      "wcoTypeName" -> JsString("werewr-"),
      "responsibleAgencyName" -> JsString("werewr"),
      "functionalReferenceID" -> JsString("12"),
      "id" -> JsString("12"),
      "submitter.id" -> JsString("12"),
      "additionalInformation.statementDescription" -> JsString("12"),
      "additionalInformation.statementTypeCode" -> JsString("12"),
      "additionalInformation.pointer.sequenceNumeric" -> JsString("12"),
      "amendment.changeReasonCode" -> JsString("12")
    )
  )

  val wrongJson: JsValue = JsObject(
    Map(
      "ducr" -> JsString("")
    )
  )

  val correctDucrJson: JsValue = JsObject(
    Map(
      "ducr" -> JsString("5GB123456789000-123ABC456DEFIIIII")
    )
  )

  val wrongMinimumGoodsDate: JsValue = JsObject(
    Map(
      "day" -> JsNumber(0),
      "month" -> JsNumber(0),
      "year" -> JsNumber(2017),
      "hour" -> JsNumber(-1),
      "minute" -> JsNumber(-1)
    )
  )

  val wrongMaximumGoodsDate: JsValue = JsObject(
    Map(
      "day" -> JsNumber(40),
      "month" -> JsNumber(113),
      "year" -> JsNumber(2018),
      "hour" -> JsNumber(25),
      "minute" -> JsNumber(60)
    )
  )

  val goodsDate: JsValue = JsObject(
    Map(
      "day" -> JsNumber(15),
      "month" -> JsNumber(4),
      "year" -> JsNumber(2018),
      "hour" -> JsNumber(16),
      "minute" -> JsNumber(30)
    )
  )

  val emptyLocation: JsValue = JsObject(Map("" -> JsString("")))

  val location: JsValue = JsObject(
    Map(
      "agentLocation" -> JsString("Agent location"),
      "agentRole" -> JsString("Agent role"),
      "goodsLocation" -> JsString("Goods location"),
      "shed" -> JsString("Shed")
    )
  )

  val incorrectTransport: JsValue = JsObject(
    Map(
      "transportId" -> JsString("Transport Id"),
      "transportMode" -> JsString("Transport mode"),
      "transportNationality" -> JsString("Transport nationality")
    )
  )

  val correctTransport: JsValue = JsObject(
    Map(
      "transportId" -> JsString("Transport Id"),
      "transportMode" -> JsString("M"),
      "transportNationality" -> JsString("PL")
    )
  )

  def validMovementRequest(movementType: String) = InventoryLinkingMovementRequest(
    messageCode = movementType,
    agentDetails = Some(AgentDetails(
      eori = Some("QWERTY123"),
      agentLocation = Some("Location"),
      agentRole = Some("ABC")
    )),
    ucrBlock = UcrBlock(
      ucr = "GB/NLA-0YH06GF0V3CUPJC9393",
      ucrType = "D"
    ),
    goodsLocation = "Location",
    goodsArrivalDateTime = Some("2018-11-21T17:47:02"),
    goodsDepartureDateTime = Some("2018-11-21T17:47:02"),
    shedOPID = Some("ABC"),
    masterUCR = Some("GB/NLA-0YH06GF0V3CUPJC9393"),
    masterOpt = Some("A"),
    movementReference = Some("Movement Reference"),
    transportDetails = Some(TransportDetails(
      transportID = Some("Transport ID"),
      transportMode = Some("M"),
      transportNationality = Some("UK")
    ))
  )


  val choiceForm = Json.toJson(ChoiceForm("EAL"))

  def getMovementCacheMap(id:String, movementType:String) = {

    val data = Map(choiceId -> Json.toJson(ChoiceForm(movementType)),
      enterDucrId -> correctDucrJson,
      goodsDateId -> Json.toJson(GoodsDateForm("01","02","2020",None,None)),
        locationId -> location,
        transportId -> correctTransport)
    CacheMap(id,data)
  }

  val correctConsignorAddress: JsValue = JsObject(
    Map(
      "eori" -> JsString("PL213472539481923"),
      "fullName" -> JsString("Full name"),
      "address" -> JsString("Address"),
      "townOrCity" -> JsString("Town or city"),
      "postCode" -> JsString("PostCode1"),
      "country" -> JsString("PL")
    )
  )

  protected def randomString(length: Int): String = Random.alphanumeric.take(length).mkString

  val incorrectConsignorAddress: JsValue = JsObject(
    Map(
      "eori" -> JsString(randomString(18)),
      "fullName" -> JsString(randomString(71)),
      "address" -> JsString(randomString(71)),
      "townOrCity" -> JsString(randomString(36)),
      "postCode" -> JsString(randomString(10)),
      "country" -> JsString(randomString(3))
    )
  )
}
