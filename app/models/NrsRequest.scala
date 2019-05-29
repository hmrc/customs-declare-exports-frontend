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

package models

import org.joda.time.{DateTime, LocalDate}
import play.api.libs.functional.syntax.{unlift, _}
import play.api.libs.json._
import play.api.libs.json.JodaWrites._
import play.api.libs.json.JodaReads._
import uk.gov.hmrc.auth.core.retrieve.{Credentials, LoginTimes, Name, _}
import uk.gov.hmrc.auth.core.{AffinityGroup, ConfidenceLevel}
import uk.gov.hmrc.http.controllers.RestFormats

case class NrsSubmissionResponse(nrSubmissionId: String)

object NrsSubmissionResponse {
  implicit val format: OFormat[NrsSubmissionResponse] = Json.format[NrsSubmissionResponse]
}

case class NRSSubmission(payload: String, metadata: Metadata)

object NRSSubmission {
  implicit val mdFormat: OFormat[Metadata] = Metadata.format
  implicit val format: OFormat[NRSSubmission] = Json.format[NRSSubmission]
}

case class Metadata(
  businessId: String,
  notableEvent: String,
  payloadContentType: String,
  payloadSha256Checksum: Option[String],
  userSubmissionTimestamp: DateTime,
  identityData: IdentityData,
  userAuthToken: String,
  headerData: HeaderData,
  searchKeys: SearchKeys
)

object Metadata {
  implicit val idformat: OFormat[IdentityData] = IdentityData.format
  implicit val hdWrts: Writes[HeaderData] = HeaderData.writes
  implicit val hdRds: Reads[HeaderData] = HeaderData.reads
  implicit val format: OFormat[Metadata] = Json.format[Metadata]
}

case class IdentityData(
  internalId: Option[String] = None,
  externalId: Option[String] = None,
  agentCode: Option[String] = None,
  credentials: Option[Credentials] = None,
  confidenceLevel: Option[ConfidenceLevel] = None,
  nino: Option[String] = None,
  saUtr: Option[String] = None,
  name: Option[Name] = None,
  dateOfBirth: Option[LocalDate] = None,
  email: Option[String] = None,
  agentInformation: Option[AgentInformation] = None,
  groupIdentifier: Option[String] = None,
  credentialRole: Option[String] = None,
  mdtpInformation: Option[MdtpInformation] = None,
  itmpName: Option[ItmpName] = None,
  itmpDateOfBirth: Option[LocalDate] = None,
  itmpAddress: Option[ItmpAddress] = None,
  affinityGroup: Option[AffinityGroup] = None,
  credentialStrength: Option[String] = None,
  loginTimes: Option[LoginTimes] = None
)

object IdentityData {
  implicit val localDateFormat: Format[LocalDate] = RestFormats.localDateFormats
  implicit val dateTimeReads: Format[DateTime] = RestFormats.dateTimeFormats
  implicit val credentialsFormat = Json.format[Credentials]
  implicit val nameFormat = Json.format[Name]
  implicit val agentInformationFormat = Json.format[AgentInformation]
  implicit val mdtpInformationFormat = Json.format[MdtpInformation]
  implicit val itmpNameFormat = Json.format[ItmpName]
  implicit val itmpAddressFormat = Json.format[ItmpAddress]
  implicit val loginTimesFormat = Json.format[LoginTimes]

  implicit val format: OFormat[IdentityData] = Json.format[IdentityData]
}

case class HeaderData(
  publicIp: Option[String] = None,
  port: Option[String] = None,
  deviceId: Option[String] = None,
  userId: Option[String] = None,
  timeZone: Option[String] = None,
  localIp: Option[String] = None,
  screenResolution: Option[String] = None,
  windowSize: Option[String] = None,
  colourDepth: Option[String] = None
)

object HeaderData {
  implicit val writes: Writes[HeaderData] = (
    (__ \ "Gov-Client-Public-IP").writeNullable[String] and
      (__ \ "Gov-Client-Public-Port").writeNullable[String] and
      (__ \ "Gov-Client-Device-ID").writeNullable[String] and
      (__ \ "Gov-Client-User-ID").writeNullable[String] and
      (__ \ "Gov-Client-Timezone").writeNullable[String] and
      (__ \ "Gov-Client-Local-IP").writeNullable[String] and
      (__ \ "Gov-Client-Screen-Resolution").writeNullable[String] and
      (__ \ "Gov-Client-Window-Size").writeNullable[String] and
      (__ \ "Gov-Client-Colour-Depth").writeNullable[String]
  )(unlift(HeaderData.unapply))

  implicit val reads: Reads[HeaderData] = (
    (__ \ "Gov-Client-Public-IP").readNullable[String] and
      (__ \ "Gov-Client-Public-Port").readNullable[String] and
      (__ \ "Gov-Client-Device-ID").readNullable[String] and
      (__ \ "Gov-Client-User-ID").readNullable[String] and
      (__ \ "Gov-Client-Timezone").readNullable[String] and
      (__ \ "Gov-Client-Local-IP").readNullable[String] and
      (__ \ "Gov-Client-Screen-Resolution").readNullable[String] and
      (__ \ "Gov-Client-Window-Size").readNullable[String] and
      (__ \ "Gov-Client-Colour-Depth").readNullable[String]
  )(HeaderData.apply _)
}

case class SearchKeys(conversationId: Option[String], ducr: Option[String])

object SearchKeys {
  implicit val localDateFormat: Format[LocalDate] = RestFormats.localDateFormats
  implicit val format: OFormat[SearchKeys] = Json.format[SearchKeys]
}
