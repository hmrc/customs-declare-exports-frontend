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

import play.api.libs.json.Json

case class Submission(
  eori: String,
  conversationId: String,
  ducr: String,
  lrn: Option[String] = None,
  mrn: Option[String] = None,
  status: Status
)

object Submission {
  implicit val format = Json.format[Submission]
}

case class SubmissionData(
  eori: String,
  conversationId: String,
  ducr: String,
  mrn: Option[String],
  lrn: Option[String],
  submittedTimestamp: Long,
  status: Status,
  noOfNotifications: Int
)

object SubmissionData {
  implicit val format = Json.format[SubmissionData]
}
