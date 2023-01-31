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

package models.declaration.submissions

import models.declaration.submissions.EnhancedStatus._
import play.api.libs.json.Json

import java.time.ZonedDateTime

case class Submission(
  uuid: String,
  eori: String,
  lrn: String,
  mrn: Option[String] = None,
  ducr: Option[String] = None,
  latestEnhancedStatus: Option[EnhancedStatus] = None,
  enhancedStatusLastUpdated: Option[ZonedDateTime] = None,
  actions: Seq[Action],
  latestDecId: String,
  latestVersionNo: Int = 1,
  blockAmendments: Boolean = false
) {
  lazy val allSubmissionRequestStatuses: Seq[EnhancedStatus] = (
    for {
      subRequestAction <- actions.find {
        case _: SubmissionAction => true
        case _                   => false
      }
      notificationSummaries <- subRequestAction.notifications
    } yield notificationSummaries.map(_.enhancedStatus)
  ).getOrElse(Seq.empty[EnhancedStatus])
  lazy val isStatusAcceptedOrReceived: Boolean =
    allSubmissionRequestStatuses.intersect(Seq(GOODS_ARRIVED_MESSAGE, GOODS_ARRIVED, RECEIVED)).nonEmpty
  val latestAction: Option[Action] =
    if (actions.isEmpty) None
    else Some(actions.minBy(_.requestTimestamp)(Submission.dateTimeOrdering))
  val latestCancellationAction: Option[Action] = {
    val cancelActions = actions.filter {
      case _: CancellationAction => true
      case _                     => false
    }
    if (cancelActions.nonEmpty) {
      Some(cancelActions.minBy(_.requestTimestamp)(Submission.dateTimeOrdering))
    } else None
  }
}

object Submission {

  implicit val formats = Json.format[Submission]

  val dateTimeOrdering: Ordering[ZonedDateTime] = Ordering.fromLessThan[ZonedDateTime]((a, b) => b.isBefore(a))

  implicit val ordering: Ordering[Submission] = Ordering.by[Submission, Option[ZonedDateTime]] { submission =>
    submission.latestAction.map(_.requestTimestamp)
  }(Ordering.Option(dateTimeOrdering))

  val newestEarlierOrdering: Ordering[Submission] = ordering
}
