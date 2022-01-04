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

import java.time.ZonedDateTime
import java.util.UUID

import play.api.libs.json.Json

case class Submission(
  uuid: String = UUID.randomUUID().toString,
  eori: String,
  lrn: String,
  mrn: Option[String] = None,
  ducr: Option[String] = None,
  actions: Seq[Action]
) {

  val latestAction: Option[Action] = if (actions.nonEmpty) {
    Some(actions.minBy(_.requestTimestamp)(Submission.dateTimeOrdering))
  } else {
    None
  }
}

object Submission {

  val dateTimeOrdering: Ordering[ZonedDateTime] = Ordering.fromLessThan[ZonedDateTime]((a, b) => a.isBefore(b))

  implicit val formats = Json.format[Submission]

  implicit val ordering: Ordering[Submission] =
    Ordering.by[Submission, Option[ZonedDateTime]](submission => submission.latestAction.map(_.requestTimestamp))(Ordering.Option(dateTimeOrdering))

  val newestEarlierOrdering: Ordering[Submission] = ordering.reverse
}
