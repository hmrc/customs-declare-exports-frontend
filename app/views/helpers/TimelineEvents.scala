/*
 * Copyright 2021 HM Revenue & Customs
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

package views.helpers

import java.time.ZonedDateTime

import models.declaration.notifications.Notification
import play.api.i18n.Messages
import play.twirl.api.Html

case class TimelineEvent(title: String, dateTime: ZonedDateTime, content: Option[Html]) extends Ordered[TimelineEvent] {

  def compare(that: TimelineEvent): Int =
    if (dateTime == that.dateTime) 0
    else if (dateTime.isAfter(that.dateTime)) -1
    else 1
}

object TimelineEvents {

  def apply(notifications: Seq[Notification])(implicit messages: Messages): Seq[TimelineEvent] =
    notifications.map { notification =>
      TimelineEvent(title = StatusOfSubmission.asText(notification), dateTime = notification.dateTimeIssuedInUK, content = None)
    }.sorted
}
