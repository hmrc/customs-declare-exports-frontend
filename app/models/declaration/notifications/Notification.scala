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

package models.declaration.notifications

import models.declaration.submissions.SubmissionStatus._
import play.api.libs.json.{Json, OFormat}

import java.time.{ZoneId, ZonedDateTime}

case class Notification(actionId: String, mrn: String, dateTimeIssued: ZonedDateTime, status: SubmissionStatus, errors: Seq[NotificationError])
    extends Ordered[Notification] {

  def compare(that: Notification): Int =
    if (this.dateTimeIssued == that.dateTimeIssued) 0
    else if (this.dateTimeIssued.isAfter(that.dateTimeIssued)) 1
    else -1

  lazy val isStatusDMSAcc: Boolean = status == ACCEPTED

  lazy val isStatusDMSAccOrDMSRcv: Boolean = status == ACCEPTED || status == RECEIVED

  lazy val isStatusDMSCle: Boolean = status == CLEARED

  lazy val isStatusDMSDocOrDMSCtl: Boolean = status == ADDITIONAL_DOCUMENTS_REQUIRED || status == UNDERGOING_PHYSICAL_CHECK

  lazy val isStatusDMSQry: Boolean = status == QUERY_NOTIFICATION_MESSAGE

  lazy val isStatusDMSRcv: Boolean = status == RECEIVED

  lazy val isStatusDMSRej: Boolean = status == REJECTED

  lazy val dateTimeIssuedInUK: ZonedDateTime = dateTimeIssued.withZoneSameInstant(ZoneId.of("Europe/London"))
}

object Notification {
  implicit val format: OFormat[Notification] = Json.format[Notification]
}
