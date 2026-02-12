/*
 * Copyright 2024 HM Revenue & Customs
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

package models.declaration.submissions


import java.time.{ZoneId, ZonedDateTime}
import play.api.libs.json.{Json, OFormat}

case class Action(
  id: String,
  requestType: RequestType,
  requestTimestamp: ZonedDateTime = ZonedDateTime.now(defaultDateTimeZone),
  notifications: Option[Seq[NotificationSummary]],
  decId: Option[String],
  versionNo: Int
) {
  val latestNotificationSummary: Option[NotificationSummary] =
    notifications.flatMap {
      case seq if seq.isEmpty => None
      case seq                => Some(seq.minBy(_.dateTimeIssued)(Action.dateTimeOrdering))
    }
}

object Action {
  implicit val format: OFormat[Action] = Json.format[Action]

  val dateTimeOrdering: Ordering[ZonedDateTime] = Ordering.fromLessThan[ZonedDateTime]((a, b) => b.isBefore(a))

  val defaultDateTimeZone: ZoneId = ZoneId.of("UTC")
}
