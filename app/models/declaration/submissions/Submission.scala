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

import models.declaration.submissions.EnhancedStatus._
import models.declaration.submissions.RequestType.{CancellationRequest, ExternalAmendmentRequest, SubmissionRequest}
import play.api.libs.json.{Json, OFormat}

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
  latestDecId: Option[String],
  latestVersionNo: Int = 1
) {
  lazy val allSubmissionRequestStatuses: Seq[EnhancedStatus] = (
    for {
      subRequestAction <- actions.find(_.requestType == SubmissionRequest)
      notificationSummaries <- subRequestAction.notifications
    } yield notificationSummaries.map(_.enhancedStatus)
  ).getOrElse(Seq.empty[EnhancedStatus])

  // 'latestDecId' can be empty only in legacy declarations.
  // 'latestDecId' was formerly emptied at reception of a DMSRES notification.
  lazy val blockAmendments: Boolean = latestDecId.isEmpty

  lazy val isStatusAcceptedOrReceived: Boolean =
    allSubmissionRequestStatuses.intersect(Seq(GOODS_ARRIVED_MESSAGE, GOODS_ARRIVED, RECEIVED)).nonEmpty

  lazy val hasExternalAmendments: Boolean = actions.exists(_.requestType == ExternalAmendmentRequest)

  lazy val latestAction: Option[Action] =
    if (actions.isEmpty) None
    else Some(actions.minBy(_.requestTimestamp)(Submission.dateTimeOrdering))

  lazy val latestCancellationAction: Option[Action] = {
    val cancelActions = actions.filter(_.requestType == CancellationRequest)
    if (cancelActions.nonEmpty) {
      Some(cancelActions.minBy(_.requestTimestamp)(Submission.dateTimeOrdering))
    } else None
  }

  def action(actionId: String): Option[Action] = actions.find(_.id == actionId)
}

object Submission {

  implicit val formats: OFormat[Submission] = Json.format[Submission]

  val dateTimeOrdering: Ordering[ZonedDateTime] = Ordering.fromLessThan[ZonedDateTime]((a, b) => b.isBefore(a))

  implicit val ordering: Ordering[Submission] = Ordering.by[Submission, Option[ZonedDateTime]] { submission =>
    submission.latestAction.map(_.requestTimestamp)
  }(Ordering.Option(dateTimeOrdering))
}
