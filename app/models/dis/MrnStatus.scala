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

package models.dis

import java.time.ZonedDateTime

import play.api.libs.json.{Json, OFormat}

case class MrnStatus(
  mrn: String,
  versionId: String,
  eori: String,
  declarationType: String,
  ucr: Option[String] = None,
  receivedDateTime: ZonedDateTime,
  releasedDateTime: Option[ZonedDateTime] = None,
  acceptanceDateTime: Option[ZonedDateTime] = None,
  createdDateTime: ZonedDateTime,
  roe: String,
  ics: String,
  irc: Option[String] = None,
  totalPackageQuantity: String,
  goodsItemQuantity: String,
  previousDocuments: Seq[PreviousDocument]
)

object MrnStatus {
  implicit val formats: OFormat[MrnStatus] = Json.format[MrnStatus]
}
