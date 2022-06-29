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

import models.declaration.submissions.RequestType.CancellationRequest

import models.declaration.submissions.EnhancedStatus._
import models.declaration.submissions.RequestType.SubmissionRequest

import java.time.ZonedDateTime
import java.util.UUID

import play.api.libs.json.Json

case class Submission(
  uuid: String = UUID.randomUUID.toString,
  eori: String,
  lrn: String,
  mrn: Option[String] = None,
  ducr: Option[String] = None,
  latestEnhancedStatus: Option[EnhancedStatus] = None,
  enhancedStatusLastUpdated: Option[ZonedDateTime] = None,
  actions: Seq[Action]
) {

  val latestAction: Option[Action] = if (actions.nonEmpty) {
    Some(actions.minBy(_.requestTimestamp)(Submission.dateTimeOrdering))
  } else {
    None
  }

  val latestCancellationAction: Option[Action] = {
    val cancelActions = actions.filter(_.requestType == CancellationRequest)
    if (cancelActions.nonEmpty) {
      Some(cancelActions.minBy(_.requestTimestamp)(Submission.dateTimeOrdering))
    } else None
  }

  lazy val allSubmissionRequestStatuses: Seq[EnhancedStatus] = {
    val maybeExistingStatuses = for {
      subRequestAction <- actions.filter(_.requestType == SubmissionRequest).headOption
      notificationSummaries <- subRequestAction.notifications
    } yield notificationSummaries.map(_.enhancedStatus)
    maybeExistingStatuses.getOrElse(Seq.empty[EnhancedStatus])
  }

  lazy val isStatusAcceptedOrReceived: Boolean =
    allSubmissionRequestStatuses.intersect(Seq(GOODS_ARRIVED_MESSAGE, GOODS_ARRIVED, RECEIVED)).nonEmpty
}

object Submission {

  val dateTimeOrdering: Ordering[ZonedDateTime] = Ordering.fromLessThan[ZonedDateTime]((a, b) => b.isBefore(a))

  implicit val formats = Json.format[Submission]

  implicit val ordering: Ordering[Submission] =
    Ordering.by[Submission, Option[ZonedDateTime]](submission => submission.latestAction.map(_.requestTimestamp))(Ordering.Option(dateTimeOrdering))

  val newestEarlierOrdering: Ordering[Submission] = ordering
}
