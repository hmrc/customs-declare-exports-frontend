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

package models.declaration.submissions

import models.declaration.submissions.Action.defaultDateTimeZone

import java.time.{ZoneId, ZonedDateTime}
import play.api.libs.json.Json

case class Action(
  id: String,
  requestType: RequestType,
  requestTimestamp: ZonedDateTime = ZonedDateTime.now(defaultDateTimeZone),
  notifications: Option[Seq[NotificationSummary]]
) {
  val latestNotificationSummary: Option[NotificationSummary] =
    notifications.flatMap(_.lastOption)
}

object Action {
  implicit val format = Json.format[Action]

  val dateTimeOrdering: Ordering[ZonedDateTime] = Ordering.fromLessThan[ZonedDateTime]((a, b) => b.isBefore(a))

  val defaultDateTimeZone: ZoneId = ZoneId.of("UTC")
}
