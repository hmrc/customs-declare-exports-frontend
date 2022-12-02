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

package base

import forms.Choice
import forms.Choice.AllowedChoiceValues._
import forms.Choice._
import forms.declaration.LocationOfGoods
import forms.declaration.ModeOfTransportCode.RoRo
import forms.declaration.declarationHolder.AuthorizationTypeCodes.{CSE, EXRR}
import models.AuthKey.{enrolment, identifierKey}
import models.codes.AdditionalProcedureCode.NO_APC_APPLIES_CODE
import models.declaration.ProcedureCodesData.warehouseRequiredProcedureCodes
import models.declaration.{ExportItem, ProcedureCodesData}
import models.{IdentityData, SignedInUser}
import play.api.libs.json._
import services.cache.{ExportsDeclarationBuilder, ExportsItemBuilder}
import uk.gov.hmrc.auth.core.AffinityGroup.Individual
import uk.gov.hmrc.auth.core.ConfidenceLevel.L50
import uk.gov.hmrc.auth.core.retrieve._
import uk.gov.hmrc.auth.core.{Enrolment, Enrolments, User}

import java.time.{Instant, LocalDate, ZoneId, ZonedDateTime}
import java.util.UUID

object ExportsTestData extends ExportsDeclarationBuilder with ExportsItemBuilder {

  val exportsCacheModelFull = aDeclaration()

  val nrsCredentials = Credentials(providerId = "providerId", providerType = "providerType")
  val nrsGroupIdentifierValue = Some("groupIdentifierValue")
  val nrsCredentialRole = Some(User)
  val nrsMdtpInformation = MdtpInformation("deviceId", "sessionId")
  val nrsItmpName = ItmpName(Some("givenName"), Some("middleName"), Some("familyName"))
  val nrsItmpAddress =
    ItmpAddress(Some("line1"), Some("line2"), Some("line3"), Some("line4"), Some("line5"), Some("postCode"), Some("countryName"), Some("countryCode"))
  val nrsAffinityGroup = Some(Individual)
  val nrsCredentialStrength = Some("STRONG")
  val nrsDateOfBirth = Some(LocalDate.now().minusYears(25))
  val eori = "GB123456789012000"
  val lrn = "123LRN"
  val lrnWithSpaces = "123 LRN"
  val ducr = "5GB123456789000-123ABC456DEFIIIII"
  val mrn = "20GB46J8TMJ4RFGVA0"
  val mucr = "CZYX123A"
  val eidrDateStamp = "20001231"

  val pc1040 = Some(ProcedureCodesData(Some("1040"), List(NO_APC_APPLIES_CODE)))

  def itemWithPC(
    procedureCode: String,
    additionalProcedureCodes: Seq[String] = List(NO_APC_APPLIES_CODE),
    itemId: String = UUID.randomUUID.toString
  ) =
    ExportItem(itemId, procedureCodes = Some(ProcedureCodesData(Some(procedureCode), additionalProcedureCodes)))

  val zoneUTC = ZoneId.of("UTC")

  val currentLoginTime: Instant = Instant.ofEpochSecond(1530442800000L)
  val previousLoginTime: Instant = Instant.ofEpochSecond(1530464400000L)
  val nrsTimeStamp: ZonedDateTime = Instant.ofEpochSecond(1530475200000L).atZone(zoneUTC)

  val nrsLoginTimes = LoginTimes(currentLoginTime, Some(previousLoginTime))

  val valuesRequiringToSkipInlandOrBorder = List(
    withDeclarationHolders(Some(CSE)),
    withDeclarationHolders(Some(EXRR)),
    withBorderModeOfTransportCode(Some(RoRo)),
    withGoodsLocation(LocationOfGoods("GBAUCBRLHRXXD")) // GBAUCBRLHRXXD => DEP location code
  )

  val allValuesRequiringToSkipInlandOrBorder =
    valuesRequiringToSkipInlandOrBorder ++
      warehouseRequiredProcedureCodes.map(pc => withItem(anItem(withProcedureCodes(Some(s"12$pc")))))

  def newUser(eori: String, externalId: String): SignedInUser =
    SignedInUser(
      eori,
      Enrolments(Set(Enrolment(enrolment).withIdentifier(identifierKey, eori))),
      IdentityData(
        Some("Int-ba17b467-90f3-42b6-9570-73be7b78eb2b"),
        Some(externalId),
        None,
        Some(nrsCredentials),
        Some(L50),
        None,
        None,
        Some(Name(Some("Aldo"), Some("Rain"))),
        Some(LocalDate.now().minusYears(25)),
        Some("amina@hmrc.co.uk"),
        Some(AgentInformation(Some("agentId"), Some("agentCode"), Some("agentFriendlyName"))),
        None,
        None,
        None,
        None,
        None,
        None,
        None,
        Some("crdentialStrength 50"),
        Some(LoginTimes(Instant.now, None))
      )
    )

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

  val wrongJson: JsValue = JsObject(Map("ducr" -> JsString("")))

  val correctDucrJson: JsValue = JsObject(Map("ducr" -> JsString("5GB123456789000-123ABC456DEFIIIII")))

  val wrongMinimumGoodsDate: JsValue = JsObject(
    Map(
      "day" -> JsNumber(0),
      "month" -> JsNumber(0),
      "year" -> JsNumber(LocalDate.now().getYear - 1),
      "hour" -> JsNumber(-1),
      "minute" -> JsNumber(-1)
    )
  )

  val wrongMaximumGoodsDate: JsValue = JsObject(
    Map(
      "day" -> JsNumber(40),
      "month" -> JsNumber(113),
      "year" -> JsNumber(LocalDate.now().getYear),
      "hour" -> JsNumber(25),
      "minute" -> JsNumber(60)
    )
  )

  val goodsDate: JsValue = JsObject(
    Map("day" -> JsNumber(15), "month" -> JsNumber(4), "year" -> JsNumber(LocalDate.now().getYear), "hour" -> JsNumber(16), "minute" -> JsNumber(30))
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
    Map("transportId" -> JsString("Transport Id"), "transportMode" -> JsString("M"), "transportNationality" -> JsString("PL"))
  )

  val choiceForm = Json.toJson(Choice("EAL"))

  val allJourneys = Seq(CreateDec, ContinueDec, Dashboard, Inbox, Movements)
}
