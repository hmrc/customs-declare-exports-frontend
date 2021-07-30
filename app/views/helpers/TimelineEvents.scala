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

import config.featureFlags.SfusConfig
import javax.inject.Inject
import models.declaration.notifications.Notification
import models.declaration.submissions.Submission
import play.api.i18n.Messages
import play.twirl.api.Html
import views.html.components.upload_files_partial_for_timeline

case class TimelineEvent(title: String, dateTime: ZonedDateTime, content: Option[Html])

class TimelineEvents @Inject()(sfusConfig: SfusConfig, uploadFilesPartialForTimeline: upload_files_partial_for_timeline) {
  def apply(submission: Submission, notifications: Seq[Notification])(implicit messages: Messages): Seq[TimelineEvent] = {
    val sortedNotifications = {
      /*
      Not sure if the normalisation we are doing by using ZonedDateTime.withZoneSameInstant (dateTimeIssuedInUK)
      could potentially affect or not the order in case the source ZonedDateTime instances in Notification have
      different time zones. Accordingly, just to be safe, I decided to apply the normalisation before sorting.
       */
      notifications
        .map(notification => notification.copy(dateTimeIssued = notification.dateTimeIssuedInUK))
        .sorted
        .reverse
    }

    val IndexToMatchForUploadFilesContent = sortedNotifications.indexWhere(_.isStatusDMSDocOrDMSCtl)

    sortedNotifications.zipWithIndex.map {
      case (notification, index) =>
        val content = index match {
          case IndexToMatchForUploadFilesContent if sfusConfig.isSfusUploadEnabled => uploadFilesContent(submission.mrn)
          case _                                                                   => None
        }
        TimelineEvent(title = StatusOfSubmission.asText(notification), dateTime = notification.dateTimeIssued, content = content)
    }
  }

  private def uploadFilesContent(mrn: Option[String])(implicit messages: Messages): Option[Html] = {
    val element = uploadFilesPartialForTimeline(mrn)
    Some(new Html(List(element)))
  }
}
